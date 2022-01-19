package net.teamuni.rewardapi.data;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

public class PlayerDataManager implements Closeable {

    private final RewardAPI instance;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Map<UUID, List<Consumer<PlayerData>>> loadingTask = new HashMap<>();
    private final SpongeExecutorService scheduler;
    private final ExecutorService singleThread;

    public PlayerDataManager(RewardAPI instance, int saveInterval) {
        this.instance = instance;
        this.scheduler = Sponge.getScheduler().createSyncExecutor(this.instance);
        this.singleThread = Executors.newSingleThreadExecutor();
        Task.builder()
            .name("RewardAPI - Store In Database")
            .interval(saveInterval, TimeUnit.SECONDS)
            .execute(this::saveAllData)
            .submit(instance);
    }

    private void loadPlayerData(UUID uuid) {
        if (this.playerDataMap.containsKey(uuid) || loadingTask.containsKey(uuid)) {
            return;
        }

        this.loadingTask.put(uuid, new ArrayList<>());
        CompletableFuture
            .supplyAsync(() -> new PlayerData(uuid, this.instance.getDatabase().load(uuid)), this.singleThread)
            .thenAcceptAsync((rewards) -> {
                this.playerDataMap.put(uuid, rewards);
                for (Consumer<PlayerData> callback : loadingTask.remove(uuid)) {
                    callback.accept(rewards);
                }
            }, this.scheduler);
    }

    public void usePlayerData(UUID uuid, Consumer<PlayerData> consumer) {
        if (this.playerDataMap.containsKey(uuid)) {
            consumer.accept(this.playerDataMap.get(uuid));
            return;
        }
        if (this.loadingTask.containsKey(uuid)) {
            this.loadingTask.get(uuid).add(consumer);
            return;
        }
        loadPlayerData(uuid);
        if (this.loadingTask.containsKey(uuid)) {
            this.loadingTask.get(uuid).add(consumer);
        } else if (playerDataMap.containsKey(uuid)) {
            consumer.accept(this.playerDataMap.get(uuid));
        } else {
            throw new RuntimeException("Failed to load player data.");
        }
    }

    public @Nullable PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    private void savePlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        List<Reward> rewards = playerDataMap.get(uuid).getRewards();
        CompletableFuture
            .runAsync(() -> this.instance.getDatabase().save(uuid, rewards), this.singleThread);
    }

    private void saveAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }

        for (PlayerData playerData : playerDataMap.values()) {
            if (playerData.isChanged) {
                savePlayerData(playerData.uuid);
            }
        }
    }

    private void unloadAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }
        saveAllData();
        playerDataMap.clear();
    }

    @Override
    public void close() {
        unloadAllData();
        singleThread.shutdown();
        try {
            if (!singleThread.awaitTermination(30, TimeUnit.SECONDS)) {
                singleThread.shutdownNow();
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static final class PlayerData {

        private final UUID uuid;
        private final ArrayList<Reward> rewards;
        private boolean isChanged = false;

        private PlayerData(UUID uuid, List<Reward> rewards) {
            this.uuid = uuid;
            this.rewards = new ArrayList<>(rewards);
        }

        public UUID getUuid() {
            return uuid;
        }

        public List<Reward> getRewards() {
            return Collections.unmodifiableList(rewards);
        }

        public void addReward(Reward reward) {
            this.isChanged = true;
            rewards.add(reward);
        }

        public Reward removeReward(int index) {
            this.isChanged = true;
            return rewards.remove(index);
        }
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        UUID uuid = event.getTargetEntity().getUniqueId();
        loadPlayerData(uuid);
    }
}

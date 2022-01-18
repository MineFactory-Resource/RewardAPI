package net.teamuni.rewardapi.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.database.Database;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.SpongeExecutorService;

public class PlayerDataManager {

    private final RewardAPI instance;
    private final Map<UUID, List<Reward>> playerDataMap = new HashMap<>();
    private final Map<UUID, List<Consumer<List<Reward>>>> loadingTask = new HashMap<>();
    private final SpongeExecutorService scheduler;
    private final ExecutorService singleThread;

    public PlayerDataManager(RewardAPI instance) {
        this.instance = instance;
        this.scheduler = Sponge.getScheduler().createSyncExecutor(this.instance);
        this.singleThread = Executors.newSingleThreadExecutor();
    }

    private void loadPlayerData(UUID uuid) {
        if (this.playerDataMap.containsKey(uuid) || loadingTask.containsKey(uuid)) {
            return;
        }

        this.loadingTask.put(uuid, new ArrayList<>());
        CompletableFuture
            .supplyAsync(() -> Arrays.asList(this.instance.getDatabase().load(uuid)), this.singleThread)
            .thenAcceptAsync((rewards) -> {
                this.playerDataMap.put(uuid, rewards);
                for (Consumer<List<Reward>> callback : loadingTask.remove(uuid)) {
                    callback.accept(rewards);
                }
            }, this.scheduler);
    }

    public void usePlayerData(UUID uuid, Consumer<List<Reward>> consumer) {
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

    public @Nullable List<Reward> getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    private void savePlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        Reward[] rewards = playerDataMap.get(uuid).toArray(new Reward[0]);
        CompletableFuture
            .runAsync(() -> this.instance.getDatabase().save(uuid, rewards), this.singleThread);
    }

    public void unloadPlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        savePlayerData(uuid);
        playerDataMap.remove(uuid);
    }

    public void unloadAllData() {
        if (playerDataMap.isEmpty()) {
            return;
        }
        Database database = instance.getDatabase();
        for (Map.Entry<UUID, List<Reward>> entry : playerDataMap.entrySet()) {
            database.save(entry.getKey(), entry.getValue().toArray(new Reward[0]));
        }
        playerDataMap.clear();
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        UUID uuid = event.getTargetEntity().getUniqueId();
        loadPlayerData(uuid);
    }

    @Listener
    public void onPlayerLeft(ClientConnectionEvent.Disconnect event) {
        UUID uuid = event.getTargetEntity().getUniqueId();
        unloadPlayerData(uuid);
    }
}

package net.teamuni.rewardapi.data;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.kyori.adventure.sound.Sound;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.database.Database.Action;
import net.teamuni.rewardapi.data.object.Reward;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PlayerDataManager implements Listener, Closeable {

    private final RewardAPI instance;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final Map<UUID, List<Consumer<PlayerData>>> loadingTask = new HashMap<>();
    private final ExecutorService singleThread;

    public PlayerDataManager(RewardAPI instance, int saveInterval) {
        this.instance = instance;
        this.singleThread = Executors.newSingleThreadExecutor();
        Bukkit.getScheduler().runTaskTimer(instance, this::saveAllData, saveInterval* 20L, saveInterval * 20L);
    }

    private void loadPlayerData(UUID uuid) {
        if (this.playerDataMap.containsKey(uuid) || loadingTask.containsKey(uuid)) {
            return;
        }

        this.loadingTask.put(uuid, new ArrayList<>());
        this.singleThread.execute(() -> {
            PlayerData playerData = new PlayerData(uuid, this.instance.getDatabase().load(uuid));
            Bukkit.getScheduler().runTask(this.instance, () -> {
                this.playerDataMap.put(uuid, playerData);
                for (Consumer<PlayerData> callback : loadingTask.remove(uuid)) {
                    callback.accept(playerData);
                }
            });
        });
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
        this.singleThread.execute(() -> instance.getDatabase().save(uuid, rewards));
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        loadPlayerData(uuid);
        usePlayerData(uuid, data -> {
            if (data == null || data.getRewards().isEmpty()) return;
            String msg = RewardAPI.getInstance().getMessageStorage().getRawMessage("left_overs",  data.getRewards().size());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        });
    }

    public final class PlayerData {

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
            applyPlayer(player -> {
                player.sendMessage(RewardAPI.getInstance().getMessageStorage().getMessage("add_reward"));
                player.playSound(RewardAPI.getInstance().getSoundStorage().getSound("add_reward"));
            });
            instance.getDatabase().log(Action.RECEIVED, uuid, reward);
        }

        public Reward removeReward(int index) {
            this.isChanged = true;
            return rewards.remove(index);
        }

        private void applyPlayer(Consumer<Player> consumer) {
            Player player = Bukkit.getPlayer(this.uuid);
            if (player == null || !player.isOnline()) return;
            consumer.accept(player);
        }
    }
}

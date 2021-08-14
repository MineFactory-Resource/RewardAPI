package net.teamuni.rewardapi.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.PlayerDataManager;

public class StorageBox {

    private final UUID uuid;
    private final List<Reward> rewards;
    private static final Map<UUID, StorageBox> storageBoxMap = new HashMap<>();

    private StorageBox(UUID uuid, List<Reward> rewards) {
        this.uuid = uuid;
        this.rewards = rewards;
    }

    public static StorageBox getStorageBox(UUID uuid) {
        if (storageBoxMap.containsKey(uuid)) {
            return storageBoxMap.get(uuid);
        }

        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        List<Reward> list = playerDataManager.getPlayerData(uuid);

        StorageBox storageBox = new StorageBox(uuid, list);
        storageBoxMap.put(uuid, storageBox);
        return storageBox;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Reward> getRewards() {
        return rewards;
    }
}

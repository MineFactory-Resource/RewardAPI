package net.teamuni.rewardapi.api;

import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.data.object.Reward;

public class StorageBoxAPI {
    private StorageBoxAPI() {}

    private static class InnerInstanceClazz {
        private static final StorageBoxAPI uniqueInstance = new StorageBoxAPI();
    }

    public static StorageBoxAPI getInstance() {
        return InnerInstanceClazz.uniqueInstance;
    }

    public void give(UUID uuid, Reward reward) {
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        playerDataManager.usePlayerData(uuid, (playerData) -> {playerData.addReward(reward);});
    }

    // TODO
}

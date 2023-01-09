package net.teamuni.rewardapi.api;

import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.data.object.Reward;

public class StorageBoxAPI {

    private StorageBoxAPI() {
    }

    public static void give(UUID uuid, Reward reward) {
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        playerDataManager.usePlayerData(uuid, data -> data.addReward(reward));
    }

    // TODO
}

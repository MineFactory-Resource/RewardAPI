package net.teamuni.rewardapi.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class StorageBox {

    private final UUID uuid;
    private final ArrayList<Reward> rewards;

    private StorageBox(UUID uuid) {
        this.uuid = uuid;
        this.rewards = new ArrayList<>();
    }
    private StorageBox(UUID uuid, Reward[] rewards) {
        this(uuid);
        this.rewards.addAll(Arrays.asList(rewards));
    }

    public static StorageBox getStorageBox(UUID uuid) {
        return null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ArrayList<Reward> getRewards() {
        return rewards;
    }
}

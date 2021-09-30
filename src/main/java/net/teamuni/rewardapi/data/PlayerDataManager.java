package net.teamuni.rewardapi.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.database.Database;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerDataManager {

    private final RewardAPI instance;
    private final Map<UUID, List<Reward>> playerDataMap = new HashMap<>();

    public PlayerDataManager(RewardAPI instance) {
        this.instance = instance;
    }

    public void loadPlayerData(UUID uuid) {
        if (playerDataMap.containsKey(uuid)) {
            return;
        }
        // TODO 비동기
        Reward[] rewards = instance.getDatabase().load(uuid);
        playerDataMap.put(uuid, new ArrayList<>(Arrays.asList(rewards)));
    }

    public void savePlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            return;
        }
        instance.getDatabase().save(uuid, playerDataMap.get(uuid).toArray(new Reward[0]));
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

    public List<Reward> getPlayerData(UUID uuid) {
        loadPlayerData(uuid);
        return playerDataMap.get(uuid);
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

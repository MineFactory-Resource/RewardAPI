package net.teamuni.rewardapi.database;

import java.io.IOException;
import java.util.UUID;
import net.teamuni.rewardapi.api.Reward;

public interface Database {
    Reward[] load(UUID uuid) throws IOException;
    void save(UUID uuid, Reward[] rewards);
}
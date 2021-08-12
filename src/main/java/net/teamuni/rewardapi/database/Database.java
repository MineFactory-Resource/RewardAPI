package net.teamuni.rewardapi.database;

import java.util.UUID;
import net.teamuni.rewardapi.api.Reward;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Database {
    @NonNull Reward[] load(@NonNull UUID uuid);
    void save(@NonNull UUID uuid, @NonNull Reward[] rewards);
}
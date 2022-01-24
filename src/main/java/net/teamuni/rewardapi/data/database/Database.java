package net.teamuni.rewardapi.data.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import net.teamuni.rewardapi.serializer.ItemStackSerializer;
import net.teamuni.rewardapi.serializer.RewardSerializer;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class Database {

    protected final RewardAPI instance;
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Reward.class, new RewardSerializer())
        .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
        .create();

    protected Database(RewardAPI instance) {
        this.instance = instance;
    }

    public @NonNull List<Reward> load(@NonNull UUID uuid) {
        String json;
        try {
            json = loadJson(uuid);
        } catch (IOException | SQLException e) {
            this.instance.getLogger().error("Failed to load data. ("+uuid+")", e);
            return Collections.emptyList();
        }

        if (json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return Arrays.asList(gson.fromJson(json, Reward[].class));
        } catch (JsonSyntaxException e) {
            this.instance.getLogger().error("Failed to parse json data. ("+uuid+")", e);
            return Collections.emptyList();
        }
    }

    public void save(@NonNull UUID uuid, @NonNull List<Reward> rewards) {
        String json = rewards.size() > 0 ? gson.toJson(rewards) : "{\"rewards\":[]}";
        try {
            saveJson(uuid, json);
        } catch (IOException | SQLException e) {
            this.instance.getLogger().error("Failed to save data. ("+uuid+")", e);
        }
    }

    abstract protected @NonNull String loadJson(@NonNull UUID uuid) throws IOException, SQLException;
    abstract protected void saveJson(@NonNull UUID uuid, @NonNull String json)
        throws IOException, SQLException;
}
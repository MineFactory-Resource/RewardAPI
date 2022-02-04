package net.teamuni.rewardapi.data.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import net.teamuni.rewardapi.serializer.RewardSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class Database {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Reward.class, new RewardSerializer())
        .create();
    protected final RewardAPI instance;

    protected Database(RewardAPI instance) {
        this.instance = instance;
    }

    public @NonNull List<Reward> load(@NonNull UUID uuid) {
        String json;
        try {
            json = loadJson(uuid);
        } catch (IOException | SQLException e) {
            this.instance.getLogger().log(Level.SEVERE, "Failed to load data. (" + uuid + ")", e);
            return Collections.emptyList();
        }

        if (json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return Arrays.asList(
                gson.fromJson(JsonParser.parseString(json).getAsJsonObject().get("rewards"),
                    Reward[].class));
        } catch (JsonSyntaxException e) {
            this.instance.getLogger()
                .log(Level.SEVERE, "Failed to parse json data. (" + uuid + ")", e);
            return Collections.emptyList();
        }
    }

    public void save(@NonNull UUID uuid, @NonNull List<Reward> rewards) {
        String json;
        if (rewards.size() > 0) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("rewards", gson.toJsonTree(rewards.toArray(), Reward[].class));
            json = jsonObject.toString();
        } else {
            json = "{\"rewards\":[]}";
        }
        try {
            saveJson(uuid, json);
        } catch (IOException | SQLException e) {
            this.instance.getLogger().log(Level.SEVERE, "Failed to save data. (" + uuid + ")", e);
        }
    }

    abstract protected @NonNull String loadJson(@NonNull UUID uuid)
        throws IOException, SQLException;

    abstract protected void saveJson(@NonNull UUID uuid, @NonNull String json)
        throws IOException, SQLException;
}
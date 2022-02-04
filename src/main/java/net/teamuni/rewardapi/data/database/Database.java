package net.teamuni.rewardapi.data.database;

import com.google.common.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.data.object.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class Database {

    protected final RewardAPI instance;

    protected Database(RewardAPI instance) {
        this.instance = instance;
    }

    @SuppressWarnings("UnstableApiUsage")
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

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader
            .builder()
            .setSource(() -> new BufferedReader(new StringReader(json)))
            .build();
        try {
            ConfigurationNode node = loader.load();
            return node.getNode("rewards").getList(TypeToken.of(Reward.class));
        } catch (ObjectMappingException | IOException e) {
            this.instance.getLogger().error("Failed to parse json data. ("+uuid+")", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void save(@NonNull UUID uuid, @NonNull List<Reward> rewards) {
        String json;
        if (rewards.size() > 0) {
            StringWriter sw = new StringWriter();
            ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader
                .builder()
                .setSink(() -> new BufferedWriter(sw))
                .build();
            ConfigurationNode node = loader.createEmptyNode();
            try {
                node.getNode("rewards").setValue(new TypeToken<List<Reward>>() {}, rewards);
                loader.save(node);
            } catch (IOException | ObjectMappingException e) {
                this.instance.getLogger().error("Failed to parse json data. (" + uuid + ")", e);
            }
            json = sw.toString();
        } else {
            json = "{\"rewards\":[]}";
        }

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
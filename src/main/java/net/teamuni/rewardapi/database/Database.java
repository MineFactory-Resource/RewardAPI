package net.teamuni.rewardapi.database;

import com.google.common.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.Reward;
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
    public @NonNull Reward[] load(@NonNull UUID uuid) {
        String json;
        try {
            json = loadJson(uuid);
        } catch (IOException | SQLException e) {
            this.instance.getLogger().error("Failed to load data. ("+uuid+")", e);
            return new Reward[0];
        }

        if (json.isEmpty()) {
            return new Reward[0];
        }

        ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader
            .builder()
            .setSource(() -> new BufferedReader(new StringReader(json)))
            .build();
        try {
            ConfigurationNode node = loader.load();
            List<Reward> rewardList = node.getNode("rewards").getList(TypeToken.of(Reward.class));
            return rewardList.toArray(new Reward[0]);
        } catch (ObjectMappingException | IOException e) {
            this.instance.getLogger().error("Failed to parse json data. ("+uuid+")", e);
            return new Reward[0];
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void save(@NonNull UUID uuid, @NonNull Reward[] rewards) {
        String json;
        if (rewards.length > 0) {
            StringWriter sw = new StringWriter();
            ConfigurationLoader<ConfigurationNode> loader = GsonConfigurationLoader
                .builder()
                .setSink(() -> new BufferedWriter(sw))
                .build();
            ConfigurationNode node = loader.createEmptyNode();
            try {
                node.getNode("rewards").setValue(new TypeToken<List<Reward>>() {}, Arrays.asList(rewards));
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
package net.teamuni.rewardapi.database;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

public class YamlDatabase implements Database {

    private final RewardAPI instance;
    private final Path dataFolder;

    public YamlDatabase(RewardAPI instance, Path dataFolder) throws IOException {
        this.instance = instance;
        this.dataFolder = dataFolder.toAbsolutePath().resolve("data");
    }

    @Override
    public @NonNull Reward[] load(@NonNull UUID uuid) {
        Path dataPath = null;
        try {
            dataPath = checkDataFile(uuid);
        } catch (IOException e) {
            instance.getLogger().error("Could not load data file. ("+uuid+")", e);
            return new Reward[0];
        }
        ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader
            .builder()
            .setPath(dataPath)
            .setDefaultOptions(instance.getConfigOptions())
            .build();
        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException e) {
            instance.getLogger().error("Could not load data file. ("+uuid+")", e);
            return new Reward[0];
        }

        try {
            List<Reward> rewardList = node.getList(TypeToken.of(Reward.class));
            return rewardList.toArray(new Reward[0]);
        } catch (ObjectMappingException e) {
            instance.getLogger().error("Could not load data file. ("+uuid+")", e);
            return new Reward[0];
        }
    }

    @Override
    public void save(@NonNull UUID uuid, @NonNull Reward[] rewards) {
        Path dataPath = null;
        try {
            dataPath = checkDataFile(uuid);
        } catch (IOException e) {
            instance.getLogger().error("Could not save data file. ("+uuid+")", e);
            return;
        }
        ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader
            .builder()
            .setPath(dataPath)
            .setDefaultOptions(instance.getConfigOptions())
            .build();
        ConfigurationNode node = loader.createEmptyNode();
        try {
            node.setValue(new TypeToken<List<Reward>>() {}, Arrays.asList(rewards));
        } catch (ObjectMappingException e) {
            instance.getLogger().error("Could not save data file. ("+uuid+")", e);
            return;
        }
        try {
            loader.save(node);
        } catch (IOException e) {
            instance.getLogger().error("Could not save data file. ("+uuid+")", e);
        }
    }

    private Path checkDataFile(UUID uuid) throws IOException {
        if (!Files.exists(dataFolder)) Files.createDirectories(dataFolder);
        Path dataPath = dataFolder.resolve(uuid.toString()+".yml");
        if (!Files.exists(dataPath)) {
            Files.createFile(dataPath);
        }
        return dataPath;
    }
}

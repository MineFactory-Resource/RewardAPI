package net.teamuni.rewardapi.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.teamuni.rewardapi.api.Reward;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

public class YamlDatabase implements Database {

    private final Path dataFolder;

    public YamlDatabase(Path dataFolder) throws IOException {
        this.dataFolder = dataFolder;
        if (!Files.exists(dataFolder)) Files.createDirectory(dataFolder);
    }

    @Override
    public Reward[] load(UUID uuid) throws IOException {
        Path dataPath = dataFolder.resolve(uuid.toString()+".yml");
        if (!Files.exists(dataPath)) Files.createFile(dataPath);
        ConfigurationLoader<ConfigurationNode> loader = YAMLConfigurationLoader.builder().setPath(dataPath).build();
        ConfigurationNode node;
        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            node = loader.createEmptyNode(ConfigurationOptions.defaults());
        }



        return new Reward[0];
    }

    @Override
    public void save(UUID uuid, Reward[] rewards) {

    }
}

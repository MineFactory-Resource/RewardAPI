package net.teamuni.rewardapi.config;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.teamuni.rewardapi.RewardAPI;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

public class ConfigManager {

    protected final Logger logger;
    private final Path file;
    private final HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;
    private ConfigurationNode defaults = ConfigurationNode.root();

    public ConfigManager(String fileName) {
        Path folder = RewardAPI.getInstance().getConfigDir();
        this.logger = RewardAPI.getInstance().getLogger();
        folder.toFile().mkdirs();
        this.file = folder.resolve(fileName);
        Sponge.getAssetManager().getAsset(RewardAPI.getInstance(), fileName)
            .ifPresent(asset -> {
                try {
                    if (!Files.exists(this.file)) {
                        asset.copyToFile(this.file);
                    }
                    defaults = HoconConfigurationLoader.builder().setURL(asset.getUrl()).build()
                        .load();
                } catch (IOException e) {
                    this.logger.error("Failed to load default config file: " + fileName, e);
                }
            });
        if (!Files.exists(this.file)) {
            try {
                Files.createFile(this.file);
            } catch (Exception e) {
                this.logger.error("Failed to create config file: " + fileName, e);
            }
        }
        this.loader = HoconConfigurationLoader.builder().setPath(this.file).build();
        try {
            this.node = this.loader.load();
        } catch (Exception e) {
            logger.error("Failed to load config file: " + fileName, e);
        }
    }

    public ConfigManager(ConfigManager superConfig, String... startNode) {
        this.logger = RewardAPI.getInstance().getLogger();
        this.file = superConfig.file;
        this.loader = superConfig.loader;
        this.node = superConfig.node.getNode((Object[]) startNode);
    }

    public void setComment(String comment, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            node.setComment(comment);
        } catch (Exception e) {
            this.logger.error("Failed to set comment: " + file.getFileName(), e);
        }
    }

    public <T> void setDefault(String comment, Class<T> type, T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            if (node.isVirtual()) {
                node.setValue(TypeToken.of(type), value);
            }
            if (!comment.isEmpty()) {
                node.setComment(comment);
            }
        } catch (Exception e) {
            this.logger.error("Failed to set default: " + file.getFileName(), e);
        }
    }

    public <T> void setDefault(Class<T> type, T value, String... nodes) {
        setDefault("", type, value, nodes);
    }

    @SuppressWarnings("UnstableApiUsage")
    public <T> Optional<T> getValue(Class<T> type, String... nodes) {
        return getValue(TypeToken.of(type), nodes);
    }

    public <T> T getValue(Class<T> type, T defaultValue, String... nodes) {
        return getValue(type, nodes).orElse(defaultValue);
    }

    @SuppressWarnings("UnstableApiUsage")
    public <T> Optional<T> getValue(TypeToken<T> token, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);

        T value = null;
        try {
            value = node.getValue(token);
        } catch (ObjectMappingException e) {
            this.logger.error("Failed to map object: " + file.getFileName(), e);
        }

        if (value == null) {
            try {
                value = defaults.getNode((Object[]) nodes).getValue(token);
            } catch (ObjectMappingException ignored) {
            }
        }

        return Optional.ofNullable(value);
    }

    public String getString(String def, String... nodes) {
        return getValue(String.class, nodes).orElse(def);
    }

    public <T> void setValue(TypeToken<T> token, T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            node.setValue(token, value);
        } catch (Exception e) {
            this.logger.error("Failed to set value: " + file.getFileName(), e);
        }
    }

    public CommentedConfigurationNode getNode() {
        return this.node;
    }

    public void overwriteNode(CommentedConfigurationNode node) {
        this.node = node;
        save();
    }

    public void save() {
        try {
            this.loader.save(this.node);
        } catch (Exception e) {
            this.logger.error("Failed to save config file: " + file.getFileName(), e);
        }
    }
}

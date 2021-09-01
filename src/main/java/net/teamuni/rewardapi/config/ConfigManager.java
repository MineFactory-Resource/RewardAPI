package net.teamuni.rewardapi.config;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import com.google.common.reflect.TypeToken;
import java.nio.file.Path;
import net.teamuni.rewardapi.RewardAPI;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

public class ConfigManager {

    protected final Logger logger;
    private final Path file;
    private final HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;

    public ConfigManager(Path folder, String fileName, Logger logger) {
        this.logger = logger;
        folder.toFile().mkdirs();
        this.file = folder.resolve(fileName);
        if (!Files.exists(this.file)) {
            Sponge.getAssetManager().getAsset(RewardAPI.getInstance(), fileName)
                .ifPresent(asset -> {
                    try {
                        asset.copyToFile(this.file);
                    } catch (IOException e) {
                        this.logger.error("Failed to load default config file: " + fileName, e);
                    }
                });
        }
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

    public <T> Optional<T> getValue(Class<T> type, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
        try {
            return Optional.ofNullable(node.getValue(TypeToken.of(type)));
        } catch (Exception e) {
            this.logger.error("Failed to map object: " + file.getFileName(), e);
            return Optional.empty();
        }
    }

    public <T> T getValue(Class<T> type, T defaultValue, String... nodes) {
        CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
        if (node.isVirtual()) {
            return defaultValue;
        }
        try {
            return node.getValue(TypeToken.of(type));
        } catch (Exception e) {
            this.logger.error("Failed to map object: " + file.getFileName(), e);
            return defaultValue;
        }
    }

    public <T> T getValue(T defaultValue, Class<T> type, String... nodes) {
        return this.getValue(type, defaultValue, nodes);
    }

    public <T> void setValue(T value, String... nodes) {
        try {
            CommentedConfigurationNode node = this.node.getNode((Object[]) nodes);
            node.setValue(value);
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

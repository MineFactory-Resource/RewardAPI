package net.teamuni.rewardapi.config;

import net.teamuni.rewardapi.RewardAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.Text;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.Map;

public class MessageStorage extends ConfigManager {

    private final Map<String, String> defaults = new HashMap<>();

    public MessageStorage(Path folder, Logger logger) {
        super(folder, "message.conf", logger);
        Sponge.getAssetManager().getAsset(RewardAPI.getInstance(), "message.conf")
            .ifPresent(asset -> {
                try {
                    HoconConfigurationLoader.builder().setURL(asset.getUrl()).build().load()
                        .getChildrenList().forEach(child -> {
                            defaults.put((String) child.getKey(), child.getString());
                        });
                } catch (IOException e) {
                    logger.error("Failed to load default message file", e);
                }
            });
    }

    public String getRawMessage(String key) {
        ConfigurationNode childNode = getNode().getNode(key);
        return childNode.isVirtual() ? defaults.getOrDefault(key, "Not message loaded")
            : childNode.getString();
    }

    public Text getMessage(String key) {
        return TextSerializers.FORMATTING_CODE.deserialize(getRawMessage(key));
    }
}

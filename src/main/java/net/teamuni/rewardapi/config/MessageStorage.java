package net.teamuni.rewardapi.config;

import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.Text;

public class MessageStorage extends ConfigManager {

    public MessageStorage(ConfigManager superConfig, String... startNode) {
        super(superConfig, startNode);
    }

    public String getRawMessage(String... key) {
        return getValue(String.class, "Not message loaded: " + String.join(".", key), key);
    }

    public Text getMessage(String... key) {
        return TextSerializers.FORMATTING_CODE.deserialize(getRawMessage(key));
    }
}

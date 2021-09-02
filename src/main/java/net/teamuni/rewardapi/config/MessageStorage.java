package net.teamuni.rewardapi.config;

import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.Text;
import org.slf4j.Logger;
import java.nio.file.Path;

public class MessageStorage extends ConfigManager {

    public MessageStorage(Path folder, Logger logger) {
        super(folder, "message.conf", logger);
    }

    public String getRawMessage(String... key) {
        return getValue(String.class, "Not message loaded: " + String.join(".", key), key);
    }

    public Text getMessage(String... key) {
        return TextSerializers.FORMATTING_CODE.deserialize(getRawMessage(key));
    }
}

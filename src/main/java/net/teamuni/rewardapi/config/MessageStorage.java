package net.teamuni.rewardapi.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;

public class MessageStorage extends ConfigManager {

    public MessageStorage(ConfigManager superConfig, String startPath) {
        super(superConfig, startPath);
    }

    public String getRawMessage(String key) {
        return getValue(String.class, "Not message loaded: " + String.join(".", key), key);
    }

    public TextComponent getMessage(String key) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', getRawMessage(key)));
    }
}

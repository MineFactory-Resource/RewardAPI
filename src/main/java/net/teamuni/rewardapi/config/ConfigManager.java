package net.teamuni.rewardapi.config;

import java.util.Optional;
import java.util.logging.Logger;
import net.teamuni.rewardapi.RewardAPI;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigManager {

    protected final Logger logger;
    private ConfigurationSection section;

    public ConfigManager() {
        RewardAPI instance = RewardAPI.getInstance();
        this.logger = instance.getLogger();
        instance.saveDefaultConfig();
        instance.reloadConfig();
        this.section = instance.getConfig();
    }

    public ConfigManager(ConfigManager superConfig, String startPath) {
        this.logger = RewardAPI.getInstance().getLogger();
        this.section = superConfig.section.getConfigurationSection(startPath);
    }

    public <T> T getValue(Class<T> type, T defaultValue, String path) {
        return getValue(type, path).orElse(defaultValue);
    }

    public <T> Optional<T> getValue(Class<T> clazz, String path) {
        ConfigurationSection section = this.section.getConfigurationSection(path);
        if (section == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(section.getObject(path, clazz));
    }

    public String getString(String def, String path) {
        return getValue(String.class, path).orElse(def);
    }
}

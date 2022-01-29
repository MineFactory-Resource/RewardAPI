package net.teamuni.rewardapi.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.teamuni.rewardapi.RewardAPI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ConfigManager {

    protected final Logger logger;
    private final ConfigurationSection loot;

    public ConfigManager() {
        RewardAPI instance = RewardAPI.getInstance();
        this.logger = instance.getLogger();
        instance.saveDefaultConfig();
        instance.reloadConfig();
        this.loot = instance.getConfig();
    }

    public ConfigManager(ConfigManager superConfig, String startPath) {
        this.logger = RewardAPI.getInstance().getLogger();
        this.loot = superConfig.loot.getConfigurationSection(startPath);
    }

    public <T> T getValue(Class<T> type, T defaultValue, String path) {
        return getValue(type, path).orElse(defaultValue);
    }

    public <T> Optional<T> getValue(Class<T> clazz, String path) {
        return Optional.ofNullable(this.loot.getObject(path, clazz));
    }

    public <T> List<T> getValueList(Class<T> clazz, String path) {
        List<?> list = this.loot.getList(path);
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();

        for (Object object : list) {
            if (clazz.isInstance(object)) {
                result.add(clazz.cast(object));
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    public Optional<ItemStack> getSimpleItemStack(String path) {
        ConfigurationSection section = this.loot.getConfigurationSection(path);
        if (section == null) {
            return Optional.empty();
        }
        String type = section.getString("type");
        if (type == null) {
            return Optional.empty();
        }
        Material material = Material.matchMaterial(type);
        if (material == null) {
            material = Material.matchMaterial(type, true);
            if (material == null) {
                return Optional.empty();
            }
        }
        int data = section.getInt("data");
        String name =  section.getString("name");
        List<String> lore = section.getStringList("lore");
        ItemStack is = new ItemStack(material, 1);
        if (data != 0) {
            MaterialData md = is.getData();
            if (md != null) {
                md.setData((byte) data);
                is.setData(md);
            }
        }
        ItemMeta im = is.getItemMeta();
        if (name != null) {
            im.displayName(Component.text(name));
        }
        if (!lore.isEmpty()) {
            im.lore(lore.stream().map(Component::text).collect(Collectors.toList()));
        }
        is.setItemMeta(im);
        return Optional.of(is);
    }

    public Map<Character, ItemStack> getMapSimpleItemStack(String path) {
        ConfigurationSection section = this.loot.getConfigurationSection(path);
        if (section == null) {
            return new HashMap<>();
        }
        Map<Character, ItemStack> map = new HashMap<>();
        Set<String> keys = section.getKeys(false);
        for (String str : keys) {
            if (str.length() == 1) {
                Optional<ItemStack> is = getSimpleItemStack(path + "." + str);
                is.ifPresent(itemStack -> map.put(str.charAt(0), itemStack));
            }
        }
        return map;
    }
    public String getString(String def, String path) {
        return getValue(String.class, path).orElse(def);
    }
}

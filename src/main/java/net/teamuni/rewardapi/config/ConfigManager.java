package net.teamuni.rewardapi.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.teamuni.rewardapi.RewardAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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
                RewardAPI.getInstance().getLogger().log(Level.WARNING, "'%s' 이라는 아이템을 찾지 못했습니다.", type);
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
        if (im instanceof SkullMeta skullMeta) {
            String headName = section.getString("head-name");
            if (headName != null) {
                skullMeta.setOwner(headName);
            } else {
                String headCustom = section.getString("head-texture");
                if (headCustom != null) {
                    try {
                        setCustomSkull(skullMeta, headCustom);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        RewardAPI.getInstance().getLogger().log(Level.WARNING, "커스텀 머리를 불러오는데 실패하였습니다.", e);
                    }
                }
            }
        }
        if (name != null) {
            im.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', name)));
        }
        if (!lore.isEmpty()) {
            im.lore(lore.stream().map(s -> Component.text(ChatColor.translateAlternateColorCodes('&', s)))
                .collect(Collectors.toList()));
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

    public static void setCustomSkull(SkullMeta itemMeta, String value)
        throws NoSuchFieldException, IllegalAccessException {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        byte[] encodedData = Base64.getEncoder().encode(
            String.format("{textures:{SKIN:{url:\"https://textures.minecraft.net/texture/%s\"}}}", value)
                .getBytes(UTF_8));
        propertyMap.put("textures", new Property("textures", new String(encodedData)));

        Field profileField = itemMeta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        profileField.set(itemMeta, profile);
    }
}

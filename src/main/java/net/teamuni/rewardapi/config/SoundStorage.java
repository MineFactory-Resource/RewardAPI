package net.teamuni.rewardapi.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.intellij.lang.annotations.Subst;

public class SoundStorage extends ConfigManager {

    private static final Sound NULL_SOUND = Sound.sound(Key.key(Key.MINECRAFT_NAMESPACE), Sound.Source.MUSIC, 0f, 0f);

    public SoundStorage(ConfigManager superConfig, String startPath) {
        super(superConfig, startPath);
    }

    public Sound getSound(String key) {
        ConfigurationSection section = getValue(ConfigurationSection.class, key).orElse(null);
        if (section == null) return NULL_SOUND;
        @Subst(Key.MINECRAFT_NAMESPACE) String soundName = section.getString("sound");
        if (soundName == null) return NULL_SOUND;
        soundName = soundName.toLowerCase();
        float volume = (float) section.getDouble("volume", 0.0F);
        float pitch = (float) section.getDouble("pitch", 1.0F);
        return Sound.sound(Key.key(soundName), Sound.Source.MASTER, volume, pitch);
    }
}
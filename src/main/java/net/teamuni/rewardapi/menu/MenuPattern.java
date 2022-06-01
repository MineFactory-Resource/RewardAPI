package net.teamuni.rewardapi.menu;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MenuPattern {
    private final Map<Character, ItemStack> mapping = new HashMap<>();
    private final String pattern;
    private final List<ItemStack> turningButtons = Lists.newArrayListWithExpectedSize(4);

    public MenuPattern(@NonNull String pattern) {
        this.pattern = pattern;
    }

    @NonNull
    public String getPattern() {
        return this.pattern;
    }

    public void setItem(char key, @NonNull ItemStack is) {
        this.mapping.put(key, is);
    }

    public void setTurningButtons(List<ItemStack> iss) {
        turningButtons.clear();
        turningButtons.addAll(iss);
    }

    void apply(@NonNull Menu menu) {
        final int[] i = {0};
        this.pattern.chars().mapToObj(e -> (char)e).forEach(c ->
            menu.setItem(i[0]++, (c != '_' && c != ' ' && c != 'L' && c != 'R' ) ? this.mapping.get(c) : null));
    }

    void updateReward(@NonNull Menu menu, @NonNull List<ItemStack> rewards) {
        final int[] i = {0, 0};
        this.pattern.chars().mapToObj(e -> (char)e).forEach(c -> {
            if (c == '_') {
                menu.setItem(i[0], rewards.size() > i[1]++ ? rewards.get(i[1] - 1) : null);
            }
            i[0]++;
        });
    }

    void updateTurningButton(@NonNull Menu menu, boolean canTurnLeft, boolean canTurnRight) {
        menu.setItem(pattern.indexOf('L'),
            turningButtons.get(canTurnLeft ? 0 : 1));
        menu.setItem(pattern.indexOf('R'),
            turningButtons.get(canTurnRight ? 2 : 3));
    }
}

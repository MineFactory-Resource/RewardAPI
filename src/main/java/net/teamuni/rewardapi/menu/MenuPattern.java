package net.teamuni.rewardapi.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class MenuPattern {
    private final Map<Character, ItemStackSnapshot> mappping = new HashMap<>();
    private String pattern;

    @NonNull
    public MenuPattern setItem(char key, @NonNull ItemStackSnapshot is) {
        this.mappping.put(key, is);
        return this;
    }

    @NonNull
    public MenuPattern setPattern(@NonNull String pattern) {
        this.pattern = pattern;
        return this;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void apply(@NonNull Menu menu) {
        final int[] i = {0};
        this.pattern.chars().mapToObj(e -> (char)e).anyMatch(c -> {
            menu.setItem(i[0]++, (c != '_' && c != ' ') ? this.mappping.get(c).createStack() : ItemStack.empty());
            return i[0] >= menu.getCapacity();
        });
    }

    void updateReward(@NonNull Menu menu, @NonNull List<ItemStack> rewards) {
        final int[] i = {0, 0};
        this.pattern.chars().mapToObj(e -> (char)e).forEach(c -> {
            if (c == '_') {
                menu.setItem(i[0], rewards.size() > i[1]++ ? rewards.get(i[1] - 1) : ItemStack.empty());
            }
            i[0]++;
        });;
    }
}

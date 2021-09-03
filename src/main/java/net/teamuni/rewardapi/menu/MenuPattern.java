package net.teamuni.rewardapi.menu;

import java.util.HashMap;
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
    public MenuPattern setPattern(@NonNull String... pattern) {
        StringBuilder sb = new StringBuilder();
        for (String s : pattern) {
            sb.append(s);
        }
        this.pattern = sb.toString();
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
}

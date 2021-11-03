package net.teamuni.rewardapi.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class MenuPattern {
    private final Map<Character, ItemStackSnapshot> mappping = new HashMap<>();
    private final String pattern;

    public MenuPattern(@NonNull String pattern) {
        this.pattern = pattern;
    }

    @NonNull
    public String getPattern() {
        return this.pattern;
    }

    public void setItem(char key, @NonNull ItemStackSnapshot is) {
        this.mappping.put(key, is);
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

    void updateTurningButton(@NonNull Menu menu, boolean canTurnLeft, boolean canTurnRight) {
        // TODO 컨피그에서 아이템 가져와서 적용
    }
}

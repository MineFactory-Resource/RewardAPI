package net.teamuni.rewardapi.menu;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class MenuPattern {
    private final Map<Character, ItemStackSnapshot> mappping = new HashMap<>();
    private final String pattern;
    private final List<ItemStackSnapshot> turningButtons = Lists.newArrayListWithExpectedSize(4);

    private final int LEFT_CAN_TURNING_BUTTON = 0;
    private final int LEFT_CANT_TURNING_BUTTON = 1;
    private final int RIGHT_CAN_TURNING_BUTTON = 2;
    private final int RIGHT_CANT_TURNING_BUTTON = 3;

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

    public void setTurningButtons(List<ItemStackSnapshot> iss) {
        turningButtons.clear();
        turningButtons.addAll(iss);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void apply(@NonNull Menu menu) {
        final int[] i = {0};
        this.pattern.chars().mapToObj(e -> (char)e).anyMatch(c -> {
            menu.setItem(i[0]++, (c != '_' && c != ' ' && c != 'L' && c != 'R' ) ? this.mappping.get(c).createStack() : ItemStack.empty());
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
        menu.setItem(pattern.indexOf('L'),
            turningButtons.get(canTurnLeft ? LEFT_CAN_TURNING_BUTTON : LEFT_CANT_TURNING_BUTTON).createStack());
        menu.setItem(pattern.indexOf('R'),
            turningButtons.get(canTurnRight ? RIGHT_CAN_TURNING_BUTTON : RIGHT_CANT_TURNING_BUTTON).createStack());
    }
}

package net.teamuni.rewardapi.data.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Reward {

    private final ItemStack viewItem;

    protected Reward(ItemStack viewItem) {
        this.viewItem = viewItem;
    }

    public ItemStack getViewItem() {
        return viewItem;
    }

    public boolean isItemReward() {
        return this instanceof ItemReward;
    }

    abstract public boolean claim(Player player);
}

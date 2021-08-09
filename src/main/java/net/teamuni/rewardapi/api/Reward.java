package net.teamuni.rewardapi.api;

import org.spongepowered.api.item.inventory.ItemStack;

public abstract class Reward {

    private ItemStack viewItem;

    public Reward(ItemStack viewItem) {
        this.viewItem = viewItem;
    }

    public ItemStack getViewItem() {
        return viewItem;
    }

    public void setViewItem(ItemStack viewItem) {
        this.viewItem = viewItem;
    }
}

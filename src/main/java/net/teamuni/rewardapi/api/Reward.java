package net.teamuni.rewardapi.api;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public abstract class Reward {

    private ItemStackSnapshot viewItem;

    protected Reward(ItemStackSnapshot viewItem) {
        this.viewItem = viewItem;
    }

    public ItemStackSnapshot getViewItem() {
        return viewItem;
    }

    public void setViewItem(ItemStackSnapshot viewItem) {
        this.viewItem = viewItem;
    }

    public boolean isItemReward() {
        return this instanceof ItemReward;
    }
}

package net.teamuni.rewardapi.data.object;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public abstract class Reward {

    private final ItemStackSnapshot viewItem;

    protected Reward(ItemStackSnapshot viewItem) {
        this.viewItem = viewItem;
    }

    public ItemStackSnapshot getViewItem() {
        return viewItem;
    }

    public boolean isItemReward() {
        return this instanceof ItemReward;
    }
}

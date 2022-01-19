package net.teamuni.rewardapi.data.object;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ItemReward extends Reward {

    private ItemStackSnapshot[] rewardItems;

    public ItemReward(ItemStackSnapshot viewItem, ItemStackSnapshot[] itemStacks) {
        super(viewItem);
        this.rewardItems = itemStacks;
    }

    public ItemStackSnapshot[] getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(ItemStackSnapshot[] rewardItems) {
        this.rewardItems = rewardItems;
    }
}

package net.teamuni.rewardapi.data.object;

import org.bukkit.inventory.ItemStack;

public class ItemReward extends Reward {

    private ItemStack[] rewardItems;

    public ItemReward(ItemStack viewItem, ItemStack[] itemStacks) {
        super(viewItem);
        this.rewardItems = itemStacks;
    }

    public ItemStack[] getRewardItems() {
        return rewardItems;
    }

    public void setRewardItems(ItemStack[] rewardItems) {
        this.rewardItems = rewardItems;
    }
}

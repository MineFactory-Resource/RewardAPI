package net.teamuni.rewardapi.data.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Reward {

    private final ItemStack viewItem;
    private long receivedLogId = -1;

    protected Reward(ItemStack viewItem) {
        this.viewItem = viewItem;
    }

    public ItemStack getViewItem() {
        return viewItem;
    }

    abstract public boolean claim(Player player);

    public long getReceivedLogId() {
        return receivedLogId;
    }

    public void setReceivedLogId(long receivedLogId) {
        this.receivedLogId = receivedLogId;
    }
}

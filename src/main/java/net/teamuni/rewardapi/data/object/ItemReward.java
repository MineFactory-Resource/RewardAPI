package net.teamuni.rewardapi.data.object;

import java.util.Arrays;
import java.util.Map;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.config.MessageStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ItemReward extends Reward {

    private static final Inventory tempInv = Bukkit.createInventory(null, 36);
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

    @Override
    public boolean claim(Player player) {
        if (canFit(player, this.rewardItems)) {
            player.getInventory().addItem(this.rewardItems);
            return true;
        } else {
            MessageStorage messageStorage = RewardAPI.getInstance().getMessageStorage();
            player.sendMessage(messageStorage.getMessage("Menu.out_of_space"));
            return false;
        }
    }

    public static boolean canFit(Player p, ItemStack[] items) {
        PlayerInventory pInv = p.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack item = pInv.getItem(i);
            if (item != null) {
                tempInv.setItem(i, item.clone());
            }
        }
        Map<Integer, ItemStack> map = tempInv.addItem(Arrays.stream(items).map(ItemStack::clone).toArray(ItemStack[]::new));
        tempInv.clear();
        return map.isEmpty();
    }
}

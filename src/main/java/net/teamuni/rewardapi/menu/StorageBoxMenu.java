package net.teamuni.rewardapi.menu;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.config.MessageStorage;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.data.PlayerDataManager.PlayerData;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.data.object.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class StorageBoxMenu extends Menu {

    private static MenuPattern menuPattern;
    private static String title;
    private static int rows;
    private static int countReward;
    private static final Inventory tempInv = Bukkit.createInventory(null, 36);
    private final UUID uuid;
    private int page = 1;

    public StorageBoxMenu(UUID uuid) {
        super(title, rows);
        this.uuid = uuid;
        applyPattern(menuPattern);
        update();
    }

    public static void init() {
        ConfigManager menuConfig = RewardAPI.getInstance().getMenuConfig();

        title = menuConfig.getString("", "StorageBox.title");
        rows = menuConfig.getValue(Integer.class, 6, "StorageBox.rows");

        String pattern = String.join("",
            menuConfig.getValueList(String.class, "StorageBox.Pattern"));
        try {
            pattern = pattern.substring(0, rows * 9);
        } catch (StringIndexOutOfBoundsException ignored) {}

        menuPattern = new MenuPattern(pattern);
        countReward = countChar(pattern, '_');

        Map<Character, ItemStack> map = menuConfig.getMapSimpleItemStack("StorageBox.Items");
        map.entrySet().stream()
            .filter(entry ->
                entry.getKey() != ' ' && entry.getKey() != '_' &&
                    entry.getKey() != 'L' && entry.getKey() != 'R')
            .forEach(entry -> {
                menuPattern.setItem(entry.getKey(),
                    entry.getValue());
            });

        List<ItemStack> issList = Lists.newArrayListWithExpectedSize(4);
        String[] paths = new String[]{
            "StorageBox.Buttons.Left.Can",
            "StorageBox.Buttons.Left.Cant",
            "StorageBox.Buttons.Right.Can",
            "StorageBox.Buttons.Right.Cant"
        };
        for (String path : paths) {
            issList.add(menuConfig.getSimpleItemStack(path).orElse(new ItemStack(Material.AIR)));
        }
        menuPattern.setTurningButtons(issList);
    }

    private static int countChar(String str, char ch) {
        return Math.toIntExact(str.chars().filter(c -> c == ch).count());
    }

    public void update() {
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if (playerData == null) {
            return;
        }
        List<Reward> rewards = playerData.getRewards();

        int start = (page - 1) * countReward;
        int end = Math.min(page * countReward + 1, rewards.size());

        if (start > end) {
            return;
        }

        menuPattern.updateReward(this,
            rewards.subList(start, end)
                .stream()
                .map(Reward::getViewItem)
                .collect(Collectors.toList()));

        menuPattern.updateTurningButton(this,
            this.page != 1,
            Math.ceil((double) rewards.size() / countReward) >= this.page + 1);
    }

    @Override
    protected void onClick(Player player, int slotIndex, ItemStack clickItem,
        InventoryAction clickType) {
        String pattern = menuPattern.getPattern();
        char c = pattern.charAt(slotIndex);
        if (c != '_' && c != 'L' && c != 'R') {
            return;
        }

        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if (playerData == null) {
            return;
        }
        List<Reward> rewards = playerData.getRewards();

        if (c == 'L' && this.page != 1) {
            this.page--;
        } else if (c == 'R' && Math.ceil((double) rewards.size() / countReward) >= this.page + 1) {
            this.page++;
        } else if (c == '_') {
            int rewardIndex = getRewardIndex(slotIndex);
            if (rewardIndex >= rewards.size()) {
                return;
            }
            Reward reward = rewards.get(rewardIndex);
            if (reward.isItemReward()) {
                ItemReward itemReward = (ItemReward) reward;
                if (canFit(player, itemReward.getRewardItems())) {
                    player.getInventory().addItem(itemReward.getRewardItems());
                } else {
                    MessageStorage messageStorage = RewardAPI.getInstance().getMessageStorage();
                    player.sendMessage(messageStorage.getMessage("Menu.out_of_space"));
                    return;
                }
            } else {
                CommandReward commandReward = (CommandReward) reward;
                for (String command : commandReward.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
            playerData.removeReward(rewardIndex);
            // TODO 이펙트
        } else {
            return;
        }
        Bukkit.getScheduler().runTask(RewardAPI.getInstance(), this::update);
    }

    private int getRewardIndex(int slotIndex) {
        String pattern = menuPattern.getPattern();

        int tmp = 0;
        int n = 0;
        for (char c : pattern.toCharArray()) {
            if (c == '_') {
                tmp++;
            }
            if (n++ >= slotIndex) {
                break;
            }
        }
        return (page - 1) * countReward + tmp - 1;
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

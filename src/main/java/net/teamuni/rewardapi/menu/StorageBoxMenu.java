package net.teamuni.rewardapi.menu;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.CommandReward;
import net.teamuni.rewardapi.api.ItemReward;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.config.MessageStorage;
import net.teamuni.rewardapi.config.SimpleItemStack;
import net.teamuni.rewardapi.data.PlayerDataManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.UserInventory;

public class StorageBoxMenu extends Menu {

    private static MenuPattern menuPattern;
    private static String title;
    private static int rows;
    private static int countReward;

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

        title = menuConfig.getString("", "storagebox", "title");
        rows = menuConfig.getValue(Integer.class, 6, "storagebox", "rows");

        String pattern = String.join("", menuConfig.getValue(new TypeToken<List<String>>() {}, "storagebox", "pattern")
                .orElse(Collections.emptyList()));
        try {
            pattern = pattern.substring(0, rows * 9);
        } catch (StringIndexOutOfBoundsException ignored) {}

        menuPattern = new MenuPattern(pattern);
        countReward = countChar(pattern, '_');

        Map<Character, SimpleItemStack> map = menuConfig.getValue(
            new TypeToken<Map<Character, SimpleItemStack>>() {},
            "storagebox", "items").orElse(Collections.emptyMap());
        map.entrySet().stream()
            .filter(entry ->
                entry.getKey() != ' ' && entry.getKey() != '_' &&
                entry.getKey() != 'L' && entry.getKey() != 'R')
            .forEach(entry -> menuPattern.setItem(entry.getKey(),
                entry.getValue().createItemStackSnapShot()));

        List<ItemStackSnapshot> issList = Lists.newArrayListWithExpectedSize(4);
        String[][] nodePath = new String[][] {
            new String[] {"storagebox", "button", "left", "can"},
            new String[] {"storagebox", "button", "left", "cant"},
            new String[] {"storagebox", "button", "right", "can"},
            new String[] {"storagebox", "button", "right", "cant"}
        };
        for (String[] node : nodePath) {
            issList.add(menuConfig.getValue(
                    new TypeToken<SimpleItemStack>() {}, node)
                .orElse(SimpleItemStack.NONE)
                .createItemStackSnapShot());
        }
        menuPattern.setTurningButtons(issList);
    }

    private static int countChar(String str, char ch) {
        return Math.toIntExact(str.chars().filter(c -> c == ch).count());
    }

    public void update() {
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        List<Reward> rewards = playerDataManager.getPlayerData(uuid);

        int start = (page - 1) * countReward;
        int end = Math.min(page * countReward + 1, rewards.size());

        if (start > end) {
            return;
        }

        menuPattern.updateReward(this,
            rewards.subList(start, end)
                .stream()
                .map(reward -> reward.getViewItem().createStack())
                .collect(Collectors.toList()));

        menuPattern.updateTurningButton(this,
            this.page != 1,
            Math.ceil((double) rewards.size() / countReward) >= this.page + 1);
    }

    @Override
    protected void onClick(Player player, int slotIndex, Slot slot,
        ItemStackSnapshot clickedItem, ClickType clickType) {
        if (clickedItem.isEmpty()) {
            return;
        }
        String pattern = menuPattern.getPattern();
        char c = pattern.charAt(slotIndex);
        if (c != '_' && c != 'L' && c != 'R') {
            return;
        }

        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        List<Reward> rewards = playerDataManager.getPlayerData(uuid);

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
                List<ItemStack> items = new ArrayList<>();
                for (ItemStackSnapshot iss : itemReward.getRewardItems()) {
                    items.add(iss.createStack());
                }
                if (items.stream().allMatch(item ->
                    ((UserInventory<? extends User>) player.getInventory()).getMain().canFit(item))) {
                    for (ItemStack item : items) {
                        player.getInventory().offer(item);
                    }
                } else {
                    MessageStorage messageStorage = RewardAPI.getInstance().getMessageStorage();
                    player.sendMessage(messageStorage.getMessage("menu", "out_of_space"));
                    return;
                }
            } else {
                CommandReward commandReward = (CommandReward) reward;
                for (String command : commandReward.getCommands()) {
                    Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
                }
            }
            rewards.remove(rewardIndex);
            // TODO 이펙트
        } else {
            return;
        }
        Sponge.getScheduler().createTaskBuilder()
            .execute(this::update)
            .submit(RewardAPI.getInstance());
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
}

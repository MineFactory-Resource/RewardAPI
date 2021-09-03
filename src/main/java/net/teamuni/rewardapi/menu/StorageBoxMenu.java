package net.teamuni.rewardapi.menu;

import com.google.common.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;

public class StorageBoxMenu extends Menu {

    private static MenuPattern menuPattern;
    private static String title;
    private static int rows;
    private final UUID uuid;
    private int page = 0;

    public static void init() {
        ConfigManager menuConfig = RewardAPI.getInstance().getMenuConfig();

        title = menuConfig.getString("", "storage_box", "title");
        rows = menuConfig.getValue(Integer.class, 6, "storage_box", "rows");

        List<String> pattern = menuConfig.getValue(new TypeToken<List<String>>() {}, "storage_box", "pattern").orElse(Collections.emptyList());
        menuPattern = new MenuPattern().setPattern(pattern.toArray(new String[0]));

        Map<Character, SimpleItemStack> map2 = menuConfig.getValue(new TypeToken<Map<Character, SimpleItemStack>>() {}, "storage_box", "items").orElse(Collections.emptyMap());
        map2.forEach((key, value) -> menuPattern.setItem(key, value.createItemStack().createSnapshot()));
    }

    public StorageBoxMenu(UUID uuid) {
        super(title, rows); //TODO Config에서 변경 가능하게
        this.uuid = uuid;
        applyPattern(menuPattern);
        update();
    }

    public void update() {
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        List<Reward> rewards = playerDataManager.getPlayerData(uuid);
        for (int i = 0; i < 36; i++) {
            setItem(i + 9, i < rewards.size() ? rewards.get(i).getViewItem().createStack()
                : ItemStack.empty());
        }

    }


    @Override
    protected void onClick(Player player, int slotIndex, Slot slot, ClickType clickType) {
        if (slotIndex < 9 || slotIndex > 43) {
            return;
        }
        PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
        List<Reward> rewards = playerDataManager.getPlayerData(uuid);
        if (slotIndex - 9 >= rewards.size()) {
            return;
        }
        Reward reward = rewards.get(slotIndex - 9);
        if (reward.isItemReward()) {
            ItemReward itemReward = (ItemReward) reward;
            List<ItemStack> items = new ArrayList<>();
            for (ItemStackSnapshot iss : itemReward.getRewardItems()) {
                items.add(iss.createStack());
            }
            if (items.stream().allMatch((item) -> player.getInventory().canFit(item))) {
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
        rewards.remove(slotIndex - 9);
        // TODO 이펙트
        Sponge.getScheduler().createTaskBuilder().execute(this::update).submit(RewardAPI.getInstance());
    }
}

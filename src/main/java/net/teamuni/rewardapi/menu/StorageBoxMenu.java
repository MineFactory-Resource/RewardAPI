package net.teamuni.rewardapi.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.teamuni.rewardapi.RewardAPI;
import net.teamuni.rewardapi.api.CommandReward;
import net.teamuni.rewardapi.api.ItemReward;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.data.PlayerDataManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;

public class StorageBoxMenu extends Menu {

    private static final MenuPattern menuPattern;
    private final UUID uuid;

    static {
        // TODO Config에서 변경 가능하게
        menuPattern = new MenuPattern()
            .setItem('A', ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, DyeColors.PURPLE)
                .build().createSnapshot())
            .setItem('B', ItemTypes.SIGN.getTemplate())
            .setPattern(
                "AAAABAAAA",
                "_________",
                "_________",
                "_________",
                "_________",
                "AAAAAAAAA"
            );
    }

    public StorageBoxMenu(UUID uuid) {
        super("&6보관함", 6); //TODO Config에서 변경 가능하게
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
        Reward reward = rewards.remove(slotIndex - 9);
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
                player.sendMessage(Text.of("공간 부족")); //TODO Config에서 변경 가능하게
            }
        } else {
            CommandReward commandReward = (CommandReward) reward;
            for (String command : commandReward.getCommands()) {
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
            }
        }
        // TODO 이펙트
        Sponge.getScheduler().createTaskBuilder().execute(this::update).submit(RewardAPI.getInstance());
    }
}

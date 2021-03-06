package net.teamuni.rewardapi.data.object;

import org.bukkit.inventory.ItemStack;

public class CommandReward extends Reward {

    private String[] commands;

    public CommandReward(ItemStack viewItem, String[] commands) {
        super(viewItem);
        this.commands = commands;
    }

    public String[] getCommands() {
        return commands;
    }

    public void setCommands(String[] commands) {
        this.commands = commands;
    }
}

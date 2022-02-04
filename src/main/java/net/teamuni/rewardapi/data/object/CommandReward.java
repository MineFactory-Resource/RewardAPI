package net.teamuni.rewardapi.data.object;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class CommandReward extends Reward {

    private String[] commands;

    public CommandReward(ItemStackSnapshot viewItem, String[] commands) {
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

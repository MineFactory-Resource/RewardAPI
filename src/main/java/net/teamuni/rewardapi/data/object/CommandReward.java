package net.teamuni.rewardapi.data.object;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    @Override
    public boolean claim(Player player) {
        for (String command : this.commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        return true;
    }
}

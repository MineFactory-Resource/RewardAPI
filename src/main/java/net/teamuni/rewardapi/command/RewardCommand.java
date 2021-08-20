package net.teamuni.rewardapi.command;

import net.teamuni.rewardapi.menu.StorageBoxMenu;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class RewardCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player p = (Player) src;
            //new StorageBoxMenu(p.getUniqueId()).open(p);
            // TODO
        }
        return CommandResult.success();
    }
}

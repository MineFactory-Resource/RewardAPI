package net.teamuni.rewardapi.command;

import java.util.Optional;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.api.StorageBoxAPI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public class AddCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // TODO
        if (src instanceof Player) {
            Player p = (Player) src;
            Optional<ItemStack> heldItem = p.getItemInHand(HandTypes.MAIN_HAND);
            if (heldItem.isPresent()) {
                StorageBoxAPI.getInstance().give(args.<Player>requireOne("player").getUniqueId(),
                    new CommandReward(heldItem.get().createSnapshot(),
                        new String[] {args.requireOne("command")}));
            }
        }
        return CommandResult.success();
    }
}

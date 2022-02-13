package net.teamuni.rewardapi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.teamuni.rewardapi.api.StorageBoxAPI;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.menu.StorageBoxMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandManager implements CommandExecutor, TabCompleter {

    protected final Logger logger;

    public CommandManager(RewardAPI instance, ConfigManager config) {
        this.logger = instance.getLogger();
        PluginCommand command = instance.getCommand("rewardapi");
        if (command == null) {
            this.logger.log(Level.SEVERE, "커맨드를 불러오는데 실패하였습니다.");
            return;
        }
        CommandMap commandMap = null;
        try {
            commandMap = getCommandMap();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            this.logger.log(Level.SEVERE, "커맨드의 별칭을 지정하는데 실패하였습니다.", e);
        }
        if (commandMap != null) {
            command.unregister(commandMap);
            command.setAliases(config.getValueList(String.class, "aliases"));
            command.register(commandMap);
            commandMap.register(instance.getDescription().getName(), command);
        }
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private static CommandMap getCommandMap()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
        getCommandMap.setAccessible(true);
        return (CommandMap) getCommandMap.invoke(Bukkit.getServer());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
        @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0 || !player.hasPermission("rewardapi.admin")) {
                new StorageBoxMenu(player.getUniqueId()).open(player);
                return true;
            }
        }
        if (args.length > 0) {
            String arg = args[0].toLowerCase(Locale.ROOT);
            switch (arg) {
                case "give", "주기" -> {
                    if (args.length < 4) {
                        sendMessage(sender, "&6[&e-&6] &c사용법: &f/" + label + " " + arg + " [플레이어] [보일아이템] [보상아이템]");
                    } else {
                        OfflinePlayer targetName = Bukkit.getOfflinePlayerIfCached(args[1]);
                        if (targetName != null) {
                            // TODO Give
                            StorageBoxAPI.getInstance().give(targetName.getUniqueId(),
                                new ItemReward(((Player) sender).getInventory().getItemInMainHand(),
                                    new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD)}));
                        } else {
                            sendMessage(sender, "&6[&e-&6] &c해당 플레이어는 서버에 접속한 기록이 없습니다.");
                        }
                    }
                    return true;
                }
            }
        }
        sendMessage(sender, "&6[&e-&6] &f/" + label + " &e- 보상을 받을 수 있는 보관함을 엽니다.");
        sendMessage(sender, "&6[&e-&6] &f/" + label + " 주기 [플레이어] [보일아이템] [보상아이템] &e- 플레이어에게 아이템 보상을 지급합니다.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
        @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}

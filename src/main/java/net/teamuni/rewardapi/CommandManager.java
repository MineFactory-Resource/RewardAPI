package net.teamuni.rewardapi;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.data.object.CommandReward;
import net.teamuni.rewardapi.data.object.Reward;
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
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandManager implements CommandExecutor, TabCompleter {

    protected final Logger logger;
    private final List<String> items = new ArrayList<>();

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

        for (Material type : Material.values()) {
            this.items.add(type.toString().toLowerCase(Locale.ROOT));
        }
        this.items.add("hand");
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
            if (arg.equalsIgnoreCase("give") || arg.equalsIgnoreCase("주기")) {
                if (args.length >= 3) {
                    String[] varArgs = parseGiveArgs(args);
                    if (Arrays.stream(varArgs).allMatch(Objects::nonNull) && !varArgs[3].isBlank()) {
                        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(varArgs[0]);
                        if (target != null) {
                            ItemStack view;
                            if (varArgs[1].equalsIgnoreCase("hand")) {
                                if (sender instanceof Player player) {
                                    view = player.getInventory().getItemInMainHand();
                                    if (view.getType().isAir()) {
                                        sendMessage(sender, "&9[ &fRewardAPI &9] &c아이템을 들고 있지 않습니다.");
                                        return true;
                                    }
                                } else {
                                    sendMessage(sender, "&9[ &fRewardAPI &9] &c" + sender.getName() + "은 손에 들고 있는 아이템을 인식할 수 없습니다.");
                                    return true;
                                }
                            } else {
                                Material type = Material.matchMaterial(varArgs[1]);
                                if (type != null) {
                                    view = new ItemStack(type, 1);
                                    if (!varArgs[2].isBlank()) {
                                        applyNBT(view, varArgs[2]);
                                    }
                                } else {
                                    sendMessage(sender, "&9[ &fRewardAPI &9] &c'" + varArgs[1] + "' 은(는) 아이템이 아닙니다.");
                                    return true;
                                }
                            }
                            String playerName = target.getName();
                            if (playerName == null) {
                                playerName = varArgs[0];
                            }
                            varArgs[3] = varArgs[3].replaceAll("%p", playerName);
                            Reward reward = new CommandReward(view, new String[] { varArgs[3] } );
                            PlayerDataManager playerDataManager = RewardAPI.getInstance().getPlayerDataManager();
                            playerDataManager.usePlayerData(target.getUniqueId(), (data) -> data.addReward(reward));
                            sendMessage(sender, "&9[ &fRewardAPI &9] &f해당 플레이어에게 보상을 지급하였습니다.");
                        } else {
                            sendMessage(sender, "&9[ &fRewardAPI &9] &c해당 플레이어는 서버에 접속한 기록이 없습니다.");
                        }
                        return true;
                    }
                }
                sendMessage(sender, "&9[ &fRewardAPI &9] &c사용법: &f/" + label + " " + arg + " [플레이어] [보일아이템] [커맨드]");
                return true;
            }
        }

        sendMessage(sender, "&9[ &fRewardAPI &9] &f/" + label + " &7- 보상을 받을 수 있는 보관함을 엽니다.");
        sendMessage(sender, "&9[ &fRewardAPI &9] &f/" + label + " 주기 [플레이어] [보일아이템] [커맨드]");
        sendMessage(sender, "&9[ &fRewardAPI &9] &7 - 플레이어에게 커맨드 보상을 지급합니다.");
        sendMessage(sender, "&9[ &fRewardAPI &9] &7 - [보일아이템]에 'hand'를 입력 시 손에 들고 있는 아이템을 지정합니다.");
        sendMessage(sender, "&9[ &fRewardAPI &9] &7 - 커맨드에서 '%p' 는 플레이어 이름으로 치환됩니다.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
        @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return sender.hasPermission("rewardapi.admin") ? switch (args.length) {
            case 1 -> Collections.singletonList("주기");
            case 2 -> null;
            case 3 -> StringUtil.copyPartialMatches(args[args.length - 1], this.items, Lists.newArrayList());
            default -> Collections.emptyList();
        } : Collections.emptyList();
    }

    private static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private static String[] parseGiveArgs(String[] args) {
        args = Arrays.copyOfRange(args, 1, args.length);
        String[] varArgs = new String[4];
        int varIndex = 0;
        int countBrace = 0;
        boolean isAlreadyAddName = false;
        List<String> nbt = new ArrayList<>();
        for (final String s : args) {
            if (varIndex == 0) {
                varArgs[0] = s;
                varIndex++;
            } else if (varIndex == 1) {
                String item = s;
                if (!isAlreadyAddName) {
                    if (!s.contains("{")) {
                        varArgs[1] = s;
                        varArgs[2] = "";
                        varIndex++;
                        continue;
                    } else {
                        varArgs[1] = s.substring(0, s.indexOf('{'));
                        item = s.substring(s.indexOf('{'));
                    }
                    isAlreadyAddName = true;
                }
                nbt.add(item);
                countBrace += CharMatcher.is('{').countIn(s);
                countBrace -= CharMatcher.is('}').countIn(s);
                if (countBrace == 0) {
                    varArgs[2] = String.join(" ", nbt);
                    varIndex++;
                }
            } else if (varIndex == 2) {
                if (varArgs[3] == null) {
                    varArgs[3] = s;
                    if (varArgs[3].startsWith("/")) {
                        varArgs[3] = varArgs[3].substring(1);
                    }
                } else {
                    varArgs[3] = varArgs[3] + " " + s;
                }
            }
        }
        if (Objects.equals(varArgs[3], "")) {
            varArgs[3] = null;
        }
        return varArgs;
    }

    @SuppressWarnings("deprecation")
    private void applyNBT(ItemStack item, String data) {
        Bukkit.getUnsafe().modifyItemStack(item, data);
    }
}

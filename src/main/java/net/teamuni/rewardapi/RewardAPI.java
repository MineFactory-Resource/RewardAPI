package net.teamuni.rewardapi;

import java.nio.file.Paths;
import java.sql.SQLException;
import net.teamuni.rewardapi.command.AddCommand;
import net.teamuni.rewardapi.command.RewardCommand;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.config.MessageStorage;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.data.database.Database;
import net.teamuni.rewardapi.data.database.JsonDatabase;
import net.teamuni.rewardapi.data.database.SQLDatabase;
import net.teamuni.rewardapi.menu.Menu.InventoryEventListener;
import net.teamuni.rewardapi.menu.StorageBoxMenu;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RewardAPI extends JavaPlugin {

    private static RewardAPI instance;

    private Database database;
    private PlayerDataManager playerDataManager;

    private ConfigManager config;
    private ConfigManager menuConfig;
    private ConfigManager databaseConfig;
    private MessageStorage messageStorage;

    public static RewardAPI getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.config = new ConfigManager();
        this.menuConfig = new ConfigManager(this.config, "Menu");
        this.databaseConfig = new ConfigManager(this.config, "Database");
        this.messageStorage = new MessageStorage(this.config, "Message");

        Bukkit.getPluginManager().registerEvents(new InventoryEventListener(), this);
        StorageBoxMenu.init();

        CommandSpec addCommandSpec = CommandSpec.builder()
            .executor(new AddCommand())
            .arguments(
                GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                // TODO Item 인수
                GenericArguments.remainingJoinedStrings(Text.of("command"))
            )
            .build();
        CommandSpec rewardCommandSpec = CommandSpec.builder()
            .child(addCommandSpec, "add").executor(new RewardCommand())
            .build();

        if (this.databaseConfig.getString("json", "type").equalsIgnoreCase("json")) {
            this.database = new JsonDatabase(instance, Paths.get(
                this.databaseConfig.getString("./plugins/RewardAPI/data", "Json.datafolder")));
        } else {
            String host = this.databaseConfig.getString("", "MySQL.host");
            int port = this.databaseConfig.getValue(Integer.TYPE, 3306, "MySQL.port");
            String database = this.databaseConfig.getString("", "MySQL.database");
            String tableName = this.databaseConfig.getString("", "MySQL.tablename");
            String parameters = this.databaseConfig.getString("", "MySQL.parameters");
            String userName = this.databaseConfig.getString("", "MySQL.username");
            String password = this.databaseConfig.getString("", "MySQL.password");
            try {
                this.database = new SQLDatabase(this, host, port, database, tableName, parameters, userName, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        int saveInterval = this.databaseConfig.getValue(Integer.TYPE, 300, "save-interval");
        this.playerDataManager = new PlayerDataManager(this, saveInterval);
        Bukkit.getPluginManager().registerEvents(this.playerDataManager, this);
        Sponge.getCommandManager().register(this, rewardCommandSpec, "rewardapi", "reward");
    }

    @Override
    public void onDisable() {
        playerDataManager.close();
    }


    public Database getDatabase() {
        return database;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ConfigManager getMenuConfig() {
        return menuConfig;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

}

package net.teamuni.rewardapi;

import java.nio.file.Paths;
import java.sql.SQLException;
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

    private ConfigManager menuConfig;
    private MessageStorage messageStorage;

    public static RewardAPI getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigManager config = new ConfigManager();
        this.menuConfig = new ConfigManager(config, "Menu");
        this.messageStorage = new MessageStorage(config, "Message");

        Bukkit.getPluginManager().registerEvents(new InventoryEventListener(), this);
        StorageBoxMenu.init();

        ConfigManager databaseConfig = new ConfigManager(config, "Database");
        if (databaseConfig.getString("json", "type").equalsIgnoreCase("json")) {
            this.database = new JsonDatabase(instance, Paths.get(
                databaseConfig.getString("./plugins/RewardAPI/data", "Json.datafolder")));
        } else {
            String host = databaseConfig.getString("", "MySQL.host");
            int port = databaseConfig.getValue(Integer.TYPE, 3306, "MySQL.port");
            String database = databaseConfig.getString("", "MySQL.database");
            String tableName = databaseConfig.getString("", "MySQL.tablename");
            String parameters = databaseConfig.getString("", "MySQL.parameters");
            String userName = databaseConfig.getString("", "MySQL.username");
            String password = databaseConfig.getString("", "MySQL.password");
            try {
                this.database = new SQLDatabase(this, host, port, database, tableName, parameters, userName, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        int saveInterval = databaseConfig.getValue(Integer.TYPE, 300, "save-interval");
        this.playerDataManager = new PlayerDataManager(this, saveInterval);
        Bukkit.getPluginManager().registerEvents(this.playerDataManager, this);

        new CommandManager(this, new ConfigManager(config, "Command"));
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.close();
        }
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

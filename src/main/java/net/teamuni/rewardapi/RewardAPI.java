package net.teamuni.rewardapi;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.command.AddCommand;
import net.teamuni.rewardapi.command.RewardCommand;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.config.MessageStorage;
import net.teamuni.rewardapi.config.SimpleItemStack;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.database.Database;
import net.teamuni.rewardapi.database.JsonDatabase;
import net.teamuni.rewardapi.database.SQLDatabase;
import net.teamuni.rewardapi.menu.StorageBoxMenu;
import net.teamuni.rewardapi.serializer.RewardSerializer;
import net.teamuni.rewardapi.serializer.SimpleItemSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(
    id = "rewardapi",
    name = "RewardAPI",
    description = "RewardAPI pluigin"
)
public class RewardAPI {

    private static RewardAPI instance;
    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = true)
    private Path configDir;
    private Database database;
    private PlayerDataManager playerDataManager;

    private ConfigManager config;
    private ConfigManager menuConfig;
    private ConfigManager databaseConfig;
    private MessageStorage messageStorage;

    public static RewardAPI getInstance() {
        return instance;
    }

    @Listener
    public void onServerStart(GameInitializationEvent event) {
        instance = this;

        TypeSerializerCollection.defaults()
            .register(TypeToken.of(SimpleItemStack.class), new SimpleItemSerializer())
            .register(TypeToken.of(Reward.class), new RewardSerializer());

        this.config = new ConfigManager("rewardapi.conf");
        this.menuConfig = new ConfigManager(this.config, "menu");
        this.databaseConfig = new ConfigManager(this.config, "database");
        this.messageStorage = new MessageStorage(this.config, "message");

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
                this.databaseConfig.getString("rewardapi/data", "Json", "datafolder")));
        } else {
            String host = this.databaseConfig.getString("", "MySQL", "host");
            int port = this.databaseConfig.getValue(Integer.TYPE, 3306, "MySQL", "port");
            String database = this.databaseConfig.getString("", "MySQL", "database");
            String tableName = this.databaseConfig.getString("", "MySQL", "tablename");
            String parameters = this.databaseConfig.getString("", "MySQL", "parameters");
            String userName = this.databaseConfig.getString("", "MySQL", "username");
            String password = this.databaseConfig.getString("", "MySQL", "password");
            try {
                this.database = new SQLDatabase(this, host, port, database, tableName, parameters, userName, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.playerDataManager = new PlayerDataManager(this);
        Sponge.getEventManager().registerListeners(this, this.playerDataManager);
        Sponge.getCommandManager().register(this, rewardCommandSpec, "rewardapi", "reward");
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        playerDataManager.unloadAllData();
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public Database getDatabase() {
        return database;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public ConfigManager getMenuConfig() {
        return menuConfig;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }

}

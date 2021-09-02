package net.teamuni.rewardapi;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.command.AddCommand;
import net.teamuni.rewardapi.command.RewardCommand;
import net.teamuni.rewardapi.config.ConfigManager;
import net.teamuni.rewardapi.config.MessageStorage;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.database.Database;
import net.teamuni.rewardapi.database.YamlDatabase;
import net.teamuni.rewardapi.serializer.ItemSerializer;
import net.teamuni.rewardapi.serializer.RewardSerializer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
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
    private PluginContainer plugin;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    private ConfigManager configManager;
    private MessageStorage messageStorage;
    private Database database;
    private PlayerDataManager playerDataManager;

    @Listener
    public void onServerStart(GameInitializationEvent event) {
        instance = this;

        TypeSerializerCollection.defaults()
            .register(TypeToken.of(ItemStackSnapshot.class), new ItemSerializer())
            .register(TypeToken.of(Reward.class), new RewardSerializer());


        this.configManager = new ConfigManager(configDir, "config.conf", this.logger);
        this.messageStorage = new MessageStorage(configDir, this.logger);

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

        this.database = new YamlDatabase(instance, Paths.get("rewardapi"));
        this.playerDataManager = new PlayerDataManager(this);
        Sponge.getEventManager().registerListeners(this, this.playerDataManager);
        Sponge.getCommandManager().register(plugin, rewardCommandSpec, "rewardapi", "reward");
    }

    public static RewardAPI getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Database getDatabase() {
        return database;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageStorage getMessageStorage() {
        return messageStorage;
    }
}

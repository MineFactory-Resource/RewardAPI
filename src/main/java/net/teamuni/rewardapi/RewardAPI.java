package net.teamuni.rewardapi;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.command.AddCommand;
import net.teamuni.rewardapi.command.RewardCommand;
import net.teamuni.rewardapi.data.PlayerDataManager;
import net.teamuni.rewardapi.database.Database;
import net.teamuni.rewardapi.database.YamlDatabase;
import net.teamuni.rewardapi.serializer.ItemSerializer;
import net.teamuni.rewardapi.serializer.RewardSerializer;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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
    private final Path dataFolder = Paths.get("rewardapi");;
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer plugin;
    private ConfigurationOptions configOptions;
    private Database database;
    private PlayerDataManager playerDataManager;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        this.configOptions = ConfigurationOptions.defaults().withSerializers(TypeSerializerCollection.defaults().newChild()
            .register(TypeToken.of(ItemStackSnapshot.class), new ItemSerializer())
            .register(TypeToken.of(Reward.class), new RewardSerializer()));
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
        this.database = new YamlDatabase(instance, dataFolder);
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

    public Path getDataFolder() {
        return dataFolder;
    }

    public ConfigurationOptions getConfigOptions() {
        return configOptions;
    }

    public Database getDatabase() {
        return database;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}

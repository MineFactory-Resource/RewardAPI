package net.teamuni.rewardapi;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import java.nio.file.Path;
import net.teamuni.rewardapi.api.Reward;
import net.teamuni.rewardapi.command.AddCommand;
import net.teamuni.rewardapi.command.RewardCommand;
import net.teamuni.rewardapi.util.RewardSerialization;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
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
    private Path dataFolder;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        TypeSerializerCollection.defaults().register(TypeToken.of(Reward.class), new RewardSerialization());
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
}

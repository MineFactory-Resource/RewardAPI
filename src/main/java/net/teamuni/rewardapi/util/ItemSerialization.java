package net.teamuni.rewardapi.util;

import java.io.IOException;
import java.util.Optional;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ItemSerialization {
    public static ConfigurationNode serialize(ItemStackSnapshot item) throws IOException {
        final DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;
        DataContainer dataContainer = item.toContainer();
        return translator.translate(dataContainer);
    }

    public static Optional<ItemStackSnapshot> deserialize(ConfigurationNode node) {
        final DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;
        return Sponge.getDataManager().deserialize(ItemStackSnapshot.class, translator.translate(node));
    }
}

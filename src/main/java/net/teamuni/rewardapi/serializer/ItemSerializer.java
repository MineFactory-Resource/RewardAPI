package net.teamuni.rewardapi.serializer;

import com.google.common.reflect.TypeToken;
import java.util.Optional;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ItemSerializer implements TypeSerializer<ItemStackSnapshot> {
    public static ConfigurationNode serialize(@NonNull ItemStackSnapshot item) {
        final DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;
        DataContainer dataContainer = item.toContainer();
        return translator.translate(dataContainer);
    }

    public static Optional<ItemStackSnapshot> deserialize(@NonNull ConfigurationNode node) {
        final DataTranslator<ConfigurationNode> translator = DataTranslators.CONFIGURATION_NODE;
        return Sponge.getDataManager().deserialize(ItemStackSnapshot.class, translator.translate(node));
    }

    @Override
    public @Nullable ItemStackSnapshot deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        return deserialize(value).orElse(null);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable ItemStackSnapshot obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }
        value.setValue(serialize(obj));
    }
}

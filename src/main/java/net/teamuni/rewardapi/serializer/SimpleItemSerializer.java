package net.teamuni.rewardapi.serializer;

import com.google.common.reflect.TypeToken;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.teamuni.rewardapi.config.SimpleItemStack;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SimpleItemSerializer implements TypeSerializer<SimpleItemStack> {

    @Override
    public @Nullable SimpleItemStack deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (value.getNode("type").isVirtual()) {
            return null;
        }

        ItemType itemType = value.getNode("type").getValue(TypeToken.of(ItemType.class));
        if (itemType == null) {
            return null;
        }

        int data = value.getNode("data").getInt(0);
        String name = value.getNode("name").getString();
        List<String> lores = value.getNode("lore").getList(TypeToken.of(String.class), Collections.emptyList());
        return new SimpleItemStack(itemType, data, name, lores);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable SimpleItemStack obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }

        value.getNode("type").setValue(TypeToken.of(ItemType.class), obj.getType());
        if (obj.getData() != 0) {
            value.getNode("data").setValue(obj.getData());
        }
        if (!obj.getName().isEmpty()) {
            value.getNode("name").setValue(obj.getNameString());
        }
        if (!obj.getLores().isEmpty()) {
            value.getNode("lore").setValue(obj.getLoresString());
        }
    }
}

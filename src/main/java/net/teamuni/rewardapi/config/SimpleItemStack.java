package net.teamuni.rewardapi.config;

import java.util.List;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SimpleItemStack {

    @NonNull
    private final ItemType type;
    private final int data;
    @Nullable
    private final Text name;
    @NonNull
    private final List<Text> lores;

    public SimpleItemStack(@NonNull ItemType type, int data, @Nullable String name, @NonNull List<String> lores) {
        this.type = type;
        this.data = data;
        this.name = name != null ? TextSerializers.FORMATTING_CODE.deserialize(name) : null;
        this.lores = lores.stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList());
    }

    public ItemStack createItemStack() {
        ItemStack.Builder builder = ItemStack.builder().itemType(this.type);

        if (data != 0) {
            builder.fromContainer(DataContainer.createNew().set(DataQuery.of("UnsafeDamage"), data));
        }
        if (this.name != null) {
            builder.add(Keys.DISPLAY_NAME, this.name);
        }
        if (!this.lores.isEmpty()) {
            builder.add(Keys.ITEM_LORE, this.lores);
        }
        return builder.build();
    }


    @NonNull
    public ItemType getType() {
        return type;
    }

    public int getData() {
        return data;
    }

    @Nullable
    public Text getName() {
        return name;
    }

    @Nullable
    public String getNameString() {
        return name != null ? TextSerializers.FORMATTING_CODE.serialize(name) : null;
    }


    @NonNull
    public List<Text> getLores() {
        return lores;
    }

    @NonNull
    public List<String> getLoresString() {
        return lores.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList());
    }
}

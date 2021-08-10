package net.teamuni.rewardapi.util;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ItemSerialization {
    public static void serialize(ItemStackSnapshot item) {
        DataContainer dataContainer = item.toContainer();
        // TODO DataFormats.NBT.writeTo();
    }
}

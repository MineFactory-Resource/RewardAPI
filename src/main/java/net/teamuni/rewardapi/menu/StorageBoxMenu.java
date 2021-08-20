package net.teamuni.rewardapi.menu;

import java.util.UUID;
import net.teamuni.rewardapi.api.StorageBox;
import org.spongepowered.api.item.inventory.Slot;

public class StorageBoxMenu extends Menu {

    private final UUID uuid;
    private final StorageBox storageBox;

    public StorageBoxMenu(UUID uuid) {
        super("&6보관함", 6);
        this.uuid = uuid;
        this.storageBox = StorageBox.getStorageBox(uuid);
    }

    @Override
    protected void onClick(int slotIndex, Slot slot, ClickType clickType) {
        // TODO
    }
}

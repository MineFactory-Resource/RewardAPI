package net.teamuni.rewardapi.menu;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.teamuni.rewardapi.RewardAPI;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.AbstractInventoryProperty;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.text.serializer.TextSerializers;

public abstract class Menu {

    private static final Set<Menu> menus = Collections.newSetFromMap(new WeakHashMap<Menu, Boolean>());

    private final Inventory inv;

    public Menu(@NonNull String title, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows parameter must be between 1 and 6.");
        }
        menus.add(this);
        this.inv = Inventory.builder()
            .of(InventoryArchetypes.DOUBLE_CHEST)
            .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(title)))
            .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, rows))
            .listener(ClickInventoryEvent.class, this::onClick)
            .build(RewardAPI.getInstance());
    }

    public void setItem(int slot, @NonNull ItemStack item) {
        inv.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(slot))).set(item);
    }

    public void applyPattern(@NonNull MenuPattern p) {
        p.apply(this);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    private void onClick(ClickInventoryEvent event) {
        if (event.getTransactions().isEmpty()) {
            return;
        }
        SlotTransaction st = event.getTransactions().get(0);
        int slot = st.getSlot().getInventoryProperty(SlotIndex.class).map(AbstractInventoryProperty::getValue).orElse(-1);
        if (slot < 0 || slot >= inv.capacity()) {
            return;
        }
        event.setCancelled(true);
    }

    protected abstract void onClick(int slotIndex, Slot slot, ClickType clickType);
}

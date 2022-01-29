package net.teamuni.rewardapi.menu;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.WeakHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class Menu {

    private static final WeakHashMap<Player, Menu> menus = new WeakHashMap<>();

    protected final Inventory inv;

    protected Menu(@NonNull String title, int rows) {
        checkArgument(rows >= 1 && rows <= 6, "Rows parameter must be between 1 and 6.", rows);
        this.inv = Bukkit.createInventory(null, rows * 9, Component.text(title));
    }

    public void setItem(int slot, @Nullable ItemStack item) {
        inv.setItem(slot, item);
    }

    public void applyPattern(@NonNull MenuPattern p) {
        p.apply(this);
    }

    public void open(Player player) {
        menus.put(player, this);
        player.openInventory(inv);
    }

    private void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) {
            return;
        }
        event.setCancelled(true);
        onClick((Player) event.getWhoClicked(), event.getSlot(), event.getCurrentItem(), event.getAction());
    }

    protected abstract void onClick(Player player, int slotIndex, ItemStack clickItem, InventoryAction clickType);

    public static class InventoryEventListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            if (menus.containsKey(player)) {
                Menu menu = menus.get(player);
                if (menu.inv.equals(event.getClickedInventory())) {
                    menu.onClick(event);
                }
            }
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            menus.remove((Player) event.getPlayer());
        }
    }
}

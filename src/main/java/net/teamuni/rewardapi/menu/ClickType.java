package net.teamuni.rewardapi.menu;

import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

public enum ClickType {
    DOUBLE,
    MIDDLE_CLICK,
    LEFT_CLICK,
    RIGHT_CLICK,
    SHIFT_LEFT_CLICK,
    SHIFT_RIGHT_CLICK,
    UNKNOWN;

    public static ClickType fromEvent(ClickInventoryEvent e) {
        if (e instanceof ClickInventoryEvent.Double) {
            return ClickType.DOUBLE;
        } else if (e instanceof ClickInventoryEvent.Middle) {
            return ClickType.MIDDLE_CLICK;
        } else if (e instanceof ClickInventoryEvent.Shift.Primary) {
            return ClickType.SHIFT_LEFT_CLICK;
        } else if (e instanceof ClickInventoryEvent.Shift.Secondary) {
            return ClickType.SHIFT_RIGHT_CLICK;
        } else if (e instanceof ClickInventoryEvent.Primary) {
            return ClickType.LEFT_CLICK;
        } else if (e instanceof ClickInventoryEvent.Secondary) {
            return ClickType.RIGHT_CLICK;
        } else {
            return ClickType.UNKNOWN;
        }
    }

    public boolean isLeftClick() {
        switch (this) {
            case DOUBLE:
            case LEFT_CLICK:
            case SHIFT_LEFT_CLICK:
                return true;
            default:
                return false;
        }
    }

    public boolean isRightClick() {
        switch (this) {
            case RIGHT_CLICK:
            case SHIFT_RIGHT_CLICK:
                return true;
            default:
                return false;
        }
    }

    public boolean isShift() {
        switch (this) {
            case SHIFT_LEFT_CLICK:
            case SHIFT_RIGHT_CLICK:
                return true;
            default:
                return false;
        }
    }
}

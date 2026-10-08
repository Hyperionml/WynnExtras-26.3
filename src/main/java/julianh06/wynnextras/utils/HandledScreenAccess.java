package julianh06.wynnextras.utils;

import julianh06.wynnextras.mixin.Accessor.HandledScreenAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import java.lang.reflect.Field;

public final class HandledScreenAccess {
    private HandledScreenAccess() {}

    public static int x(AbstractContainerScreen<?> screen) {
        if (screen instanceof HandledScreenAccessor accessor) return accessor.getX();
        return getInt(screen, 0, "leftPos", "x", "field_2776", "z");
    }

    public static int y(AbstractContainerScreen<?> screen) {
        if (screen instanceof HandledScreenAccessor accessor) return accessor.getY();
        return getInt(screen, 0, "topPos", "y", "field_2800", "A");
    }

    public static int backgroundWidth(AbstractContainerScreen<?> screen) {
        if (screen instanceof HandledScreenAccessor accessor) return accessor.getImageWidth();
        return getInt(screen, 176, "imageWidth", "backgroundWidth", "field_2792", "d");
    }

    public static int backgroundHeight(AbstractContainerScreen<?> screen) {
        if (screen instanceof HandledScreenAccessor accessor) return accessor.getImageHeight();
        return getInt(screen, 166, "imageHeight", "backgroundHeight", "field_2779", "e");
    }

    public static Slot focusedSlot(AbstractContainerScreen<?> screen) {
        if (screen instanceof HandledScreenAccessor accessor) return accessor.getFocusedSlot();
        Object value = get(screen, Slot.class, "hoveredSlot", "focusedSlot", "field_2787", "y");
        return value instanceof Slot slot ? slot : null;
    }

    public static void setFocusedSlot(AbstractContainerScreen<?> screen, Slot slot) {
        if (screen instanceof HandledScreenAccessor accessor) {
            accessor.setFocusedSlot(slot);
            return;
        }
        set(screen, Slot.class, slot, "hoveredSlot", "focusedSlot", "field_2787", "y");
    }

    private static int getInt(AbstractContainerScreen<?> screen, int fallback, String... names) {
        Object value = get(screen, int.class, names);
        return value instanceof Integer i ? i : fallback;
    }

    private static Object get(AbstractContainerScreen<?> screen, Class<?> type, String... names) {
        Field field = findField(screen.getClass(), type, names);
        if (field == null) return null;
        try {
            field.setAccessible(true);
            return field.get(screen);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static void set(AbstractContainerScreen<?> screen, Class<?> type, Object value, String... names) {
        Field field = findField(screen.getClass(), type, names);
        if (field == null) return;
        try {
            field.setAccessible(true);
            field.set(screen, value);
        } catch (IllegalAccessException ignored) {}
    }

    private static Field findField(Class<?> start, Class<?> type, String... names) {
        for (Class<?> cls = start; cls != null; cls = cls.getSuperclass()) {
            for (String name : names) {
                try {
                    Field field = cls.getDeclaredField(name);
                    if (field.getType() == type) return field;
                } catch (NoSuchFieldException ignored) {}
            }
        }
        return null;
    }
}
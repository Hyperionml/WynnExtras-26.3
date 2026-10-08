package julianh06.wynnextras.utils;

import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.mixin.Accessor.BossBarHudAccessor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class BossBarUtils {
    private static Field bossBarsField;
    private static boolean accessFailureLogged = false;

    public static Collection<LerpingBossEvent> getBossBars(BossHealthOverlay hud) {
        if (hud == null) return Collections.emptyList();

        if (hud instanceof BossBarHudAccessor accessor) {
            Map<UUID, LerpingBossEvent> bars = accessor.getBossBars();
            return bars == null ? Collections.emptyList() : bars.values();
        }

        Map<UUID, LerpingBossEvent> bars = getBossBarsReflective(hud);
        return bars == null ? Collections.emptyList() : bars.values();
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, LerpingBossEvent> getBossBarsReflective(BossHealthOverlay hud) {
        try {
            Field field = bossBarsField;
            if (field == null) {
                field = findBossBarsField(hud.getClass());
                if (field == null) {
                    logAccessFailure(null);
                    return null;
                }
                field.setAccessible(true);
                bossBarsField = field;
            }

            Object value = field.get(hud);
            if (value instanceof Map<?, ?> map) return (Map<UUID, LerpingBossEvent>) map;
        } catch (ReflectiveOperationException | RuntimeException e) {
            logAccessFailure(e);
        }

        return null;
    }

    private static Field findBossBarsField(Class<?> clazz) {
        Field field = findFieldByName(clazz, "bossBars");
        if (isMapField(field)) return field;

        field = findFieldByName(clazz, "field_2060");
        if (isMapField(field)) return field;

        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            for (Field candidate : current.getDeclaredFields()) {
                if (isMapField(candidate)) return candidate;
            }
        }

        return null;
    }

    private static Field findFieldByName(Class<?> clazz, String name) {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static boolean isMapField(Field field) {
        return field != null && Map.class.isAssignableFrom(field.getType());
    }

    private static void logAccessFailure(Throwable throwable) {
        if (accessFailureLogged) return;
        accessFailureLogged = true;

        if (throwable == null) {
            WynnExtras.LOGGER.warn("[WynnExtras] Could not find BossBarHud bossBars field.");
        } else {
            WynnExtras.LOGGER.warn("[WynnExtras] Could not access BossBarHud bossBars field.", throwable);
        }
    }
}

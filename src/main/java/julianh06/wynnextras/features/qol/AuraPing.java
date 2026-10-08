package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.compat.client.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.Window;
import java.lang.reflect.Field;

public class AuraPing {
    private static final int AURA_PROC_TIME_MS = 3200;
    private static long firstAura = 0;
    private static long lastAura = 0;
    private static Field subtitleField = null;

    public static void register() {
        HudRenderCallback.EVENT.register(AuraPing::render);
    }

    private static void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!WynnExtrasConfig.INSTANCE.auraPingEnabled) return;

        checkSubtitle();
        long now = System.currentTimeMillis();
        if (now - firstAura >= AURA_PROC_TIME_MS) return;

        long remaining = AURA_PROC_TIME_MS - (now - firstAura);
        String remText = String.format("%.1f", remaining / 1000.0);

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        ctx.pose().pushMatrix();
        ctx.pose().scale(6.0f, 6.0f);
        int tx = window.getGuiScaledWidth() / 12 - mc.font.width(remText) / 3;
        int ty = window.getGuiScaledHeight() / 12 - 10;
        ctx.text(mc.font, Component.nullToEmpty(remText), tx, ty, 0xFF00FFFF);
        ctx.pose().popMatrix();

        if (now - firstAura < 400) {
            int color = parseColor(WynnExtrasConfig.INSTANCE.auraPingColor);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int flashColor = (50 << 24) | (r << 16) | (g << 8) | b;
            ctx.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), flashColor);
        }
    }

    private static int parseColor(String hex) {
        try {
            return Integer.parseInt(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFF6F00;
        }
    }

    private static void checkSubtitle() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (subtitleField == null) {
                for (Field f : mc.gui.hud.getClass().getDeclaredFields()) {
                    if (f.getName().equals("subtitle") || f.getName().equals("field_2039")) {
                        f.setAccessible(true);
                        subtitleField = f;
                        break;
                    }
                }
            }
            if (subtitleField == null) return;
            Component subtitle = (Component) subtitleField.get(mc.gui.hud);
            if (subtitle == null) return;
            if (subtitle.getString().contains("Aura")) {
                auraPinged();
            }
        } catch (Exception ignored) {}
    }

    private static void auraPinged() {
        long now = System.currentTimeMillis();
        if (now - lastAura > AURA_PROC_TIME_MS) {
            firstAura = now;
        }
        lastAura = now;
    }

    public static void reset() {
        firstAura = 0;
    }
}

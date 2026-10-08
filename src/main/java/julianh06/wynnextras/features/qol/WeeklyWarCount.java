package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.utils.BossBarUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import julianh06.wynnextras.compat.client.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;

public class WeeklyWarCount {
    private static final long WEEK_MS = 604_800_000L;
    private static final long WAR_COOLDOWN_MS = 25_000L;
    private static long lastWarDetected = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!WynnExtrasConfig.INSTANCE.weeklyWarCountEnabled) return;
            if (client.player == null) return;
            pruneOldWars();
            checkBossBars(client);
        });
        HudRenderCallback.EVENT.register(WeeklyWarCount::render);
    }

    private static void pruneOldWars() {
        long cutoff = System.currentTimeMillis() - WEEK_MS;
        WynnExtrasConfig.INSTANCE.weeklyWars.removeIf(t -> t < cutoff);
    }

    private static void checkBossBars(Minecraft client) {
        for (LerpingBossEvent bar : BossBarUtils.getBossBars(client.gui.hud.getBossOverlay())) {
            String name = bar.getName().getString();
            if (name == null) continue;
            String clean = name.replaceAll("§[0-9a-fk-or]", "");
            if (clean.contains("Tower")) {
                long now = System.currentTimeMillis();
                if (now - lastWarDetected > WAR_COOLDOWN_MS) {
                    WynnExtrasConfig.INSTANCE.weeklyWars.add(now);
                    WynnExtrasConfig.save();
                }
                lastWarDetected = now;
                return;
            }
        }
    }

    private static void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!WynnExtrasConfig.INSTANCE.weeklyWarCountEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) return;

        int count = WynnExtrasConfig.INSTANCE.weeklyWars.size();
        String text = count + " war" + (count == 1 ? "" : "s");

        float scale = 1.5f;
        int x = WynnExtrasConfig.INSTANCE.weeklyWarCountX;
        int y = WynnExtrasConfig.INSTANCE.weeklyWarCountY;

        ctx.pose().pushMatrix();
        ctx.pose().translate(x, y);
        ctx.pose().scale(scale, scale);

        Integer override = WynnExtrasConfig.INSTANCE.hudColorOverrides.get("weeklyWars");
        int color = override != null ? (override | 0xFF000000) : 0xFFFF55FF;
        ctx.text(mc.font, text, 0, 0, color);

        ctx.pose().popMatrix();
    }
}

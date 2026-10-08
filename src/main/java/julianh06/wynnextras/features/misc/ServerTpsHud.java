package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.config.WynnExtrasConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import julianh06.wynnextras.compat.client.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.Locale;

public class ServerTpsHud {
    private static final double NANOS_PER_TWENTY_TICKS = 20_000_000_000d;
    private static volatile long previousTimeUpdate;
    private static volatile float serverTps = 20f;

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        HudRenderCallback.EVENT.register(ServerTpsHud::render);
    }

    public static void onWorldTimeUpdate() {
        long now = System.nanoTime();
        long previous = previousTimeUpdate;
        previousTimeUpdate = now;
        if (previous == 0L) return;

        long elapsed = now - previous;
        if (elapsed > 0L) {
            serverTps = (float) Math.clamp(NANOS_PER_TWENTY_TICKS / elapsed, 0d, 20d);
        }
    }

    private static void reset() {
        previousTimeUpdate = 0L;
        serverTps = 20f;
    }

    private static void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        WynnExtrasConfig config = WynnExtrasConfig.INSTANCE;
        if (!config.serverTpsEnabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) return;

        String text = String.format(Locale.ROOT, "TPS: %.1f", serverTps);
        Integer override = config.hudColorOverrides.get("serverTps");
        int color = override != null ? override | 0xFF000000 : 0xFFFFFFFF;

        ctx.pose().pushMatrix();
        ctx.pose().translate(config.serverTpsX, config.serverTpsY);
        ctx.pose().scale(config.serverTpsScale, config.serverTpsScale);
        ctx.text(client.font, text, 0, 0, color);
        ctx.pose().popMatrix();
    }
}

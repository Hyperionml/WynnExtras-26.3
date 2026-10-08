package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.compat.client.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsHud {
    public static void register() {
        HudRenderCallback.EVENT.register(FpsHud::render);
    }

    private static void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        WynnExtrasConfig config = WynnExtrasConfig.INSTANCE;
        if (!config.fpsHudEnabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) return;

        String text = "FPS: " + client.getFps();
        Integer override = config.hudColorOverrides.get("fps");
        int color = override != null ? override | 0xFF000000 : 0xFFFFFFFF;

        ctx.pose().pushMatrix();
        ctx.pose().translate(config.fpsHudX, config.fpsHudY);
        ctx.pose().scale(config.fpsHudScale, config.fpsHudScale);
        ctx.text(client.font, text, 0, 0, color);
        ctx.pose().popMatrix();
    }
}

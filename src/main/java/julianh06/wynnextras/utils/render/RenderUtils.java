package julianh06.wynnextras.utils.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import julianh06.wynnextras.utils.colors.CustomColor;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class RenderUtils {
    private RenderUtils() {}

    public static void drawRect(GuiGraphicsExtractor context, CustomColor color, float x, float y, float width, float height) {
        context.fill(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), color.asInt());
    }

    public static void drawRectBorders(GuiGraphicsExtractor context, CustomColor color, float x, float y,
                                       float width, float height, float thickness) {
        drawRect(context, color, x, y, width, thickness);
        drawRect(context, color, x, y + height - thickness, width, thickness);
        drawRect(context, color, x, y, thickness, height);
        drawRect(context, color, x + width - thickness, y, thickness, height);
    }

    public static void drawLine(GuiGraphicsExtractor context, CustomColor color, float x1, float y1,
                                float x2, float y2, float thickness) {
        if (Math.abs(x2 - x1) >= Math.abs(y2 - y1)) {
            drawRect(context, color, Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1) + thickness, thickness);
        } else {
            drawRect(context, color, Math.min(x1, x2), Math.min(y1, y2), thickness, Math.abs(y2 - y1) + thickness);
        }
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Identifier texture, CustomColor color,
                                        float x, float y, float width, float height, int textureWidth, int textureHeight) {
        drawTexturedRect(context, texture, color, x, y, width, height, 0, 0,
                textureWidth, textureHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Identifier texture, CustomColor color,
                                        float x, float y, float width, float height, float u, float v,
                                        float uWidth, float vHeight, int textureWidth, int textureHeight) {
        drawTexturedRect(context, RenderPipelines.GUI_TEXTURED, texture, color, x, y, width, height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Identifier texture,
                                        float x, float y, float width, float height, float u, float v,
                                        float uWidth, float vHeight, int textureWidth, int textureHeight) {
        drawTexturedRect(context, texture, CustomColor.NONE, x, y, width, height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, RenderPipeline pipeline, Identifier texture,
                                        CustomColor color, float x, float y, float width, float height, float u, float v,
                                        float uWidth, float vHeight, int textureWidth, int textureHeight) {
        context.blit(pipeline, texture, Math.round(x), Math.round(y), u, v, Math.round(width),
                Math.round(height), Math.round(uWidth), Math.round(vHeight), textureWidth, textureHeight, color.asInt());
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Object texture, CustomColor color,
                                        float x, float y, float width, float height, float u, float v,
                                        float uWidth, float vHeight, int textureWidth, int textureHeight) {
        Identifier identifier = identifier(texture);
        if (identifier != null) drawTexturedRect(context, identifier, color, x, y, width, height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Object texture,
                                        float x, float y, float width, float height, float u, float v,
                                        float uWidth, float vHeight, int textureWidth, int textureHeight) {
        drawTexturedRect(context, texture, CustomColor.NONE, x, y, width, height,
                u, v, uWidth, vHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Object texture, CustomColor color,
                                        float x, float y, float width, float height, int textureWidth, int textureHeight) {
        drawTexturedRect(context, texture, color, x, y, width, height, 0, 0,
                textureWidth, textureHeight, textureWidth, textureHeight);
    }

    public static void drawTexturedRect(GuiGraphicsExtractor context, Object texture, float x, float y) {
        Identifier identifier = identifier(texture);
        int width = dimension(texture, "width");
        int height = dimension(texture, "height");
        if (identifier != null) drawTexturedRect(context, identifier, CustomColor.NONE, x, y, width, height, width, height);
    }

    public static void drawScalingTexturedRect(GuiGraphicsExtractor context, Object texture, CustomColor color,
                                               float x, float y, float width, float height) {
        int textureWidth = dimension(texture, "width");
        int textureHeight = dimension(texture, "height");
        drawTexturedRect(context, texture, color, x, y, width, height, textureWidth, textureHeight);
    }

    public static void drawScalingTexturedRect(GuiGraphicsExtractor context, Identifier texture,
                                               float x, float y, float width, float height, int textureWidth, int textureHeight) {
        drawTexturedRect(context, texture, CustomColor.NONE, x, y, width, height, textureWidth, textureHeight);
    }

    public static void drawSprite(GuiGraphicsExtractor context, Object texture, CustomColor color,
                                  float x, float y, float width, float height) {
        drawScalingTexturedRect(context, texture, color, x, y, width, height);
    }

    public static void drawSprite(GuiGraphicsExtractor context, Object texture, CustomColor color, float x, float y) {
        drawScalingTexturedRect(context, texture, color, x, y, dimension(texture, "width"), dimension(texture, "height"));
    }

    public static void drawArc(GuiGraphicsExtractor context, CustomColor color, float x, float y,
                               float fraction, int innerRadius, int outerRadius) {
        int segments = Math.max(1, Math.round(Math.clamp(fraction, 0, 1) * 32));
        for (int i = 0; i < segments; i++) {
            double angle = Math.PI * 2 * i / 32 - Math.PI / 2;
            drawRect(context, color, (float) (x + outerRadius + Math.cos(angle) * innerRadius),
                    (float) (y + outerRadius + Math.sin(angle) * innerRadius), 1, 1);
        }
    }

    private static Identifier identifier(Object texture) {
        if (texture instanceof Identifier identifier) return identifier;
        if (texture instanceof Texture && !FabricLoader.getInstance().isModLoaded("wynntils")) return null;
        Object value = invoke(texture, "identifier");
        return value instanceof Identifier identifier ? identifier : null;
    }

    private static int dimension(Object texture, String method) {
        Object value = invoke(texture, method);
        return value instanceof Number number ? Math.max(1, number.intValue()) : 16;
    }

    private static Object invoke(Object target, String method) {
        if (target == null) return null;
        try {
            Method resolved = target.getClass().getMethod(method);
            return resolved.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }
}

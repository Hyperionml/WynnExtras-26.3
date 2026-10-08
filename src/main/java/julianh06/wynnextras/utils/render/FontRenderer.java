package julianh06.wynnextras.utils.render;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.text.StyledText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();

    private FontRenderer() {}

    public static FontRenderer getInstance() {
        return INSTANCE;
    }

    public Font getFont() {
        return MinecraftUtils.mc().font;
    }

    public void renderText(GuiGraphicsExtractor context, StyledText text, float x, float y, CustomColor color,
                           HorizontalAlignment horizontal, VerticalAlignment vertical, TextShadow shadow, float scale) {
        Font renderer = getFont();
        float width = renderer.width(text.getComponent()) * scale;
        float height = renderer.lineHeight * scale;
        if (horizontal == HorizontalAlignment.CENTER) x -= width / 2;
        else if (horizontal == HorizontalAlignment.RIGHT) x -= width;
        if (vertical == VerticalAlignment.MIDDLE) y -= height / 2;
        else if (vertical == VerticalAlignment.BOTTOM) y -= height;

        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.text(renderer, text.getComponent(), 0, 0, color.asInt(), shadow != TextShadow.NONE);
        context.pose().popMatrix();
    }

    public void renderText(GuiGraphicsExtractor context, StyledText text, float x, float y, CustomColor color,
                           HorizontalAlignment horizontal, VerticalAlignment vertical, TextShadow shadow) {
        renderText(context, text, x, y, color, horizontal, vertical, shadow, 1);
    }

    public void renderAlignedTextInBox(GuiGraphicsExtractor context, StyledText text, float left, float right,
                                       float top, float bottom, float padding, CustomColor color,
                                       HorizontalAlignment horizontal, VerticalAlignment vertical, TextShadow shadow) {
        float x = horizontal == HorizontalAlignment.LEFT ? left + padding
                : horizontal == HorizontalAlignment.RIGHT ? right - padding : (left + right) / 2;
        float y = vertical == VerticalAlignment.TOP ? top + padding
                : vertical == VerticalAlignment.BOTTOM ? bottom - padding : (top + bottom) / 2;
        renderText(context, text, x, y, color, horizontal, vertical, shadow, 1);
    }
}

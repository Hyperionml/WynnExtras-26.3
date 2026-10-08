package julianh06.wynnextras.config.configoptions;

import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import static julianh06.wynnextras.config.ConfigTheme.*;

public abstract class ConfigOption {
    public final String name, desc;
    protected List<DescLine> richDesc = null;
    private BooleanSupplier visibilityCondition = () -> true;

    protected ConfigOption(String name, String desc) { this.name = name; this.desc = desc; }

    public abstract void render(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int mx, int my, boolean hovered, int categoryColor);
    public boolean mouseClicked(double mx, double my, int x, int y, int w, int h, int btn) { return false; }
    public boolean mouseReleased(double mx, double my, int btn) { return false; }
    public boolean mouseDragged(double mx, double my, int x, int y, int w, int h) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean charTyped(char chr, int modifiers) { return false; }

    public ConfigOption visibleWhen(BooleanSupplier condition) {
        this.visibilityCondition = condition;
        return this;
    }

    public ConfigOption withRichDesc(List<DescLine> lines) {
        this.richDesc = lines;
        return this;
    }

    public boolean isVisible() {
        return visibilityCondition.getAsBoolean();
    }

    public int controlWidth() { return 0; }

    public int getHeight(int contentW) {
        var tr = Minecraft.getInstance().font;
        int textW = Math.max(20, contentW - 16 - controlWidth());
        int nameLines = tr.split(Component.literal(name), textW).size();
        if (richDesc != null && !richDesc.isEmpty()) {
            int richH = calcRichDescHeight(tr, richDesc, textW);
            return 8 + nameLines * 10 + 4 + richH + 13;
        }
        int descLineCount = (desc == null || desc.isEmpty()) ? 0 : tr.split(Component.literal(desc), textW).size();
        int extraLines = (nameLines - 1) + Math.max(0, descLineCount - 1);
        return OPTION_HEIGHT + extraLines * 10;
    }

    public static int calcRichDescHeight(net.minecraft.client.gui.Font tr, List<DescLine> lines, int availW) {
        int total = 0;
        for (DescLine dl : lines) {
            var txt = Component.literal(dl.text).setStyle(Style.EMPTY
                .withBold(dl.bold).withItalic(dl.italic).withUnderlined(dl.underline));
            int wrapW = Math.max(1, (int)(availW / dl.scale));
            int numWrapped = Math.max(1, tr.split(txt, wrapW).size());
            total += (int)(10 * dl.scale) * numWrapped;
        }
        return total;
    }

    public static int drawWrappedTexts(GuiGraphicsExtractor ctx, int x, int y, int w, int controlW, String name, String desc, List<DescLine> richDesc, int nameColor, int descColor) {
        var tr = Minecraft.getInstance().font;
        int textW = Math.max(20, w - 16 - controlW);
        var nameLines = tr.split(Component.literal(name), textW);
        int ny = y + 8;
        for (var line : nameLines) {
            ctx.text(tr, line, x + 8, ny, nameColor);
            ny += 10;
        }
        int dy = ny + 4;
        if (richDesc != null && !richDesc.isEmpty()) {
            for (DescLine dl : richDesc) {
                dy += renderRichDescLine(ctx, dl, x + 8, dy, textW, descColor);
            }
        } else if (desc != null && !desc.isEmpty()) {
            var descLines = tr.split(Component.literal(desc), textW);
            for (var line : descLines) {
                ctx.text(tr, line, x + 8, dy, descColor);
                dy += 10;
            }
        }
        return dy;
    }

    public static int renderRichDescLine(GuiGraphicsExtractor ctx, DescLine dl, int x, int y, int availW, int color) {
        var tr = Minecraft.getInstance().font;
        var style = Style.EMPTY.withBold(dl.bold).withItalic(dl.italic).withUnderlined(dl.underline);
        var txt = Component.literal(dl.text).setStyle(style);
        float s = dl.scale;
        int lineH = Math.max(1, (int)(10 * s));
        int wrapW = Math.max(1, (int)(availW / s));
        int drawColor = dl.color != null ? dl.color.asInt() : color;
        var wrapped = tr.split(txt, wrapW);
        int curY = y;
        for (var wl : wrapped) {
            int textPxW = (int)(tr.width(wl) * s);
            int drawX = switch (dl.align) {
                case LEFT   -> x;
                case CENTER -> x + (availW - textPxW) / 2;
                case RIGHT  -> x + availW - textPxW;
            };
            if (s != 1.0f) {
                ctx.pose().pushMatrix();
                ctx.pose().translate(drawX, curY);
                ctx.pose().scale(s, s);
                ctx.text(tr, wl, 0, 0, drawColor);
                ctx.pose().popMatrix();
            } else {
                ctx.text(tr, wl, drawX, curY, drawColor);
            }
            curY += lineH;
        }
        return curY - y;
    }
}

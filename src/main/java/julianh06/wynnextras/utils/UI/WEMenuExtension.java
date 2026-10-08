package julianh06.wynnextras.utils.UI;

import julianh06.wynnextras.utils.HandledScreenAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for overlays that extend (rather than replace) a vanilla HandledScreen.
 * Renders proportionally alongside the vanilla GUI — scales naturally with GUI scale.
 * Use WEHandledScreen for full overlays that replace the vanilla GUI.
 */
public abstract class WEMenuExtension {
    protected GuiGraphicsExtractor drawContext;
    protected double scaleFactor;
    protected int xStart;
    protected int yStart;
    protected int screenWidth;
    protected int screenHeight;
    protected UIUtils ui;

    protected final List<Widget> rootWidgets = new ArrayList<>();
    protected Widget focusedWidget = null;

    public void render(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        this.drawContext = ctx;
        computeScale();

        if (ui == null)
            ui = new UIUtils(ctx, scaleFactor, xStart, yStart);
        else
            ui.updateContext(ctx, scaleFactor, xStart, yStart);

        drawBackground(ctx, mouseX, mouseY, delta);
        drawContent(ctx, mouseX, mouseY, delta);

        for (Widget w : rootWidgets) {
            w.draw(ctx, mouseX, mouseY, delta, ui);
        }

        drawForeground(ctx, mouseX, mouseY, delta);
    }

    public void computeScale() {
        Window w = Minecraft.getInstance().getWindow();
        // scaleFactor = 1.0 so that logical units == GUI units.
        // GUI unit values from the HandledScreen (getX, getBackgroundWidth, etc.) can
        // be used directly in setBounds without conversion, and automatically scale
        // proportionally with the physical pixel size as GUI scale changes.
        this.scaleFactor  = 1.0;
        this.screenWidth  = w.getGuiScaledWidth();
        this.screenHeight = w.getGuiScaledHeight();
    }

    protected float hsX(AbstractContainerScreen<?> screen) {
        return HandledScreenAccess.x(screen) * (float) scaleFactor;
    }

    protected float hsY(AbstractContainerScreen<?> screen) {
        return HandledScreenAccess.y(screen) * (float) scaleFactor;
    }

    protected float hsWidth(AbstractContainerScreen<?> screen) {
        return HandledScreenAccess.backgroundWidth(screen) * (float) scaleFactor;
    }

    protected float hsHeight(AbstractContainerScreen<?> screen) {
        return HandledScreenAccess.backgroundHeight(screen) * (float) scaleFactor;
    }

    public boolean mouseClicked(double x, double y, int button) {
        clearUiFocus();
        for (int i = rootWidgets.size() - 1; i >= 0; i--) {
            if (rootWidgets.get(i).mouseClicked(x, y, button)) {
                setFocusedWidget(rootWidgets.get(i));
                return true;
            }
        }
        setFocusedWidget(null);
        return false;
    }

    public boolean mouseReleased(double x, double y, int button) {
        for (Widget w : rootWidgets)
            if (w.mouseReleased(x, y, button)) return true;
        return false;
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (focusedWidget != null && focusedWidget.mouseDragged(x, y, button, dx, dy)) return true;
        for (Widget w : rootWidgets)
            if (w.mouseDragged(x, y, button, dx, dy)) return true;
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focusedWidget != null && focusedWidget.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (Widget w : rootWidgets)
            if (w.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    public boolean charTyped(char chr, int mods) {
        if (focusedWidget != null && focusedWidget.charTyped(chr, mods)) return true;
        for (Widget w : rootWidgets)
            if (w.charTyped(chr, mods)) return true;
        return false;
    }

    protected void setFocusedWidget(Widget w) {
        if (focusedWidget != null) focusedWidget.setFocused(false);
        focusedWidget = w;
        if (w != null) w.setFocused(true);
    }

    protected void clearUiFocus() {
        for (Widget w : rootWidgets) {
            w.clearFocusTree();
        }
        focusedWidget = null;
    }

    protected abstract void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta);
    protected abstract void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta);
    protected abstract void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta);
}

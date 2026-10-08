package julianh06.wynnextras.utils.UI;

import julianh06.wynnextras.utils.text.StyledText;
import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.render.FontRenderer;
import julianh06.wynnextras.utils.render.RenderUtils;
import julianh06.wynnextras.utils.render.HorizontalAlignment;
import julianh06.wynnextras.utils.render.TextShadow;
import julianh06.wynnextras.utils.render.VerticalAlignment;
import julianh06.wynnextras.annotations.WEModule;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import com.mojang.blaze3d.platform.Window;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class WEScreen extends Screen {
    protected GuiGraphicsExtractor drawContext;
    protected double scaleFactor;
    protected double matrixScale = 1.0;
    protected int xStart;
    protected int yStart;
    protected int screenWidth;
    protected int screenHeight;
    protected UIUtils ui;

    protected double getTargetScaleFactor() { return -1; }
    protected int getMinLogicalWidth()  { return 0; }
    protected int getMinLogicalHeight() { return 0; }
    protected boolean shouldRenderBlur() { return true; }
    protected boolean shouldRenderBackground() { return true; }
    protected double actualScale = 1.0;

    public final List<Widget> rootWidgets = new ArrayList<>();
    protected Widget focusedWidget = null;

    private static long lastScrollTime = 0;
    private static final long scrollCooldown = 0; // in ms

    protected WEScreen(Component title) {
        super(title);
        screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    protected void registerScrolling() {
        ScreenMouseEvents.afterMouseScroll(this).register((
                screen,
                mX,
                mY,
                horizontalAmount,
                verticalAmount,
                consumed
        ) -> {
            long now = System.currentTimeMillis();
            if (isScrollOnCooldown(now)) {
                return true;
            }

            if (verticalAmount > 0) {
                scrollList(30); //Scroll up
            } else {
                scrollList(-30); //Scroll down
            }
            return true;
        });
    }

    private static boolean isScrollOnCooldown(long now) {
        if (now - lastScrollTime < scrollCooldown) {
            return true;
        }

        lastScrollTime = now;
        return false;
    }

    @Override
    public void clearFocus() {
        if (shouldRenderBlur()) super.clearFocus();
    }

    @Override
    public void extractBlurredBackground(GuiGraphicsExtractor context) {
        if (shouldRenderBlur()) super.extractBlurredBackground(context);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        if (shouldRenderBackground()) super.extractBackground(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void extractTransparentBackground(GuiGraphicsExtractor context) {
        if (shouldRenderBackground()) super.extractTransparentBackground(context);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        try {
            if (super.minecraft != null) extractBlurredBackground(context);
        } catch (Exception ignored) {}

        this.drawContext = context;
        computeScaleAndOffsets();
        if (ui == null) ui = new UIUtils(context, scaleFactor, xStart, yStart);
        else ui.updateContext(context, scaleFactor, xStart, yStart);

        int mx = (int)(mouseX / matrixScale);
        int my = (int)(mouseY / matrixScale);

        if (shouldRenderBackground()) ui.drawBackground();

        context.pose().pushMatrix();
        context.pose().scale((float) matrixScale, (float) matrixScale);
        updateValues();
        drawBackground(context, mx, my, delta);
        drawContent(context, mx, my, delta);

        for (Widget w : rootWidgets) {
            w.draw(context, mx, my, delta, ui);
        }

        drawForeground(context, mx, my, delta);

        context.pose().popMatrix();
    }

    protected void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) { /* override */ }
    protected abstract void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta);
    protected void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) { /* override */ }

    protected void updateWidgetBounds() {
        if (ui == null) return;

        for (Widget w : rootWidgets) {
            int sx = (int) (w.x);
            int sy = (int) (w.y * MinecraftUtils.guiScale());
            int sw = (int) (w.width * MinecraftUtils.guiScale());
            int sh = (int) (w.height * MinecraftUtils.guiScale());
            w.setBounds(sx, sy, sw, sh);
        }
    }

    protected void updateValues() {}

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        double mouseX = click.x() / matrixScale;
        double mouseY = click.y() / matrixScale;
        int button = julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button());

        clearUiFocus();

        for (int i = rootWidgets.size() - 1; i >= 0; i--) {
            Widget w = rootWidgets.get(i);
            if (w.mouseClicked(mouseX, mouseY, button)) {
                setFocusedWidget(w);
                return true;
            }
        }

        setFocusedWidget(null);
        return super.mouseClicked(click, doubleClick);
    }

    @Override public boolean mouseReleased(MouseButtonEvent click) {
        double mouseX = click.x() / matrixScale;
        double mouseY = click.y() / matrixScale;
        int button = julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button());

        for (int i = rootWidgets.size() - 1; i >= 0; i--) {
            if (rootWidgets.get(i).mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(click);
    }


    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        double mouseX = click.x() / matrixScale;
        double mouseY = click.y() / matrixScale;
        int button = julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button());

        if(ui == null) return false;
        if (focusedWidget != null && focusedWidget.mouseDragged(mouseX, mouseY, button, dx, dy)) return true;
        for (int i = rootWidgets.size() - 1; i >= 0; i--) {
            Widget w = rootWidgets.get(i);
            if (w.mouseDragged(mouseX, mouseY, button, dx, dy)) return true;
        }
        return super.mouseDragged(click, dx, dy);
    }


    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = julianh06.wynnextras.compat.client.InputCompat.legacyKey(input.key());
        int scanCode = input.keycode();
        int modifiers = julianh06.wynnextras.compat.client.InputCompat.legacyModifiers(input.modifiers());

        if (focusedWidget != null && focusedWidget.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (Widget w : rootWidgets) {
            if (w.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        char chr = (char) input.codepoint();
        int modifiers = 0;

        if (focusedWidget != null && focusedWidget.charTyped(chr, modifiers)) return true;
        for (Widget w : rootWidgets) {
            if (w.charTyped(chr, modifiers)) return true;
        }
        return super.charTyped(input);
    }

    protected void setFocusedWidget(Widget w) {
        if (focusedWidget == w) return;
        if (focusedWidget != null) focusedWidget.setFocused(false);
        focusedWidget = w;
        if (focusedWidget != null) focusedWidget.setFocused(true);
    }

    protected void clearUiFocus() {
        for (Widget w : rootWidgets) {
            w.clearFocusTree();
        }
        focusedWidget = null;
    }

    private double lastScaleFactor = -1;
    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    public void computeScaleAndOffsets() {
        Minecraft client = Minecraft.getInstance();
        Window w = client.getWindow();
        if (w == null) return;

        double actualScale = Math.max(1.0, w.getGuiScale());
        this.actualScale = actualScale;
        double target = getTargetScaleFactor();
        if (target > 1.0 && actualScale > target) {
            this.matrixScale = target / actualScale;
            this.scaleFactor = target;
        } else {
            this.matrixScale = 1.0;
            this.scaleFactor = actualScale;
        }

        int minW = getMinLogicalWidth();
        int minH = getMinLogicalHeight();
        if (minW > 0) matrixScale = Math.min(matrixScale, w.getGuiScaledWidth()  * scaleFactor / (double) minW);
        if (minH > 0) matrixScale = Math.min(matrixScale, w.getGuiScaledHeight() * scaleFactor / (double) minH);
        matrixScale = Math.min(1.0, matrixScale);
        this.screenWidth = (int) Math.round(w.getGuiScaledWidth() / matrixScale);
        this.screenHeight = (int) Math.round(w.getGuiScaledHeight() / matrixScale);

        this.xStart = 0;
        this.yStart = 0;

        if (ui != null) ui.updateContext(drawContext, scaleFactor, xStart, yStart);
    }

    protected void scrollList(float delta) {
    }

    protected void layoutVertical(List<? extends Widget> list, float startX, float startY, float itemHeight, float spacing, float scrollOffset) {
        float yy = startY - scrollOffset;
        for (Widget w : list) {
            w.setBounds((int) startX, (int) yy, (int) (getLogicalWidth()), (int) itemHeight);
            yy += itemHeight + spacing;
        }
    }

    protected int getLogicalWidth() {
        Window w = Minecraft.getInstance().getWindow();
        return (int) Math.round(w.getGuiScaledWidth() * scaleFactor / matrixScale);
    }

    protected int getLogicalHeight() {
        Window w = Minecraft.getInstance().getWindow();
        return (int) Math.round(w.getGuiScaledHeight() * scaleFactor / matrixScale);
    }

    public void addRootWidget(Widget w) {
        if (w == null) return;
        this.rootWidgets.add(w);
        if (ui != null) {
            try {
                w.getClass().getMethod("setUi", UIUtils.class).invoke(w, ui);
            } catch (Exception ignored) { }
        }
    }

    public void removeRootWidget(Widget w) {
        if (w == null) return;
        rootWidgets.remove(w);
        if (focusedWidget == w) setFocusedWidget(null);
    }

    @Override
    public void removed() {
        super.removed();
        rootWidgets.clear();
        focusedWidget = null;
    }

    protected void drawText(String text, float x, float y, CustomColor color,
                            HorizontalAlignment horizontalAlignment,
                            VerticalAlignment verticalAlignment,
                            TextShadow shadow, float textScale) {
        if (ui == null) return;
        ui.drawText(text, x, y, color, horizontalAlignment, verticalAlignment, shadow, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color,
                            HorizontalAlignment horizontalAlignment,
                            VerticalAlignment verticalAlignment, float textScale) {
        drawText(text, x, y, color, horizontalAlignment, verticalAlignment, TextShadow.NORMAL, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.LEFT, VerticalAlignment.TOP, TextShadow.NORMAL, textScale);
    }

    protected void drawText(String text, float x, float y, CustomColor color) {
        drawText(text, x, y, color, 3f);
    }

    protected void drawText(String text, float x, float y) {
        drawText(text, x, y, CustomColor.fromHexString("FFFFFF"));
    }

    protected void drawCenteredText(String text, float x, float y, CustomColor color, float textScale) {
        drawText(text, x, y, color, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE, TextShadow.NORMAL, textScale);
    }

    protected void drawCenteredText(String text, float x, float y, CustomColor color) {
        drawCenteredText(text, x, y, color, 3f);
    }

    protected void drawCenteredText(String text, float x, float y) {
        drawCenteredText(text, x, y, CustomColor.fromHexString("FFFFFF"));
    }

    protected void drawImage(net.minecraft.resources.Identifier texture, float x, float y, float width, float height) {
        if (ui == null) return;
        ui.drawImage(texture, x, y, width, height);
    }

    /**
     * Open a screen safely on the client thread.
     * Usage: WEScreen.open(() -> new MainScreen());
     */
    public static void open(Supplier<? extends WEScreen> screenSupplier) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        client.schedule(() -> {
            WEScreen current = null;
            if (client.gui.screen() instanceof WEScreen) current = (WEScreen) client.gui.screen();
            if (current != null && current.getClass() == screenSupplier.get().getClass()) {
                return;
            }
            client.gui.setScreen(screenSupplier.get());
        });
    }

    public void setDrawContext(GuiGraphicsExtractor drawContext) {
        this.drawContext = drawContext;
    }

    public void setUi(UIUtils ui) {
        this.ui = ui;
    }

    public UIUtils getUi() {
        return this.ui;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getMatrixScale() {
        return matrixScale;
    }

    public int getxStart() {
        return xStart;
    }

    public int getyStart() {
        return yStart;
    }
}

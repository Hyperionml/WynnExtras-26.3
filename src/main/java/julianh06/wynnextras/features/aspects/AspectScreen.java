package julianh06.wynnextras.features.aspects;

import julianh06.wynnextras.utils.text.StyledText;
import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.render.FontRenderer;
import julianh06.wynnextras.utils.render.HorizontalAlignment;
import julianh06.wynnextras.utils.render.TextShadow;
import julianh06.wynnextras.utils.render.VerticalAlignment;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.features.aspects.pages.*;
import julianh06.wynnextras.utils.UI.WEScreen;
import julianh06.wynnextras.utils.UI.Widget;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import java.util.ArrayList;
import java.util.List;

public class AspectScreen extends WEScreen {
    public enum Page {AspectLootpool, RaidItems, Lootruns, Aspects, Gambits, RaidLoot, Leaderboard}

    static LootPoolPage lootPoolPage;
    static RaidItemsPage raidItemsPage;
    static LootrunLootPoolPage lootrunLootPoolPage;
    static AspectsPage aspectsPage;
    static GambitsPage gambitsPage;
    static RaidLootPage raidLootPage;
    //static ExplorePage explorePage;
    static LeadboardPage leadboardPage;

    public static Page currentPage = Page.AspectLootpool;
    private static PageWidget currentWidget;
    private static long lastScrollTime = 0;
    private static final long scrollCooldown = 0; // in ms

    private List<PageSwitchButton> pageSwitchButtons = new ArrayList<>();
    private boolean registeredScroll = false;

    @Override protected double getTargetScaleFactor() { return 2.5; }
    @Override protected int getMinLogicalWidth()  { return 2000; }
    @Override protected int getMinLogicalHeight() { return 870; }

    public AspectScreen() {
        super(Component.nullToEmpty("WynnExtras Aspects"));

        for(Page page : Page.values()) {
            pageSwitchButtons.add(new PageSwitchButton(page));
        }
    }

    @Override
    protected void init() {
        super.init();

        registeredScroll = false;

//        // Initialize page instances
//        if (gambitsPage == null) {
//            gambitsPage = new GambitsPage(this);
//            raidLootPage = new RaidLootPage(this);
//            explorePage = new ExplorePage(this);
//            leaderboardPage = new LeaderboardPage(this);
//        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        double mx = click.x() / matrixScale;
        double my = click.y() / matrixScale;
        for(PageSwitchButton button : pageSwitchButtons) {
            if(button.mouseClicked(mx, my, julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button()))) return true;
        }

        if(currentWidget == null) return true;

        if(currentWidget.mouseClicked(mx, my, julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button()))) return true;

        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        double mx = click.x() / matrixScale;
        double my = click.y() / matrixScale;
        if(currentWidget != null) currentWidget.mouseReleased(mx, my, julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) {
        double mx = click.x() / matrixScale;
        double my = click.y() / matrixScale;
        if(currentWidget != null && currentWidget.mouseDragged(mx, my, julianh06.wynnextras.compat.client.InputCompat.legacyMouseButton(click.button()), dx, dy)) return true;
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if(currentWidget != null) currentWidget.keyPressed(julianh06.wynnextras.compat.client.InputCompat.legacyKey(input.key()), input.keycode(), julianh06.wynnextras.compat.client.InputCompat.legacyModifiers(input.modifiers()));
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {

        if(currentWidget != null) currentWidget.charTyped((char) input.codepoint(), 0);
        return super.charTyped(input);
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
        int centerX = getLogicalWidth() / 2;
        FontRenderer.getInstance().renderText(
                ctx,
                StyledText.fromComponent(WynnExtras.addWynnExtrasPrefix("")),
                ui.sx(centerX + 7),
                ui.sy(10),
                CustomColor.fromHexString("FFFFFF"),
                HorizontalAlignment.CENTER,
                VerticalAlignment.TOP,
                TextShadow.NORMAL,
                (float)(3f / scaleFactor)
        );


        int buttonAmount = pageSwitchButtons.size();
        int buttonWidth = 230;
        int buttonHeight = 50;
        int spacing = 20;
        int totalButtonWidth = (buttonWidth + spacing) * buttonAmount;

        int pw = (int) Math.round(getLogicalWidth() / scaleFactor);
        int ph = (int) Math.round(getLogicalHeight() / scaleFactor);

        int x = (int) (((pw * ui.getScaleFactorF()) - totalButtonWidth + spacing) / 2f);
        int y = (int) (ph * ui.getScaleFactorF() - buttonHeight - spacing);

        for(PageSwitchButton button : pageSwitchButtons) {
            button.setBounds(x, y, buttonWidth, buttonHeight);
            button.draw(ctx, mouseX, mouseY, tickDelta, ui);
            x += buttonWidth + spacing;
        }

        currentWidget = getTabWidget(currentPage);

        if(currentWidget == null) return;

        currentWidget.setBounds(0, 0, pw, ph);
        currentWidget.draw(ctx, mouseX, mouseY, tickDelta, ui);

        if(Minecraft.getInstance().gui.screen() == null || registeredScroll) return;
        ScreenMouseEvents.afterMouseScroll(Minecraft.getInstance().gui.screen()).register((
                screen,
                mX,
                mY,
                horizontalAmount,
                verticalAmount,
                consumed
        ) -> {
            long now = System.currentTimeMillis();
            if (now - lastScrollTime < scrollCooldown) {
                return true;
            }
            lastScrollTime = now;

            if(currentWidget == null) return false;

            return currentWidget.mouseScrolled(mX / matrixScale, mY / matrixScale, verticalAmount);
        });
        registeredScroll = true;
    }

    private PageWidget getTabWidget(Page page) {
        return switch (page) {
            case AspectLootpool -> lootPoolPage == null ? lootPoolPage = new LootPoolPage(this) : lootPoolPage;
            case RaidItems -> raidItemsPage == null ? raidItemsPage = new RaidItemsPage(this) : raidItemsPage;
            case Lootruns -> lootrunLootPoolPage == null ? lootrunLootPoolPage = new LootrunLootPoolPage(this) : lootrunLootPoolPage;
            case Aspects -> aspectsPage == null ? aspectsPage = new AspectsPage(this) : aspectsPage;
            case Gambits -> gambitsPage == null ? gambitsPage = new GambitsPage(this) : gambitsPage;
            case RaidLoot -> raidLootPage == null ? raidLootPage = new RaidLootPage(this) : raidLootPage;
            //case Explore -> explorePage == null ? explorePage = new ExplorePage(this) : explorePage;
            case Leaderboard -> leadboardPage == null ? leadboardPage = new LeadboardPage(this) : leadboardPage;
            case null, default -> null;
        };
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    private static class PageSwitchButton extends Widget {
        final Page page;

        public PageSwitchButton(Page page) {
            super(0, 0, 0, 0);
            this.page = page;
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            ui.drawButton(x, y, width, height, hovered || currentPage == page);
            String name = page.name();
            if(page == Page.AspectLootpool) name = "Aspects";
            if(page == Page.RaidItems) name = "Raids";
            if(page == Page.Lootruns) name = "Lootruns";
            if(page == Page.RaidLoot) name = "Raid Loot";
            if(page == Page.Aspects) name = "Player";
            ui.drawCenteredText(name, x + width / 2f, y + height / 2f, currentPage == page ? CustomColor.fromHexString("FFFF00") : CustomColor.fromHexString("FFFFFF"));
        }

        @Override
        protected boolean onClick(int button) {
            MinecraftUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
            currentPage = page;
            return true;
        }
    }
}

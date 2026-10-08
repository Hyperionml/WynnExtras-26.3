package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.wynncraft.menu.MenuType;
import julianh06.wynnextras.wynncraft.menu.WynncraftMenuService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.render.RenderUtils;
import julianh06.wynnextras.mixin.Accessor.HandledScreenAccessor;
import julianh06.wynnextras.utils.UI.WEMenuExtension;
import julianh06.wynnextras.utils.UI.WEScreen;
import julianh06.wynnextras.utils.UI.Widget;

public class IdentifierOverlay extends WEMenuExtension {
    SourceOfThruthOpenerWidget sourceOfThruthOpenerWidget = null;

    @Override
    protected void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (!WynncraftMenuService.isCurrent(MenuType.ITEM_IDENTIFIER)) return;
        if(Minecraft.getInstance().gui.screen() == null) return;

        if(sourceOfThruthOpenerWidget == null) {
            sourceOfThruthOpenerWidget = new SourceOfThruthOpenerWidget(0, 0, 0, 0);
            rootWidgets.add(sourceOfThruthOpenerWidget);
        }

        Screen screen = MinecraftUtils.screen();
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
        int yPos = ((HandledScreenAccessor) containerScreen).getY() - 35;

        sourceOfThruthOpenerWidget.setBounds(screenWidth / 2 - 67, yPos, 133, 20);
    }

    @Override
    protected void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {

    }

    private static class SourceOfThruthOpenerWidget extends Widget {
        public SourceOfThruthOpenerWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            ui.drawCenteredText("Should i keep gambling?", x + width / 2f, y - 8, 1f);
            ui.drawButton(x, y, width, height, hovered);
            ui.drawCenteredText("Find out here!", x + width / 2f, y + height / 2f, 1f);
        }

        @Override
        protected boolean onClick(int button) {
            MinecraftUtils.mc().gui.setScreen(null);
            WEScreen.open(SourceOfThruth::new);
            return true;
        }
    }
}
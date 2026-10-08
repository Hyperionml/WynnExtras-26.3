package julianh06.wynnextras.features.aspects;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.utils.UI.WEHandledScreen;
import julianh06.wynnextras.utils.UI.WEScreen;
import julianh06.wynnextras.utils.UI.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;

public class PartyFinderOpenLootpoolOverlay extends WEHandledScreen {
    LootPoolOpenButton lootPoolOpenButton = null;

    @Override
    protected void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if(Minecraft.getInstance().gui.screen() == null) return;
        if(Minecraft.getInstance().gui.screen().getTitle() == null) return;
        if(Minecraft.getInstance().gui.screen().getTitle().getString() == null) return;
        if(!(Minecraft.getInstance().gui.screen().getTitle().getString().equals("\uDAFF\uDFE4\uE03E") ||
            Minecraft.getInstance().gui.screen().getTitle().getString().equals("\uDAFF\uDFE4\uE03F") ||
            Minecraft.getInstance().gui.screen().getTitle().getString().equals("\uDAFF\uDFE1\uE00C"))) return;

        //" \uDAFF\uDFE4\uE03F"
        //" \uDAFF\uDFE1\uE00C" NOTG
        //WynnExtras.LOGGER.info(MinecraftClient.getInstance().currentScreen.getTitle().getString());

        if(lootPoolOpenButton == null) {
            lootPoolOpenButton = new LootPoolOpenButton(0, 0, 0, 0);
            rootWidgets.add(lootPoolOpenButton);
        }

        lootPoolOpenButton.setBounds(0, 0, 400, 60);

    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {

    }

    private static class LootPoolOpenButton extends Widget {
        public LootPoolOpenButton(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            ui.drawButton(x, y, width, height, hovered);
            ui.drawCenteredText("View Weekly Lootpools", x + width / 2f, y + height / 2f);
        }

        @Override
        protected boolean onClick(int button) {
            if(Minecraft.getInstance().gui.screen() != null) Minecraft.getInstance().gui.screen().onClose();
            WEScreen.open(AspectScreen::new);
            AspectScreen.currentPage = AspectScreen.Page.AspectLootpool;
            MinecraftUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
            return true;
        }
    }
}

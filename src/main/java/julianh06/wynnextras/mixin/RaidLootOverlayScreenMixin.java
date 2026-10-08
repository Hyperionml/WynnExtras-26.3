package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.crafting.CraftingResultPreviewer;
import julianh06.wynnextras.features.inventory.TradeMarketOverlay;
import julianh06.wynnextras.features.misc.GuildRaidBlockOverlay;
import julianh06.wynnextras.features.raid.RaidLootTrackerOverlay;
import julianh06.wynnextras.features.raid.tna.TreeRoomMinimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class RaidLootOverlayScreenMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderOverlayOnScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RaidLootTrackerOverlay.renderOnScreen(context, mouseX, mouseY);
        TradeMarketOverlay.renderOnScreen(context);
        CraftingResultPreviewer.onRender(context);
        GuildRaidBlockOverlay.render(context);

        boolean isInventory = Minecraft.getInstance().gui.screen() instanceof InventoryScreen;
        boolean isChat = Minecraft.getInstance().gui.screen() instanceof ChatScreen;
        if (!isInventory && !isChat) return;

        TreeRoomMinimap.render(context, null);
    }
}
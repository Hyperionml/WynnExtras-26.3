package julianh06.wynnextras.mixin;

import julianh06.wynnextras.wynncraft.menu.MenuType;
import julianh06.wynnextras.wynncraft.menu.WynncraftMenuService;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.inventory.BankOverlay;
import julianh06.wynnextras.features.inventory.BankOverlayType;
import julianh06.wynnextras.features.inventory.data.AccountBankData;
import julianh06.wynnextras.features.inventory.data.BookshelfData;
import julianh06.wynnextras.features.inventory.data.CharacterBankData;
import julianh06.wynnextras.features.inventory.data.MiscBucketData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class InventoryScreenMixin {
    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void hideReplacedContainerBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof julianh06.wynnextras.duck.ContainerBackgroundAccess access
                && access.wynnExtras$hideVanillaBackground()) ci.cancel();
    }

    @Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
    public void renderInGameBackground(GuiGraphicsExtractor context, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        AbstractContainerMenu currScreenHandler = MinecraftUtils.containerMenu();
        if (currScreenHandler == null) return;

        if(WynnExtrasConfig.INSTANCE.sourceOfTruthToggle) {
            if (WynncraftMenuService.isCurrent(MenuType.ITEM_IDENTIFIER)) {
                //ci.cancel();
            }
        }

        if (WynncraftMenuService.isCurrent(MenuType.CRAFTING_STATION) && WynnExtrasConfig.INSTANCE.craftingHelperOverlay && Minecraft.getInstance().options.guiScale().get() != 1) {
            //ci.cancel();
        }

        if(WynnExtrasConfig.INSTANCE.differentGUIScale) {
            if(!WynnExtras.hasStoredNormalGuiScale()) {
                WynnExtras.storeNormalGuiScale(Minecraft.getInstance().options.guiScale().get());
                Minecraft.getInstance().options.guiScale().set(WynnExtrasConfig.INSTANCE.customGUIScale);
            }
        }

        if(!WynnExtrasConfig.INSTANCE.toggleBankOverlay) {
            if (WynncraftMenuService.isCurrentAny(
                    MenuType.ACCOUNT_BANK, MenuType.CHARACTER_BANK, MenuType.BOOKSHELF, MenuType.MISC_BUCKET)) {
                BankOverlay.currentOverlayType = BankOverlayType.NONE;
                BankOverlay.currentData = null;
                //ci.cancel();
            }
            return;
        }

        BankOverlay.updateOverlayType();

        if(BankOverlay.expectedOverlayType == null) {
            BankOverlay.expectedOverlayType = BankOverlay.currentOverlayType;
        }

        if (BankOverlay.currentOverlayType != BankOverlayType.NONE) {
            //ci.cancel();

            Container playerInv = client.player.getInventory();
            BankOverlay.playerInvSlots.clear();
            BankOverlay.activeInvSlots.clear();

            for (Slot slot : currScreenHandler.slots) {
                if (slot.container == playerInv) {
                    BankOverlay.playerInvSlots.add(slot);
                } else {
                    BankOverlay.activeInvSlots.add(slot);
                }
            }

            WynnExtras.updateTestInventory(currScreenHandler.slots);
        }
    }
}

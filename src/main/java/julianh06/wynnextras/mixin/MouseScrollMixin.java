package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.bankoverlay.BankOverlay2;
import julianh06.wynnextras.features.bankoverlay.BankViewerScreen;
import julianh06.wynnextras.features.shoppinglist.ui.ShoppingListMenuExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lower-priority HEAD injections are applied later, placing this callback before
// Wynntils' default-priority callback, which can cancel container wheel events.
@Mixin(value = MouseHandler.class, priority = 500)
public class MouseScrollMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (window == 0 || window != client.getWindow().handle() || client.gui.overlay() != null) return;
        Screen screen = client.gui.screen();
        if (screen == null) return;

        if (ShoppingListMenuExtension.handleGlobalMouseScrolled(vertical)) {
            ci.cancel();
            return;
        }
        if ((screen instanceof AbstractContainerScreen<?> || screen instanceof BankViewerScreen)
                && BankOverlay2.handleMouseScrolled(vertical)) {
            ci.cancel();
        }
    }
}

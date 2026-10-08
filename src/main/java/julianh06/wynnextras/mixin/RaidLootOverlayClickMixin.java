package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.crafting.CraftingResultPreviewer;
import julianh06.wynnextras.features.inventory.TradeMarketOverlay;
import julianh06.wynnextras.features.raid.RaidLootTrackerOverlay;
import julianh06.wynnextras.features.raid.tna.TreeRoomMinimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import julianh06.wynnextras.compat.client.InputCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class RaidLootOverlayClickMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void onMouseClick(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        int mods = julianh06.wynnextras.compat.client.InputCompat.legacyModifiers(input.modifiers());
        int button = InputCompat.legacyMouseButton(input.button());

        MouseHandler mouse = (MouseHandler) (Object) this;
        double mouseX = mouse.xpos();
        double mouseY = mouse.ypos();

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() != null) {
            double scale = mc.getWindow().getGuiScale();
            mouseX = mouseX / scale;
            mouseY = mouseY / scale;
        }

        // Check if ctrl/shift is held
        boolean ctrlHeld = (mods & InputCompat.MOD_CONTROL) != 0;
        boolean shiftHeld = (mods & InputCompat.MOD_SHIFT) != 0;

        if (action == 1) {
            // On mousedown: short-circuit after first overlay claims the drag
            boolean dragClaimed = false;

            if (!dragClaimed) {
                boolean was = RaidLootTrackerOverlay.isDragging();
                RaidLootTrackerOverlay.handleClick(mouseX, mouseY, button, action, ctrlHeld, shiftHeld);
                if (!was && RaidLootTrackerOverlay.isDragging()) dragClaimed = true;
            }
            if (!dragClaimed) {
                boolean was = TradeMarketOverlay.isDragging();
                TradeMarketOverlay.handleClick(mouseX, mouseY, button, action);
                if (!was && TradeMarketOverlay.isDragging()) dragClaimed = true;
            }
            if (!dragClaimed) {
                boolean was = CraftingResultPreviewer.isDragging();
                CraftingResultPreviewer.handleClick(mouseX, mouseY, button, action);
                if (!was && CraftingResultPreviewer.isDragging()) dragClaimed = true;
            }
            if (!dragClaimed) {
                TreeRoomMinimap.handleClick(mouseX, mouseY, button, action, ctrlHeld, shiftHeld);
            }
        } else {
            // On mouseup: all overlays release drag independently
            RaidLootTrackerOverlay.handleClick(mouseX, mouseY, button, action, ctrlHeld, shiftHeld);
            TradeMarketOverlay.handleClick(mouseX, mouseY, button, action);
            CraftingResultPreviewer.handleClick(mouseX, mouseY, button, action);
            TreeRoomMinimap.handleClick(mouseX, mouseY, button, action, ctrlHeld, shiftHeld);
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMouseMove(long window, double x, double y, double deltaX, double deltaY, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() != null) {
            double scale = mc.getWindow().getGuiScale();
            x = x / scale;
            y = y / scale;
        }

        RaidLootTrackerOverlay.handleMouseMove(x, y);

        TradeMarketOverlay.handleMouseMove(x, y);

        CraftingResultPreviewer.handleMouseMove(x, y);

        TreeRoomMinimap.handleMouseMove(x, y);
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MouseHandler mouse = (MouseHandler) (Object) this;
        double mouseX = mouse.xpos();
        double mouseY = mouse.ypos();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() != null) {
            double scale = mc.getWindow().getGuiScale();
            mouseX = mouseX / scale;
            mouseY = mouseY / scale;
        }
        if (TreeRoomMinimap.handleScroll(mouseX, mouseY, vertical)) {
            ci.cancel();
        }
    }
}
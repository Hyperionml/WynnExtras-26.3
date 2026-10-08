package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.bankoverlay.BankOverlay2;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "handleContainerInput", at = @At("HEAD"), cancellable = true)
    private void blockBankOverlayScrollSlotClick(int syncId, int slotId, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (BankOverlay2.shouldBlockSlotClickAfterScroll(syncId)) {
            ci.cancel();
        }
    }
}
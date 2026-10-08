package julianh06.wynnextras.mixin;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.misc.FastRequeue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {
    @Inject(method = "clicked", at = @At("HEAD"))
    private void onMouseClick(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if(slotIndex == 4) {
            if(WynnExtrasConfig.INSTANCE.toggleFastRequeue) FastRequeue.notifyClick();
        }
    }
}
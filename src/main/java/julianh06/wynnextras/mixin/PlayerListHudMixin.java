package julianh06.wynnextras.mixin;

import julianh06.wynnextras.wynncraft.state.StatusEffectState;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {
    @Inject(method = "setFooter", at = @At("TAIL"))
    private void onSetFooter(Component footer, CallbackInfo ci) {
        StatusEffectState.update(footer);
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void onClear(CallbackInfo ci) {
        StatusEffectState.clear();
    }
}

package julianh06.wynnextras.mixin;


import julianh06.wynnextras.config.WynnExtrasConfig;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CameraType.class)
public class PerspectiveMixin {
    @Inject(method = "cycle", at = @At("HEAD"), cancellable = true)
    private void next(CallbackInfoReturnable<CameraType> cir) {
        if (!WynnExtrasConfig.INSTANCE.removeFrontPersonView) {
            return;
        }
        CameraType self = (CameraType)(Object)this;

        if (self == CameraType.THIRD_PERSON_BACK) {
            cir.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}

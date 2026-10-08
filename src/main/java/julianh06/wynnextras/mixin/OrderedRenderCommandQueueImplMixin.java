package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.badges.BadgeService;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SubmitNodeStorage.class)
public class OrderedRenderCommandQueueImplMixin {
    @ModifyArg(
            method = "submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollection;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V"),
            index = 3)
    private Component appendWynnExtrasBadge(Component label) {
        return BadgeService.appendBadge(null, label);
    }
}
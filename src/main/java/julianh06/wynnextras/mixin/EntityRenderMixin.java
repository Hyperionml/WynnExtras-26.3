package julianh06.wynnextras.mixin;


import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.badges.BadgeService;
import julianh06.wynnextras.duck.EntityRenderStateAccess;
import julianh06.wynnextras.utils.EntityVisibility;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRenderMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(T entity, Frustum frustum, double x, double y, double z, float tickDelta, CallbackInfoReturnable<Boolean> cir){
        if (EntityVisibility.isHidden(entity)) {
            cir.setReturnValue(false);
            return;
        }
        if(WynnExtrasConfig.INSTANCE.arrowHiderToggle && entity.getType().equals(net.minecraft.world.entity.EntityTypes.ARROW)){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void storeEntity(T entity, S state, float tickDelta, CallbackInfo ci) {
        ((EntityRenderStateAccess) state).wynnExtras$setEntity(entity);
    }

    @Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At("HEAD"))
    private void appendWynnExtrasBadge(S state, com.mojang.blaze3d.vertex.PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector renderQueue, net.minecraft.client.renderer.state.level.CameraRenderState camera, int offset, CallbackInfo ci) {
        state.nameTag = BadgeService.appendBadge(state, state.nameTag);
        if (state.nameTag == null) state.scoreText = BadgeService.appendBadge(state, state.scoreText);
    }
}

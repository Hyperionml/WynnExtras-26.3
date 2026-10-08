package julianh06.wynnextras.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import julianh06.wynnextras.duck.EntityRenderStateAccess;
import julianh06.wynnextras.features.misc.HuntedModeTracker;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.render.PlayerRenderFilter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements RenderLayerParent<S, M> {
    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void onRender(S state, PoseStack matrices, SubmitNodeCollector renderQueue, CameraRenderState camera, CallbackInfo ci) {
        Entity entity = ((EntityRenderStateAccess) state).wynnExtras$getEntity();
        if (entity instanceof Player player && PlayerRenderFilter.isHidden(player)) ci.cancel();
    }

    @ModifyExpressionValue(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity showOwnNameTag(Entity entity) {
        if(entity == Minecraft.getInstance().player && WynnExtrasConfig.INSTANCE.showOwnNametag) return null;

        return entity;
    }

    @Inject(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    private void beforeSuperRender(S state, PoseStack matrices, SubmitNodeCollector renderQueue, CameraRenderState camera, CallbackInfo ci) {
        if (!(state instanceof AvatarRenderState playerState)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || playerState.id != mc.player.getId()) return;
        if (!WynnExtrasConfig.INSTANCE.showOwnNametag) return;
        if (!HuntedModeTracker.huntedMode) return;

        Component pvpPrefix = Component.literal("[PvP] ").withStyle(ChatFormatting.RED);
        if (playerState.scoreText != null) {
            playerState.scoreText = pvpPrefix.copy().append(playerState.scoreText);
        }
        if (playerState.nameTag != null) {
            playerState.nameTag = pvpPrefix.copy().append(playerState.nameTag);
        }
    }
}
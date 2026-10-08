package julianh06.wynnextras.mixin;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.duck.EntityRenderStateAccess;
import julianh06.wynnextras.features.misc.CurseTracker;
import julianh06.wynnextras.utils.EntityShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.regex.Pattern;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderManagerShaderMixin {
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
        at = @At("HEAD")
    )
    private <S extends EntityRenderState> void we$beforeRender(S state, CameraRenderState camera, double x, double y, double z, PoseStack matrices, SubmitNodeCollector queue, CallbackInfo ci) {
        WynnExtrasConfig c = WynnExtrasConfig.INSTANCE;
        if (!c.curseTrackerEnabled || !c.curseTrackerColorMobs) return;
        if (!(state instanceof EntityRenderStateAccess access)) return;
        Entity entity = access.wynnExtras$getEntity();
        if (entity == null) return;
        if (entity == Minecraft.getInstance().player) return;
        if (entity instanceof Player && PLAYER_NAME_PATTERN.matcher(entity.getName().getString()).matches()) return;
        if (!(entity instanceof LivingEntity)) return;
        if (CurseTracker.cursedEntityIds.contains(entity.getId())) {
            EntityShader.activeShader = 0xFF000000 | c.curseTrackerMobColor.getRGB();
        }
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
        at = @At("RETURN")
    )
    private <S extends EntityRenderState> void we$afterRender(S state, CameraRenderState camera, double x, double y, double z, PoseStack matrices, SubmitNodeCollector queue, CallbackInfo ci) {
        EntityShader.activeShader = null;
    }
}

package julianh06.wynnextras.mixin;

import julianh06.wynnextras.utils.EntityShader;
import julianh06.wynnextras.utils.ShaderVertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(SubmitNodeCollection.class)
public class BatchingRenderCommandQueueMixin {
    @ModifyArgs(
        method = "submitItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer$Submit;<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
        )
    )
    private void we$tintItem(Args args) {
        Integer shader = EntityShader.activeShader;
        if (shader == null) return;
        List<BakedQuad> quads = args.get(6);
        if (quads == null || quads.isEmpty()) return;
        int[] tintLayers = args.get(5);
        args.set(5, EntityShader.mixedTintLayers(tintLayers, quads, shader));
        args.set(6, EntityShader.quadsWithDefaultTintIndex(quads));
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "submitModel", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int we$tintModel(int color) {
        Integer shader = EntityShader.activeShader;
        return shader == null ? color : ARGB.multiply(color, shader);
    }

    @ModifyArg(
        method = "submitCustomGeometry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/CustomFeatureRenderer$Submit;<init>(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeCollector$CustomGeometryRenderer;)V"
        ),
        index = 2
    )
    private SubmitNodeCollector.CustomGeometryRenderer we$tintCustom(SubmitNodeCollector.CustomGeometryRenderer original) {
        Integer shader = EntityShader.activeShader;
        if (shader == null) return original;
        return (entry, consumer) -> original.render(entry, ShaderVertexConsumer.wrap(consumer, shader));
    }
}

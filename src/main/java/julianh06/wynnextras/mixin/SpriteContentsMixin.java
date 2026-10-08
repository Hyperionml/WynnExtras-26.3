package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.spellhider.SpellHider;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import java.util.Optional;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin {
    @Inject(
            method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;)V",
            at = @At("RETURN")
    )
    private void onCreate(Identifier id, FrameSize dimensions, NativeImage image, CallbackInfo ci) {
        addHash(id, image);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V",
            at = @At("RETURN")
    )
    private void onCreate(Identifier id,
                          FrameSize dimensions,
                          NativeImage image,
                          Optional<AnimationMetadataSection> animationResourceMetadata,
                          List<MetadataSectionType.WithValue<?>> additionalMetadata,
                          Optional<TextureMetadataSection> metadata,
                          CallbackInfo ci
    ) {
        addHash(id, image);
    }

    @Unique private static void addHash(Identifier id, NativeImage image) {
        if (!id.getPath().startsWith("item/w")) return;
        int hash = SpellHider.hashNativeImage(image);
        SpellHider.putHash(id.getPath(), hash);
    }
}

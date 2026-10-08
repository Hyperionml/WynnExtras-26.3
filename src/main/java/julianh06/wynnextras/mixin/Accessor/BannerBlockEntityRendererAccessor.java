package julianh06.wynnextras.mixin.Accessor;

import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BannerRenderer.class)
public interface BannerBlockEntityRendererAccessor {
    @Accessor("standingFlagModel")
    BannerFlagModel getStandingFlagModel();
}

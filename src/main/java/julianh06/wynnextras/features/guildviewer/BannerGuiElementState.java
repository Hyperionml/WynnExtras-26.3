package julianh06.wynnextras.features.guildviewer;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public record BannerGuiElementState(
        BannerFlagModel flag,
        DyeColor baseColor,
        BannerPatternLayers resultBannerPatterns,
        int x0,
        int y0,
        int x1,
        int y1,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds,
        float scale
) implements PictureInPictureRenderState {
    public BannerGuiElementState(
            BannerFlagModel bannerFlagBlockModel,
            DyeColor color,
            BannerPatternLayers bannerPatterns,
            int x0,
            int y0,
            int x1,
            int y1,
            @Nullable ScreenRectangle scissorArea,
            float scale
    ) {
        this(bannerFlagBlockModel, color, bannerPatterns, x0, y0, x1, y1, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), scale);
    }
}
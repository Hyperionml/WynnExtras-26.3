package julianh06.wynnextras.features.guildviewer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.joml.Quaternionf;

public class BannerGuiRenderer extends PictureInPictureRenderer<BannerGuiElementState> {
    private final SpriteGetter sprites;

    public BannerGuiRenderer(SpriteGetter sprites) {
        this.sprites = sprites;
    }

    @Override
    protected void renderToTexture(BannerGuiElementState state, PoseStack pose, SubmitNodeCollector collector) {
        Minecraft client = Minecraft.getInstance();
        client.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        pose.rotate(new Quaternionf().rotateY(-0.5f));
        float time = (client.level == null ? 0 : client.level.getGameTime() % 100L) / 100f;
        collector.submitModel(state.flag(), time, pose, 15728880, OverlayTexture.NO_OVERLAY,
                -1, Sheets.BANNER_BASE, sprites, 0);
        BannerRenderer.submitPatterns(sprites, pose, collector, 15728880, OverlayTexture.NO_OVERLAY,
                state.flag(), time, true, state.baseColor(), state.resultBannerPatterns());
    }

    @Override
    public Class<BannerGuiElementState> getRenderStateClass() { return BannerGuiElementState.class; }

    @Override
    protected String getTextureLabel() { return "Banner GUI Renderer"; }
}

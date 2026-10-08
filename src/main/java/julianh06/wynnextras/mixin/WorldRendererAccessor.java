package julianh06.wynnextras.mixin;

import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public interface WorldRendererAccessor {
    @Accessor
    LevelRenderState getLevelRenderState();
}

package julianh06.wynnextras.mixin.Accessor;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemRenderStateAccessor {
    @Accessor("activeLayerCount") int getLayerCount();
    @Accessor("layers") ItemStackRenderState.LayerRenderState[] getLayers();
}

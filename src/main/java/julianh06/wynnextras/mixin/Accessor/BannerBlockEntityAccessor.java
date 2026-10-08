package julianh06.wynnextras.mixin.Accessor;

import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BannerBlockEntity.class)
public interface BannerBlockEntityAccessor {
    @Accessor("patterns")
    public void setPatterns(BannerPatternLayers patterns);
}

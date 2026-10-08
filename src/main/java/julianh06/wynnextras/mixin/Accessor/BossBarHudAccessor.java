package julianh06.wynnextras.mixin.Accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;

@Mixin(BossHealthOverlay.class)
public interface BossBarHudAccessor {
    @Accessor("events")
    Map<UUID, LerpingBossEvent> getBossBars();
}

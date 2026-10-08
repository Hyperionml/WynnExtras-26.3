package julianh06.wynnextras.mixin.Accessor;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface InGameHudAccessor {
    @Accessor("overlayMessageString")
    Component getOverlayMessage();

    @Accessor("title")
    Component getTitle();
}

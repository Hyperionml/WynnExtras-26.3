package julianh06.wynnextras.event;

import julianh06.wynnextras.event.api.WEEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderEvent extends WEEvent {
    public GuiGraphicsExtractor context;
    public int mouseX;
    public int mouseY;
    public float delta;

    public RenderEvent(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.delta = delta;
    }
}

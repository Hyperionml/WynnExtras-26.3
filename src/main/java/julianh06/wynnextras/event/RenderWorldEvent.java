package julianh06.wynnextras.event;

import com.mojang.blaze3d.vertex.PoseStack;
import julianh06.wynnextras.event.api.WEEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SubmitNodeCollector;

public final class RenderWorldEvent extends WEEvent {
    public PoseStack matrices;
    public Camera camera;    public float partialTicks;
    public boolean isCurrentlyDeferring = true;
    public SubmitNodeCollector orderedRenderCommandQueue;

    public RenderWorldEvent(PoseStack matrices, Camera camera, float partialTicks, SubmitNodeCollector orderedRenderCommandQueue) {
        this.matrices = matrices;
        this.camera = camera;        this.partialTicks = partialTicks;
        this.orderedRenderCommandQueue = orderedRenderCommandQueue;
    }
}

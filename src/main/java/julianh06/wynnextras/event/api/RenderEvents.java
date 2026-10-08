package julianh06.wynnextras.event.api;

import julianh06.wynnextras.annotations.WEModule;
import julianh06.wynnextras.event.RenderWorldEvent;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

@WEModule
public class RenderEvents {
    public RenderEvents() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> new RenderWorldEvent(
                context.poseStack(), context.gameRenderer().mainCamera(),
                context.levelState().worldPartialTicks, context.submitNodeCollector()).post());
    }
}

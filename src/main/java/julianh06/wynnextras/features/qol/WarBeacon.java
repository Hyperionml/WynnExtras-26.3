package julianh06.wynnextras.features.qol;

import com.mojang.blaze3d.vertex.PoseStack;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.wynncraft.state.TerritoryState;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.phys.Vec3;

public class WarBeacon {
    private static final int BEAM_RGB = 0x32FF32;
    private static final double BEAM_BASE_Y = -200.0;

    private static boolean loggedRenderFailure = false;
    private static Vec3 testBeaconTarget = null;

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(event -> {
            if (event.submitNodeCollector() == null) return;
            render(event.poseStack(), event.gameRenderer().mainCamera(), event.submitNodeCollector(), event.levelState().worldPartialTicks);
        });
        TerritoryState.initialize();
    }

    private static void render(PoseStack matrices, Camera camera, SubmitNodeCollector orderedRenderCommandQueue, float tickProgress) {
        try {
            if (testBeaconTarget != null) {
                drawBeam(matrices, camera, orderedRenderCommandQueue, tickProgress, testBeaconTarget.x, testBeaconTarget.y, testBeaconTarget.z);
                return;
            }

            if (!WynnExtrasConfig.INSTANCE.warBeaconEnabled) return;
            if (AttackTimer.soonestTerritory == null) return;

            TerritoryState.TerritoryCenter center = TerritoryState.center(AttackTimer.soonestTerritory).orElse(null);
            if (center == null) return;

            drawBeam(matrices, camera, orderedRenderCommandQueue, tickProgress, center.x(), BEAM_BASE_Y, center.z());
        } catch (Exception exception) {
            logRenderFailure(exception);
        }
    }

    private static void drawBeam(PoseStack matrices, Camera camera, SubmitNodeCollector orderedRenderCommandQueue,
                                 float tickProgress, double targetX, double targetY, double targetZ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (camera == null || orderedRenderCommandQueue == null) return;

        Vec3 cameraPos = camera.position();

        double dx = targetX - cameraPos.x;
        double dy = targetY - cameraPos.y;
        double dz = targetZ - cameraPos.z;

        double distance = Math.sqrt(dx * dx + dz * dz);
        int maxDistance = mc.options.renderDistance().get() * 16;
        if (distance > maxDistance) {
            double scale = maxDistance / distance;
            dx *= scale;
            dz *= scale;
        }

        float alpha = 1.0f;
        if (distance <= 7.0) {
            alpha = (float) Math.max(0.0, Math.min(1.0, (distance - 2.0) / 5.0));
        }

        int color = ((int) (alpha * 255.0f) << 24) | BEAM_RGB;
        float animationTime = (mc.level.getGameTime() % 40) + tickProgress;

        matrices.pushPose();
        try {
            matrices.translate(dx, dy, dz);
            BeaconRenderer.submitBeaconBeam(matrices, orderedRenderCommandQueue, BeaconRenderer.BEAM_LOCATION,
                    tickProgress, animationTime, 0, 2048, color, 0.166F, 0.33F);
        } finally {
            matrices.popPose();
        }
    }

    private static void logRenderFailure(Exception exception) {
        if (loggedRenderFailure) return;

        loggedRenderFailure = true;
        WynnExtras.LOGGER.warn("Failed to render war beacon", exception);
    }

}

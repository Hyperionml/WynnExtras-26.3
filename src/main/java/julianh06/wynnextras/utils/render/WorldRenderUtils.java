package julianh06.wynnextras.utils.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.event.RenderWorldEvent;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.Pair;
import julianh06.wynnextras.utils.WEVec;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.awt.*;
import java.util.Set;

public class WorldRenderUtils {
    public static final RenderPipeline FILLED_BOX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(WynnExtras.MOD_ID, "pipeline/debug_filled_box"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL,true))

            .build()
    );

    public static final RenderType FILLED_BOX_LAYER = RenderType.create(
            WynnExtras.MOD_ID + ":filled_box",
            RenderSetup.builder(FILLED_BOX).sortOnUpload().createRenderSetup()
    );

    private static final Matrix4f TEXT_MATRIX = new Matrix4f();

    public static WEVec getViewerPos() {
        return exactLocation(Minecraft.getInstance().gameRenderer.mainCamera());
    }

    public static WEVec exactLocation(Entity entity, float partialTicks) {
        if (!entity.isAlive()) return new WEVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(entity.blockPosition()));
        WEVec prev = new WEVec(entity.xo, entity.yo, entity.zo);

        return prev.add(new WEVec(net.minecraft.world.phys.Vec3.atBottomCenterOf(entity.blockPosition())).subtract(prev).multiply(partialTicks));
    }

    public static WEVec exactLocation(Camera camera) {
        return new WEVec(camera.position());
    }

    public static WEVec exactPlayerEyeLocation(RenderWorldEvent event) {
        return new WEVec(event.camera.position());
    }

    public static Set<Pair<WEVec, WEVec>> calculateEdges(AABB box) {
        WEVec bottomLeftFront = new WEVec(box.minX, box.minY, box.minZ);
        WEVec bottomLeftBack = new WEVec(box.minX, box.minY, box.maxZ);
        WEVec topLeftFront = new WEVec(box.minX, box.maxY, box.minZ);
        WEVec topLeftBack = new WEVec(box.minX, box.maxY, box.maxZ);
        WEVec bottomRightFront = new WEVec(box.maxX, box.minY, box.minZ);
        WEVec bottomRightBack = new WEVec(box.maxX, box.minY, box.maxZ);
        WEVec topRightFront = new WEVec(box.maxX, box.maxY, box.minZ);
        WEVec topRightBack = new WEVec(box.maxX, box.maxY, box.maxZ);

        return Set.of(
                new Pair<>(bottomLeftFront, bottomLeftBack),
                new Pair<>(bottomLeftBack, bottomRightBack),
                new Pair<>(bottomRightBack, bottomRightFront),
                new Pair<>(bottomRightFront, bottomLeftFront),

                new Pair<>(topLeftFront, topLeftBack),
                new Pair<>(topLeftBack, topRightBack),
                new Pair<>(topRightBack, topRightFront),
                new Pair<>(topRightFront, topLeftFront),

                new Pair<>(bottomLeftFront, topLeftFront),
                new Pair<>(bottomLeftBack, topLeftBack),
                new Pair<>(bottomRightBack, topRightBack),
                new Pair<>(bottomRightFront, topRightFront)
        );
    }

    // Draws
    public static void drawFilledBoundingBox(RenderWorldEvent event, AABB box, Color color, float alphaMultiplier) {
        PoseStack matrices = event.matrices;
        Vec3 camera = event.camera.position();

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        event.orderedRenderCommandQueue.submitCustomGeometry(matrices, FILLED_BOX_LAYER, (pose, buffer) -> renderFilledBox(
                pose.pose(), buffer,
                (float) box.minX, (float) box.minY, (float) box.minZ,
                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                color.getRed() / 255f * 0.9f,
                color.getGreen() / 255f * 0.9f,
                color.getBlue() / 255f * 0.9f,
                color.getAlpha() / 255f * alphaMultiplier
        ));

        matrices.popPose();
    }

    private static void renderFilledBox(Matrix4fc positionMatrix, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        // Front Face
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Back face
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // Left face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Right face
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Top face
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Bottom face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    public static void drawEdges(RenderWorldEvent event, AABB box, Color color, int lineWidth, boolean depth) {
        LineDrawer.draw3D(event, lineWidth, depth, lineDrawer -> lineDrawer.drawEdges(box, color));
    }

    public static void drawEdges(RenderWorldEvent event, WEVec location, Color color, int lineWidth, boolean depth) {
        LineDrawer.draw3D(event, lineWidth, depth, lineDrawer -> lineDrawer.drawEdges(location, color));
    }

    public static void draw3DLine(RenderWorldEvent event, WEVec p1, WEVec p2, Color color, int lineWidth, boolean depth) {
        LineDrawer.draw3D(event, lineWidth, depth, lineDrawer -> lineDrawer.draw3DLine(p1, p2, color));
    }

    public static void drawLineToEye(RenderWorldEvent event, WEVec location, Color color, int lineWidth, boolean depth) {
        WEVec rotationVec = new WEVec(MinecraftUtils.localPlayer().getViewVector(event.partialTicks));
        draw3DLine(event, exactPlayerEyeLocation(event).add(rotationVec.multiply(2)), location, color, lineWidth, depth);
    }

    public static void draw3DCircle(RenderWorldEvent event, WEVec location, double radius, Color color, int lineWidth, boolean depth) {
        LineDrawer.draw3D(event, lineWidth, depth, lineDrawer -> {
            WEVec lastPoint = location.add(radius, 0, 0);

            for (int i = 1; i <= 360; i++) {
                double rad = Math.toRadians(i);
                WEVec newPoint = location.add(Math.cos(rad) * radius, 0, Math.sin(rad) * radius);
                lineDrawer.draw3DLine(lastPoint, newPoint, color);
                lastPoint = newPoint;
            }
        });
    }

    public static void drawText(
            RenderWorldEvent event,
            WEVec location,
            Component text,
            float scale,
            boolean depth
    ) {
        Minecraft client = Minecraft.getInstance();
        Font textRenderer = client.font;
        Camera camera = client.gameRenderer.mainCamera();

        Matrix4f matrix = TEXT_MATRIX.identity();
        Vec3 viewerPos = camera.position();
        float adjustedScale = scale * 0.05f;

        matrix.translate(
                (float) (location.x() - viewerPos.x),
                (float) (location.y() - viewerPos.y),
                (float) (location.z() - viewerPos.z)
        ).rotate(camera.rotation()).scale(adjustedScale, -adjustedScale, adjustedScale);

        PoseStack textPose=new PoseStack();
        textPose.mulPose(matrix);
        event.orderedRenderCommandQueue.submitText(textPose,-textRenderer.width(text)/2f,0,text.getVisualOrderText(),false,
                depth?Font.DisplayMode.NORMAL:Font.DisplayMode.SEE_THROUGH,15728880,-1,0,0);
    }


}

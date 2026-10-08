package julianh06.wynnextras.utils.render;

import julianh06.wynnextras.event.RenderWorldEvent;
import julianh06.wynnextras.utils.Pair;
import julianh06.wynnextras.utils.WEVec;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LineDrawer {
    private final List<QueuedLine> queuedLines = new ArrayList<>();

    private static class QueuedLine {
        private final WEVec p1;
        private final WEVec p2;
        private final Color color;

        public QueuedLine(WEVec p1, WEVec p2, Color color) {
            this.p1 = p1;
            this.p2 = p2;
            this.color = color;
        }
    }

    private final RenderWorldEvent event;
    private final int lineWidth;
    private final boolean depth;

    public LineDrawer(RenderWorldEvent event, int lineWidth, boolean depth) {
        this.event = event;
        this.lineWidth = lineWidth;
        this.depth = depth;
    }

    private void drawQueuedLines() {
        if (queuedLines.isEmpty()) return;

        var lines = List.copyOf(queuedLines);
        Vec3 cameraPosition = event.camera.position();
        event.orderedRenderCommandQueue.submitCustomGeometry(event.matrices, WorldRenderUtils.FILLED_BOX_LAYER, (matrix, buffer) -> {

        float halfWidth = lineWidth * -0.01f;

        for (QueuedLine line : lines) {
            WEVec dir = line.p2.subtract(line.p1).normalize();
            Vec3 toCamera1 = cameraPosition.subtract(line.p1.toVec3d()).normalize();
            Vec3 toCamera2 = cameraPosition.subtract(line.p2.toVec3d()).normalize();
            WEVec right1 = new WEVec(toCamera1).cross(dir);
            WEVec right2 = new WEVec(toCamera2).cross(dir);

            if (right1.lengthSquared() < 1e-6) {
                right1 = new WEVec(0, 1, 0).cross(dir);
            }
            if (right2.lengthSquared() < 1e-6) {
                right2 = new WEVec(0, 1, 0).cross(dir);
            }

            right1 = right1.normalize().multiply(halfWidth);
            right2 = right2.normalize().multiply(halfWidth);

            WEVec p1a = line.p1.add(right1);
            WEVec p1b = line.p1.subtract(right1);
            WEVec p2a = line.p2.add(right2);
            WEVec p2b = line.p2.subtract(right2);

            buffer.addVertex(matrix.pose(), (float)p1a.x(), (float)p1a.y(), (float)p1a.z())
                    .setColor(line.color.getRed(), line.color.getGreen(), line.color.getBlue(), line.color.getAlpha());

            buffer.addVertex(matrix.pose(), (float)p2a.x(), (float)p2a.y(), (float)p2a.z())
                    .setColor(line.color.getRed(), line.color.getGreen(), line.color.getBlue(), line.color.getAlpha());

            buffer.addVertex(matrix.pose(), (float)p2b.x(), (float)p2b.y(), (float)p2b.z())
                    .setColor(line.color.getRed(), line.color.getGreen(), line.color.getBlue(), line.color.getAlpha());

            buffer.addVertex(matrix.pose(), (float)p1b.x(), (float)p1b.y(), (float)p1b.z())
                    .setColor(line.color.getRed(), line.color.getGreen(), line.color.getBlue(), line.color.getAlpha());
        }

        });
        queuedLines.clear();
    }

    private void addQueuedLine(WEVec p1, WEVec p2, Color color) {
        QueuedLine last = queuedLines.isEmpty() ? null : queuedLines.getLast();

        if (last == null) {
            queuedLines.add(new QueuedLine(p1, p2, color));
            return;
        }

        if (!last.p2.equals(p1)) {
            drawQueuedLines();
        }

        queuedLines.add(new QueuedLine(p1, p2, color));
    }

    public void drawEdges(WEVec location, Color color) {
        for (Pair<WEVec, WEVec> edge: location.edges()) {
            draw3DLine(edge.getFirst(), edge.getSecond(), color);
        }
    }

    public void drawEdges(AABB box, Color color) {
        for (Pair<WEVec, WEVec> edge: WorldRenderUtils.calculateEdges(box)) {
            draw3DLine(edge.getFirst(), edge.getSecond(), color);
        }
    }

    public void draw3DLine(WEVec p1, WEVec p2, Color color) {
        addQueuedLine(p1, p2, color);
    }

    static void draw3D(RenderWorldEvent event, int lineWidth, boolean depth, LineDrawerDraws draws) {
        event.matrices.pushPose();

        WEVec inverseView = WorldRenderUtils.getViewerPos().negate();
        event.matrices.translate(inverseView.x(), inverseView.y(), inverseView.z());

        LineDrawer lineDrawer = new LineDrawer(event, lineWidth, depth);
        draws.draw(lineDrawer);
        lineDrawer.drawQueuedLines();

        event.matrices.popPose();
    }

    @FunctionalInterface
    interface LineDrawerDraws {
        void draw(LineDrawer lineDrawer);
    }
}

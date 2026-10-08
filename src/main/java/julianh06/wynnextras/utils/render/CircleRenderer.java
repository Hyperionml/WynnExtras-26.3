package julianh06.wynnextras.utils.render;
import com.mojang.blaze3d.vertex.PoseStack;
import julianh06.wynnextras.utils.WEVec;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
public class CircleRenderer {
 public static CircleRenderer instance;
 private static final int SEGMENTS=256;
 private static final float HEIGHT=0.05f;
 private final List<CircleData> circleQueue=new ArrayList<>();
 public record CircleData(WEVec location,double radius,Color color) {}
 public void queueCircle(WEVec location,double radius,Color color) { circleQueue.add(new CircleData(location,radius,color)); }
 public void renderQueuedCircles(LevelRenderContext context) {
  if(circleQueue.isEmpty()) return;
  var circles=List.copyOf(circleQueue); circleQueue.clear();
  PoseStack pose=context.poseStack(); Vec3 camera=context.levelState().cameraRenderState.pos;
  pose.pushPose(); pose.translate(-camera.x,-camera.y,-camera.z);
  context.submitNodeCollector().submitCustomGeometry(pose,WorldRenderUtils.FILLED_BOX_LAYER,(matrix,buffer)-> {
   for(var circle:circles) renderCircle(buffer,matrix.pose(),circle.location(),circle.radius(),circle.color());
  });
  pose.popPose();
 }
    private void renderCircle(com.mojang.blaze3d.vertex.VertexConsumer buffer, Matrix4fc posMatrix, WEVec location, double radius, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float cx = (float) location.x();
        float cy = (float) location.y();
        float cz = (float) location.z();

        double angleStep = 2 * Math.PI / SEGMENTS;

        for (int i = 0; i < SEGMENTS; i++) {
            double angle1 = i * angleStep;
            double angle2 = (i + 1) * angleStep;

            float x1 = cx + (float) (Math.cos(angle1) * radius);
            float z1 = cz + (float) (Math.sin(angle1) * radius);
            float x2 = cx + (float) (Math.cos(angle2) * radius);
            float z2 = cz + (float) (Math.sin(angle2) * radius);

            // Draw outer face (visible from outside)
            buffer.addVertex(posMatrix, x1, cy, z1).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x1, cy + HEIGHT, z1).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x2, cy + HEIGHT, z2).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x2, cy, z2).setColor(r, g, b, a);

            // Draw inner face (visible from inside) - reverse winding order
            buffer.addVertex(posMatrix, x2, cy, z2).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x2, cy + HEIGHT, z2).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x1, cy + HEIGHT, z1).setColor(r, g, b, a);
            buffer.addVertex(posMatrix, x1, cy, z1).setColor(r, g, b, a);
        }
    }

}

package julianh06.wynnextras.utils;

import com.mojang.math.Transformation;
import julianh06.wynnextras.mixin.Invoker.GetTransformationInvoker;
import net.minecraft.world.entity.Display;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class EntityUtils {

    // TODO
    public static void setScale(Display e, Vector3fc scale) {
        Transformation current = GetTransformationInvoker.invokeGetTransformation(e.getEntityData());

        /*
        if (current == null) return;

        Matrix4fc matrix = current.getMatrix();
        Matrix4f scaleMatrix = new Matrix4f().scaling(scale.x(), scale.y(), scale.z());
        matrix = scaleMatrix.mul(matrix, new Matrix4f());

        AffineTransformation resultMatrix = new AffineTransformation(matrix);

        AffineTransformation result = new AffineTransformation(
                resultMatrix.translation(),
                current.leftRotation(),
                resultMatrix.scale(),
                current.rightRotation()
        );
        e.setTransformation(result);

         */


        Vector3f newScale = new Vector3f();
        current.scale().mul(scale, newScale);

        Transformation affineTransformation = new Transformation(
                current.translation(),
                current.leftRotation(),
                newScale,
                current.rightRotation());
        e.setTransformation(affineTransformation);

    }
}

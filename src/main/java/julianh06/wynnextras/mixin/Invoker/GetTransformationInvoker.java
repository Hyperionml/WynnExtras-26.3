package julianh06.wynnextras.mixin.Invoker;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface GetTransformationInvoker {
    @Invoker("createTransformation") static Transformation invokeGetTransformation(SynchedEntityData dataTracker) { throw new AssertionError(); }
}

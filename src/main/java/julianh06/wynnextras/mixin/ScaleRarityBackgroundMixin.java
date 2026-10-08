package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.inventory.ScaleBackgroundRenderState;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DataComponentHolder.class)
public interface ScaleRarityBackgroundMixin {
    @Inject(
            method = "get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void removeRarityModelWhileDrawingScale(
            DataComponentType<?> type,
            CallbackInfoReturnable<Object> cir
    ) {
        if (type != DataComponents.CUSTOM_MODEL_DATA) return;
        if (!((Object) this instanceof ItemStack stack)) return;
        if (!ScaleBackgroundRenderState.isActive(stack)) return;
        if (!(cir.getReturnValue() instanceof CustomModelData modelData)) return;

        List<String> strings = modelData.strings();
        List<String> withoutRarity = strings.stream()
                .map(value -> value.startsWith("item_tier") ? "" : value)
                .toList();
        if (withoutRarity.equals(strings)) return;

        cir.setReturnValue(new CustomModelData(
                modelData.floats(),
                modelData.flags(),
                withoutRarity,
                modelData.colors()
        ));
    }
}

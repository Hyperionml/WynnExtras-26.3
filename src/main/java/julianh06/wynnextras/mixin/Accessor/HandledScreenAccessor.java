package julianh06.wynnextras.mixin.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin (AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor("hoveredSlot")
    void setFocusedSlot(Slot slot);

    @Accessor("hoveredSlot")
    Slot getFocusedSlot();

    @Accessor("leftPos")
    int getX();

    @Accessor("topPos")
    int getY();

    @Accessor
    int getImageWidth();

    @Accessor
    int getImageHeight();
}

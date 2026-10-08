package julianh06.wynnextras.mixin.Invoker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenInvoker {
    @Invoker("extractFloatingItem")
    void invokeDrawItem(GuiGraphicsExtractor context, ItemStack stack, int x, int y, @Nullable String amountText);

    @Invoker("extractSlotHighlightBack")
    void invokeDrawSlotHighlightBack(GuiGraphicsExtractor context);

    @Invoker("extractSlotHighlightFront")
    void invokeDrawSlotHighlightFront(GuiGraphicsExtractor context);

    @Invoker("extractTooltip")
    void invokeDrawMouseoverTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY);
}

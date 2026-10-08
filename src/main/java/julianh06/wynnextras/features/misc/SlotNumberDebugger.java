package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.core.WynnExtras;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;

public final class SlotNumberDebugger {
    private static boolean enabled;

    private SlotNumberDebugger() {}

    public static void toggle() {
        enabled = !enabled;
        WynnExtras.sendMessageToClient(
                enabled ? "§aSlot numbers enabled." : "§cSlot numbers disabled.");
    }

    public static void render(GuiGraphicsExtractor context, Slot slot) {
        if (!enabled) return;

        String slotNumber = Integer.toString(slot.index);
        var textRenderer = Minecraft.getInstance().font;
        int x = slot.x + 16 - textRenderer.width(slotNumber);
        context.text(textRenderer, slotNumber, x, slot.y, 0xFFFFFF55, true);
    }
}

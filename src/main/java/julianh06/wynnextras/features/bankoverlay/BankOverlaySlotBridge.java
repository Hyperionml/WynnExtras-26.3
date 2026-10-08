package julianh06.wynnextras.features.bankoverlay;

import julianh06.wynnextras.utils.HandledScreenAccess;
import julianh06.wynnextras.utils.SlotAccess;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps the backing ScreenHandler slots aligned with BankOverlay's custom layout.
 *
 * Other mods usually discover inventory interaction through HandledScreen#getHoveredSlot
 * and ScreenHandler#slots. BankOverlay renders its own widgets, so those consumers need
 * the real active slots moved to the same screen coordinates while the overlay is active.
 */
public final class BankOverlaySlotBridge {
    private static final int HIDDEN_SLOT_COORD = -100000;
    private static final Map<Slot, SlotPosition> ORIGINAL_POSITIONS = new IdentityHashMap<>();
    private static final Set<Slot> EXPOSED_SLOTS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<Slot> PREVIOUS_EXPOSED_SLOTS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static AbstractContainerMenu activeHandler;
    private static int activeSlotCount = -1;

    private BankOverlaySlotBridge() {}

    public static void beginFrame(AbstractContainerScreen<?> screen) {
        if (screen == null || screen.getMenu() == null) return;

        AbstractContainerMenu handler = screen.getMenu();
        if (handler != activeHandler) {
            restoreAll();
            activeHandler = handler;
            activeSlotCount = handler.slots.size();
            hideAll(handler);
            return;
        }

        if (handler.slots.size() != activeSlotCount) {
            restoreAll();
            activeHandler = handler;
            activeSlotCount = handler.slots.size();
            hideAll(handler);
            return;
        }

        PREVIOUS_EXPOSED_SLOTS.clear();
        PREVIOUS_EXPOSED_SLOTS.addAll(EXPOSED_SLOTS);
        EXPOSED_SLOTS.clear();
    }

    public static void expose(AbstractContainerScreen<?> screen, Slot slot, int screenX, int screenY) {
        if (screen == null || slot == null) return;

        remember(slot);
        move(slot, screenX - HandledScreenAccess.x(screen), screenY - HandledScreenAccess.y(screen));
        EXPOSED_SLOTS.add(slot);
        PREVIOUS_EXPOSED_SLOTS.remove(slot);
    }

    public static void endFrame() {
        for (Slot slot : PREVIOUS_EXPOSED_SLOTS) {
            hide(slot);
        }
        PREVIOUS_EXPOSED_SLOTS.clear();
    }

    public static Slot getExposedSlotAt(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        if (screen == null) return null;

        int screenX = HandledScreenAccess.x(screen);
        int screenY = HandledScreenAccess.y(screen);

        for (Slot slot : EXPOSED_SLOTS) {
            int slotX = screenX + slot.x;
            int slotY = screenY + slot.y;
            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                return slot;
            }
        }

        return null;
    }

    public static void restoreAll() {
        for (Map.Entry<Slot, SlotPosition> entry : ORIGINAL_POSITIONS.entrySet()) {
            move(entry.getKey(), entry.getValue().x(), entry.getValue().y());
        }
        ORIGINAL_POSITIONS.clear();
        EXPOSED_SLOTS.clear();
        PREVIOUS_EXPOSED_SLOTS.clear();
        activeHandler = null;
        activeSlotCount = -1;
    }

    private static void hideAll(AbstractContainerMenu handler) {
        for (Slot slot : handler.slots) {
            hide(slot);
        }
    }

    private static void hide(Slot slot) {
        remember(slot);
        move(slot, HIDDEN_SLOT_COORD, HIDDEN_SLOT_COORD);
    }

    private static void remember(Slot slot) {
        ORIGINAL_POSITIONS.computeIfAbsent(slot, s -> new SlotPosition(s.x, s.y));
    }

    private static void move(Slot slot, int x, int y) {
        SlotAccess.setPosition(slot, x, y);
    }

    private record SlotPosition(int x, int y) {}
}

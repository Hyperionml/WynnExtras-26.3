package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.config.WynnExtrasConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom overlay for the Wynncraft "Encounter Selection" container. Hides the
 * vanilla chest GUI behind a dim layer and shows one large element-colored
 * panel per available encounter. Clicking a panel forwards the real slot click.
 */
public class EncounterOverlay {
    private static final String[][] ENCOUNTERS = {
            {"Wind-Blessed", "Air"},
            {"Wave-Blessed", "Water"},
            {"Earthen-Blessed", "Earth"},
            {"Shock-Blessed", "Thunder"},
            {"Flame-Blessed", "Fire"}
    };

    private static final Map<String, Integer> ELEMENT_COLORS = Map.of(
            "Fire", 0xFFFF4D3D,
            "Water", 0xFF3D9BFF,
            "Air", 0xFFE0E0E0,
            "Earth", 0xFF6FBB3E,
            "Thunder", 0xFFFFDD33
    );

    // Latched options keyed by element name so the same element can never appear
    // twice even if the server briefly moves it to a different slot. Preserved across
    // frames while the encounter screen is open so transient empty-slot packets don't
    // shrink the visible panel count.
    private static final java.util.LinkedHashMap<String, Option> latchedOptions = new java.util.LinkedHashMap<>();
    private static String latchedTitle = null;
    private static Screen latchedScreen = null;

    public record Option(String element, int slot, String itemName) {}

    public static boolean isEncounterScreen(Screen screen) {
        if (screen == null) return false;
        String title = screen.getTitle().getString();
        return title.contains("Encounter Selection");
    }

    /** Live re-scan that doesn't update the latch. */
    private static List<Option> scanOptionsRaw() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return List.of();
        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null) return List.of();
        List<Option> found = new ArrayList<>();
        int max = Math.min(menu.slots.size(), 54);
        for (int i = 0; i < max; i++) {
            ItemStack s = menu.getSlot(i).getItem();
            if (s.isEmpty()) continue;
            String name = s.getHoverName().getString();
            for (String[] enc : ENCOUNTERS) {
                if (name.contains(enc[0])) {
                    found.add(new Option(enc[1], i, name));
                    break;
                }
            }
        }
        return found;
    }

    /** Returns the current latched options. Updates the latch from the live scan
     *  (adds new entries, refreshes existing ones) but never drops slots — those
     *  are only cleared when the screen closes via {@link #resetLatch}. */
    public static List<Option> scanOptions() {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.gui.screen();
        if (screen == null || !isEncounterScreen(screen)) {
            resetLatch();
            return List.of();
        }
        String title = screen.getTitle().getString();
        if (screen != latchedScreen || !title.equals(latchedTitle)) {
            // New screen instance - start fresh.
            latchedOptions.clear();
            latchedTitle = title;
            latchedScreen = screen;
        }
        for (Option o : scanOptionsRaw()) {
            latchedOptions.put(o.element(), o);
        }
        return new ArrayList<>(latchedOptions.values());
    }

    private static void resetLatch() {
        latchedOptions.clear();
        latchedTitle = null;
        latchedScreen = null;
    }

    public static void clearLatchIfNoContainerOpen() {
        if (latchedTitle == null && latchedScreen == null && latchedOptions.isEmpty()) return;
        if (!(Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?>)) {
            resetLatch();
        }
    }

    private static int[] panelBounds(int index, int total, int screenW, int screenH) {
        int sideMargin = 20;
        int topMargin = 60;   // leaves room for the header
        int bottomMargin = 40;
        int gap = 16;
        int panelW = (screenW - sideMargin * 2 - gap * (total - 1)) / total;
        int panelH = screenH - topMargin - bottomMargin;
        int x = sideMargin + index * (panelW + gap);
        int y = topMargin;
        return new int[]{x, y, panelW, panelH};
    }

    /** Kept for mixin compatibility — no-op now that there's no settle state. */
    public static void tickSettle(Screen screen) {}

    /** Returns true if we should take over rendering from the vanilla chest UI.
     *  Wynncraft encounter screens always have at least 2 options; if we only see 1
     *  then a slot packet is still in-flight — let vanilla render until the 2nd lands. */
    public static boolean isReadyToRender(Screen screen) {
        if (!WynnExtrasConfig.INSTANCE.encounterOverlayEnabled) return false;
        if (!isEncounterScreen(screen)) return false;
        return scanOptions().size() >= 2;
    }

    public static void render(GuiGraphicsExtractor ctx, Screen screen, int mouseX, int mouseY) {
        if (!WynnExtrasConfig.INSTANCE.encounterOverlayEnabled) return;
        if (!isEncounterScreen(screen)) return;
        List<Option> options = scanOptions();
        if (options.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Font tr = mc.font;
        int screenW = screen.width;
        int screenH = screen.height;

        // Full-screen dim to hide the vanilla chest.
        ctx.fill(0, 0, screenW, screenH, 0xDD000000);

        String header = "Encounter Selection";
        int hw = tr.width(header);
        ctx.text(tr, header, screenW / 2 - hw / 2, 24, 0xFFFFFFFF, true);

        int count = options.size();
        for (int i = 0; i < count; i++) {
            Option opt = options.get(i);
            int[] b = panelBounds(i, count, screenW, screenH);
            int x = b[0], y = b[1], w = b[2], h = b[3];
            int baseColor = ELEMENT_COLORS.getOrDefault(opt.element, 0xFF888888);
            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            int fill = hovered ? lighten(baseColor) : baseColor;

            ctx.fill(x, y, x + w, y + h, fill);
            // White inner border for contrast.
            int border = 0xFFFFFFFF;
            ctx.fill(x, y, x + w, y + 2, border);
            ctx.fill(x, y + h - 2, x + w, y + h, border);
            ctx.fill(x, y, x + 2, y + h, border);
            ctx.fill(x + w - 2, y, x + w, y + h, border);

            // Scale the element name to fill about 30% of the panel height.
            float nameScale = Math.max(3.0f, Math.min(h / 40f, w / 60f));
            ctx.pose().pushMatrix();
            ctx.pose().translate(x + w / 2f, y + h / 2f - 12);
            ctx.pose().scale(nameScale, nameScale);
            int nw = tr.width(opt.element);
            int nh = tr.lineHeight;
            ctx.text(tr, opt.element, -nw / 2, -nh / 2, 0xFF000000, false);
            ctx.pose().popMatrix();

            // Full item name under the element.
            int iw = tr.width(opt.itemName);
            ctx.text(tr, opt.itemName, x + w / 2 - iw / 2, y + h - 22, 0xFFFFFFFF, true);
        }
    }

    public static boolean handleClick(double mouseX, double mouseY, Screen screen) {
        if (!WynnExtrasConfig.INSTANCE.encounterOverlayEnabled) return false;
        if (!isEncounterScreen(screen)) return false;
        List<Option> options = scanOptions();
        if (options.isEmpty()) return false;

        int screenW = screen.width;
        int screenH = screen.height;
        int count = options.size();
        for (int i = 0; i < count; i++) {
            int[] b = panelBounds(i, count, screenW, screenH);
            if (mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3]) {
                Minecraft mc = Minecraft.getInstance();
                AbstractContainerMenu handler = mc.player != null ? mc.player.containerMenu : null;
                if (handler == null || mc.gameMode == null) return true;
                mc.gameMode.handleContainerInput(handler.containerId, options.get(i).slot, 0, ContainerInput.PICKUP, mc.player);
                return true;
            }
        }
        return true; // eat click so vanilla slots aren't clickable through the dim
    }

    private static int lighten(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 40);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 40);
        int b = Math.min(255, (argb & 0xFF) + 40);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}

package julianh06.wynnextras.compat.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/** Registers the existing overlays with Fabric's HUD element API. */
public final class HudRenderCallback {
    public static final Registry EVENT = new Registry();

    private HudRenderCallback() {}

    public static final class Registry {
        private int nextId;

        public void register(HudElement element) {
            HudElementRegistry.addLast(
                    Identifier.fromNamespaceAndPath("wynnextras", "overlay_" + nextId++), element);
        }
    }
}

package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.event.ChatEvent;
import julianh06.wynnextras.event.api.WEEventBus;
import julianh06.wynnextras.mixin.Accessor.InGameHudAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

public class AutoSkipDialogue {
    private static long lastRun = 0;

    public static void register() {
        WEEventBus.registerEventListener(new AutoSkipDialogue());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!WynnExtrasConfig.INSTANCE.autoSkipDialogueEnabled) return;
            if (client.player == null) return;
            if (client.gui.screen() != null) return;
            if (System.currentTimeMillis() - lastRun < 200) return;

            Component overlay = ((InGameHudAccessor) client.gui.hud).getOverlayMessage();
            if (isSkippableDialogue(overlay)) {
                pressSneak();
                lastRun = System.currentTimeMillis();
            }
        });
    }

    @SubscribeEvent
    public void onChat(ChatEvent event) {
        if (!WynnExtrasConfig.INSTANCE.autoSkipDialogueEnabled) return;
        if (Minecraft.getInstance().gui.screen() != null) return;
        String msg = event.message.getString();
        if (msg.contains("  Press SHIFT to continue") || msg.contains("  Press SNEAK to continue")) {
            pressSneak();
        }
    }

    private static boolean isSkippableDialogue(Component message) {
        if (message == null) return false;
        String s = message.getString();
        if (s.equals(" to confirm") || s.equals(" to continue") || s.contains(" to continue")) return true;
        for (Component sibling : message.getSiblings()) {
            if (isSkippableDialogue(sibling)) return true;
        }
        return false;
    }

    private static void pressSneak() {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping sneak = mc.options.keyShift;
        new Thread(() -> {
            try {
                sneak.setDown(true);
                Thread.sleep(100);
                sneak.setDown(false);
            } catch (InterruptedException ignored) {}
        }).start();
    }
}

package julianh06.wynnextras.features.chat;

import julianh06.wynnextras.wynncraft.state.RaidState;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.utils.BossBarUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;

/**
 * Watches the boss-bar HUD for "X/6 Chains" texts and emits a PB-style chat
 * notification the first time each count (0..6) shows up in a raid.
 *
 * Reset via {@link RaidChatNotifier#resetCounters()} on raid start.
 */
public class ChainsAttachedTracker {
    private static final boolean[] fired = new boolean[7];

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ChainsAttachedTracker::tick);
    }

    public static void resetForNewRaid() {
        for (int i = 0; i < fired.length; i++) fired[i] = false;
    }

    private static void tick(Minecraft client) {
        if (!WynnExtrasConfig.INSTANCE.toggleRaidTimestamps) return;
        if (client == null || client.player == null || client.gui == null) return;

        BossHealthOverlay hud = client.gui.hud.getBossOverlay();
        if (hud == null) return;
        Iterable<LerpingBossEvent> bars = BossBarUtils.getBossBars(hud);

        for (LerpingBossEvent bar : bars) {
            Component n = bar.getName();
            if (n == null) continue;
            String s = n.getString();
            if (s == null) continue;
            String lower = s.toLowerCase();
            if (!lower.contains("chain")) continue;
            for (int i = 0; i <= 6; i++) {
                if (fired[i]) continue;
                if (s.contains(i + "/6")) {
                    fired[i] = true;
                    emit(i);
                    break;
                }
            }
        }
    }

    private static void emit(int count) {
        long currentTime = RaidState.currentRoomTime();
        String timestamp = RaidChatNotifier.formatTime(currentTime);
        String pbKey = "chains_" + count + "_of_6";
        String label = (count == 0) ? "Chainphase started" : count + "/6 Chains attached";
        String message = "§b" + label + " §c@ " + timestamp;

        if (currentTime > 0) {
            Long pb = RaidChatNotifier.getPB(pbKey);
            if (pb == null || currentTime < pb) {
                RaidChatNotifier.savePB(pbKey, currentTime);
                message += (pb == null ? " §e[First PB]" : " §e[New PB! Old: " + RaidChatNotifier.formatTime(pb) + "]");
            } else {
                message += " §7[PB: " + RaidChatNotifier.formatTime(pb) + "]";
            }
        }

        final String finalMessage = message;
        Minecraft.getInstance().execute(() ->
                MinecraftUtils.sendMessageToClient(WynnExtras.addWynnExtrasPrefix(Component.nullToEmpty(finalMessage))));
    }
}

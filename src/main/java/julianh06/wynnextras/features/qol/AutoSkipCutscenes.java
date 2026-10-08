package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.utils.BossBarUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

/**
 * Auto-skips Wynncraft cutscenes that prompt "Swap Hands to skip".
 *
 * Watches the boss-bar HUD for the trigger text and sends a SWAP_ITEM_WITH_OFFHAND
 * action every 4 ticks while it's visible — server interprets it as the player
 * pressing the offhand-swap key, so the cutscene advances without input.
 */
public class AutoSkipCutscenes {
    private static final String TRIGGER = "swap hands to";
    private static final int INTERVAL_TICKS = 4;

    private static int tickCounter = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!WynnExtrasConfig.INSTANCE.autoSkipCutscenesEnabled) { tickCounter = 0; return; }
            if (client == null || client.player == null || client.getConnection() == null) { tickCounter = 0; return; }
            if (client.gui == null) { tickCounter = 0; return; }

            BossHealthOverlay hud = client.gui.hud.getBossOverlay();
            if (hud == null) { tickCounter = 0; return; }
            Iterable<LerpingBossEvent> bars = BossBarUtils.getBossBars(hud);

            boolean trigger = false;
            for (LerpingBossEvent bar : bars) {
                Component n = bar.getName();
                if (n == null) continue;
                String s = n.getString();
                if (s != null && s.toLowerCase().contains(TRIGGER)) { trigger = true; break; }
            }
            if (!trigger) { tickCounter = 0; return; }

            if (tickCounter++ % INTERVAL_TICKS == 0) {
                client.getConnection().send(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                        BlockPos.ZERO,
                        Direction.DOWN
                ));
            }
        });
    }
}

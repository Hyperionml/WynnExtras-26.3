package julianh06.wynnextras.event.api;


import julianh06.wynnextras.annotations.WEModule;
import julianh06.wynnextras.event.DisconnectEvent;
import julianh06.wynnextras.event.TickEvent;
import julianh06.wynnextras.event.WorldChangeEvent;
import julianh06.wynnextras.event.KeyInputEvent;
import julianh06.wynnextras.utils.MinecraftUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import julianh06.wynnextras.compat.client.InputCompat;


@WEModule
public class ClientEvents {
    private static int totalTicks = 0;

    public ClientEvents() {
        ClientTickEvents.START_LEVEL_TICK.register(world -> {
            if (!MinecraftUtils.localPlayerExists()) return;
            if (!MinecraftUtils.localWorldExists()) return;

            new TickEvent(nextTick()).post();
        });

        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            new DisconnectEvent().post();
        }));

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, world) -> {
            new WorldChangeEvent().post();
        });
    }

    private static int nextTick() {
        totalTicks++;
        return totalTicks;
    }
}

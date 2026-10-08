package julianh06.wynnextras.features.qol;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.config.WynnExtrasConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import julianh06.wynnextras.compat.client.InputCompat;

public class TerritoryMenuKey {
    private static boolean openingTerritory = false;
    private static boolean openingBank = false;
    private static long lastAction = 0;
    private static boolean territoryKeyWasDown = false;
    private static boolean bankKeyWasDown = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            WynnExtrasConfig c = WynnExtrasConfig.INSTANCE;

            long window = client.getWindow().handle();

            // Territory / Eco menu key
            if (c.territoryMenuKeyEnabled) {
                boolean tdown = InputCompat.keyState(window, c.territoryMenuKey) == InputCompat.PRESS;
                if (tdown && !territoryKeyWasDown && client.gui.screen() == null) {
                    if (client.getConnection() != null) {
                        client.getConnection().sendCommand("gu manage");
                        openingTerritory = true;
                        openingBank = false;
                        lastAction = System.currentTimeMillis();
                    }
                }
                territoryKeyWasDown = tdown;
            }

            // Guild bank key
            if (c.guildBankKeyEnabled) {
                boolean bdown = InputCompat.keyState(window, c.guildBankKey) == InputCompat.PRESS;
                if (bdown && !bankKeyWasDown && client.gui.screen() == null) {
                    if (client.getConnection() != null) {
                        client.getConnection().sendCommand("gu manage");
                        openingBank = true;
                        openingTerritory = false;
                        lastAction = System.currentTimeMillis();
                    }
                }
                bankKeyWasDown = bdown;
            }

            // Abort if we've been waiting too long
            if ((openingTerritory || openingBank) && System.currentTimeMillis() - lastAction > 3000) {
                openingTerritory = false;
                openingBank = false;
            }

            // When the "Manage" menu opens, click the appropriate slot
            if ((openingTerritory || openingBank)
                    && client.gui.screen() instanceof ContainerScreen gcs
                    && gcs.getTitle().getString().contains(": Manage")
                    && client.player.containerMenu != null) {
                AbstractContainerMenu handler = client.player.containerMenu;
                int targetSlot = openingTerritory ? 14 : 15;
                String expectedName = openingTerritory ? "Territories" : "Bank";
                if (handler.slots.size() > targetSlot) {
                    ItemStack stack = handler.slots.get(targetSlot).getItem();
                    if (stack.getHoverName().getString().contains(expectedName)) {
                        if (client.gameMode != null) {
                            client.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, client.player);
                        }
                        openingTerritory = false;
                        openingBank = false;
                    }
                }
            }
        });
    }
}

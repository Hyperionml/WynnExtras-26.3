package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.ContainerUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import static julianh06.wynnextras.utils.ContainerUtils.clickOnSlot;

public class FastRequeue {
    static boolean inRaidChest = false;
    private static boolean waitingForQueue = false;

    public static void registerFastRequeue() {
        ClientTickEvents.END_CLIENT_TICK.register((tick) -> {
            Minecraft client = Minecraft.getInstance();
            if(client.player == null || client.level == null) { return; }

            AbstractContainerMenu currScreenHandler = MinecraftUtils.containerMenu();
            if(currScreenHandler == null) { return; }

            Screen currScreen = MinecraftUtils.mc().gui.screen();
            if(currScreen == null) { return; }

            String InventoryTitle = currScreen.getTitle().getString();
            inRaidChest = InventoryTitle.equals("\uDAFF\uDFEA\uE00E");

            // Handle queue click in the same listener instead of registering new ones
            if (waitingForQueue) {
                if (MinecraftUtils.player() == null || client.gui.screen() == null) return;

                AbstractContainerMenu menu = MinecraftUtils.containerMenu();
                if (menu == null || menu.slots.size() < 50) return;

                Slot slot = menu.getSlot(49);
                if (slot == null || slot.getItem() == null || slot.getItem().getCustomName() == null) return;

                if (slot.getItem().getCustomName().getString().contains("Queue")) {
                    clickOnSlot(49, MinecraftUtils.containerMenu().containerId, 0, MinecraftUtils.containerMenu().getItems());
                    waitingForQueue = false;
                }
            }
        });
    }

    public static void notifyClick() {
        if(inRaidChest) {
            Minecraft client = Minecraft.getInstance();
            if(client.player == null || client.level == null || client.gui.screen() == null || client.getConnection() == null)
            { return; }
            AbstractContainerMenu currScreenHandler = MinecraftUtils.containerMenu();
            if(currScreenHandler == null) { return; }
            MinecraftUtils.sendChat("/partyfinder");
            waitingForQueue = true;
        }
    }
}

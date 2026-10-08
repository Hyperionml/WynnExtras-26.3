package julianh06.wynnextras.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import julianh06.wynnextras.compat.client.InputCompat;

public class MinecraftUtils {
    public static LocalPlayer localPlayer() {
        LocalPlayer localPlayer = localPlayerOrNull();
        if (localPlayer == null) {
            throw new IllegalStateException("Local player is null, this should not happen!");
        }
        return localPlayer;
    }

    public static LocalPlayer localPlayerOrNull() {
        return Minecraft.getInstance().player;
    }

    public static boolean isLocalPlayer(Entity entity) {
        return entity != null && entity.equals(localPlayerOrNull());
    }

    public static boolean localPlayerExists() {
        return localPlayerOrNull() != null;
    }

    public static ClientLevel localWorld() {
        ClientLevel localWorld = localWorldOrNull();
        if (localWorld == null) {
            throw new IllegalStateException("Local world is null, this should not happen!");
        }
        return localWorld;
    }

    public static ClientLevel localWorldOrNull() {
        return Minecraft.getInstance().level;
    }

    public static boolean localWorldExists() {
        return localWorldOrNull() != null;
    }

    public static boolean isOnWynncraft() {
        Minecraft client = mc();
        if (client.player == null || client.level == null || client.getCurrentServer() == null) return false;
        String address = client.getCurrentServer().ip;
        int portSeparator = address.indexOf(':');
        if (portSeparator >= 0) address = address.substring(0, portSeparator);
        String host = address.toLowerCase(java.util.Locale.ROOT);
        return host.equals("wynncraft.com") || host.endsWith(".wynncraft.com");
    }

    public static boolean isControlDown() {
        long window = mc().getWindow().handle();
        return InputCompat.keyState(window, InputCompat.KEY_LEFT_CONTROL) == InputCompat.PRESS
                || InputCompat.keyState(window, InputCompat.KEY_RIGHT_CONTROL) == InputCompat.PRESS;
    }

    // TODO: This is temporary and should be replaced with a wrapper around the network handler
    public static ClientPacketListener localNetworkHandler() {
        ClientPacketListener localNetworkHandler = localNetworkHandlerOrNull();
        if (localNetworkHandler == null) {
            throw new IllegalStateException("Local network handler is null, this should not happen!");
        }
        return localNetworkHandler;
    }

    public static ClientPacketListener localNetworkHandlerOrNull() {
        return Minecraft.getInstance().getConnection();
    }

    // TODO: This is temporary and should be replaced with a wrapper around the MinecraftClient instance
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return localPlayer();
    }

    public static String playerName() {
        return player().getGameProfile().name();
    }

    public static AbstractContainerMenu containerMenu() {
        return player().containerMenu;
    }

    public static Inventory inventory() {
        return player().getInventory();
    }

    public static Screen screen() {
        return mc().gui.screen();
    }

    public static void setScreen(Screen screen) {
        mc().gui.setScreen(screen);
    }

    public static double guiScale() {
        return mc().getWindow().getGuiScale();
    }

    public static void sendMessageToClient(Component message) {
        LocalPlayer player = localPlayerOrNull();
        if (player != null) julianh06.wynnextras.compat.client.ClientMessages.display(message, false);
    }

    public static void sendChat(String message) {
        ClientPacketListener handler = localNetworkHandlerOrNull();
        if (handler != null) handler.sendChat(message);
    }

    public static void playSoundUI(SoundEvent sound) {
        playSoundUI(sound, 1f);
    }

    public static void playSoundUI(SoundEvent sound, float volume) {
        LocalPlayer player = localPlayerOrNull();
        if (player != null) player.playSound(sound, volume, 1f);
    }

    public static void playSoundUI(SoundEvent sound, float volume, float pitch) {
        mc().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public static void playSoundAmbient(SoundEvent sound) {
        playSoundAmbient(sound, 1f, 1f);
    }

    public static void playSoundAmbient(SoundEvent sound, float volume, float pitch) {
        LocalPlayer player = localPlayerOrNull();
        if (player != null) player.playSound(sound, volume, pitch);
    }
}

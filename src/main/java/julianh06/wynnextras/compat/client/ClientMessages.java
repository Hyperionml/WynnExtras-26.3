package julianh06.wynnextras.compat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientMessages {
    private ClientMessages() {}

    public static void display(Component message, boolean overlay) {
        var listener = Minecraft.getInstance().gui.chatListener();
        if (overlay) listener.handleOverlay(message);
        else listener.handleSystemMessage(message, false);
    }
}

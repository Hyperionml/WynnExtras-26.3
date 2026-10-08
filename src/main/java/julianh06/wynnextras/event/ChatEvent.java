package julianh06.wynnextras.event;

import julianh06.wynnextras.event.api.WEEvent;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;
import julianh06.wynnextras.features.chat.ChatManager;

public class ChatEvent extends WEEvent {
    private static boolean registered = false;
    public Component message;

    public ChatEvent(Component Message) {
        this.message = Message;
    }

    public static void register() {
        if (registered) return;
        registered = true;

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                new ChatEvent(message).post());
        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                new ChatEvent(message).post());
    }

    public Component getProcessedMessage() {
        return Component.literal(ChatManager.processMessageForSend(message.getString()));
    }
}

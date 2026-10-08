package julianh06.wynnextras.core.command;

import julianh06.wynnextras.features.chat.ChatManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ChatCommands {
    public static LiteralArgumentBuilder<FabricClientCommandSource> register() {
        return register("chat");
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> registerAlias() {
        return register("Chat");
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> register(String base) {
        return ClientCommands.literal(base)
                .then(ClientCommands.literal("p")
                        .executes(c -> {
                            ChatManager.setCurrentChannel(ChatManager.ChatChannel.PARTY);
                            return 1;
                        }))
                .then(ClientCommands.literal("g")
                        .executes(c -> {
                            ChatManager.setCurrentChannel(ChatManager.ChatChannel.GUILD);
                            return 1;
                        }))
                .then(ClientCommands.literal("a")
                        .executes(c -> {
                            ChatManager.setCurrentChannel(ChatManager.ChatChannel.ALL);
                            return 1;
                        }));
    }
}

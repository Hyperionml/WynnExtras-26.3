package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.qol.StackDuplicateMessages;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import julianh06.wynnextras.features.chat.mediapreview.ChatMediaPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatHudAddMessageMixin {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private Component modifyMessage(Component message) {
        return ChatMediaPreview.processMessage(StackDuplicateMessages.process(message));
    }
}

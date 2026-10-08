package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.chat.ChatManager;
import julianh06.wynnextras.features.chat.mediapreview.ChatMediaPreview;
import julianh06.wynnextras.features.misc.ProfessionOverlay;
import julianh06.wynnextras.features.shoppinglist.ui.ShoppingListMenuExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void renderProfessionOverlay(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ProfessionOverlay.renderOnScreen(context);
        ChatMediaPreview.render(context, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMediaPreviewClick(MouseButtonEvent click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (ChatMediaPreview.mouseClicked(click)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void consumeShoppingListEditorKey(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (ShoppingListMenuExtension.isEditorTextInputFocused()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
        if (message == null || message.isEmpty() || ChatManager.getCurrentChannel() == ChatManager.ChatChannel.ALL) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if(message.matches("^/a\\s.*")) {
            message = message.substring(2);
            player.connection.sendChat(message);
            mc.gui.hud.getChat().addRecentChat(message);
            ci.cancel();
            return;
        }
        if(message.matches("^/ac\\s.*")) {
            message = message.substring(3);
            player.connection.sendChat(message);
            mc.gui.hud.getChat().addRecentChat(message);
            ci.cancel();
            return;
        }

        if (message.startsWith("/")) return;

        String processed = ChatManager.processMessageForSend(message);
        player.connection.sendChat(processed);

        if (addToHistory) {
            mc.gui.hud.getChat().addRecentChat(message);
        }

        ci.cancel();
    }
}

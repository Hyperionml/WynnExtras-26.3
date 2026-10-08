package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.shoppinglist.ui.ShoppingListMenuExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public class TextFieldWidgetMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void consumeShoppingListEditorCharacter(CharacterEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().gui.screen() instanceof ChatScreen
                && ShoppingListMenuExtension.isEditorTextInputFocused()) {
            cir.setReturnValue(true);
        }
    }
}

package julianh06.wynnextras.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import julianh06.wynnextras.compat.client.InputCompat;
import julianh06.wynnextras.event.KeyInputEvent;
import julianh06.wynnextras.event.CharInputEvent;
import julianh06.wynnextras.features.inventory.BankOverlay;
import julianh06.wynnextras.features.inventory.BankOverlayType;
import julianh06.wynnextras.features.bankoverlay.BankOverlay2;
import julianh06.wynnextras.features.shoppinglist.ui.ShoppingListMenuExtension;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
 @Inject(method="keyPress", at=@At("HEAD"), cancellable=true, require=1)
 private void wynnextras$keyPress(long window, int nativeAction, KeyEvent event, CallbackInfo ci) {
  Minecraft client = Minecraft.getInstance();
  if (window != client.getWindow().handle()) return;
  int key=InputCompat.legacyKey(event.key());
  int action=nativeAction==InputConstants.REPEAT?InputCompat.REPEAT:nativeAction;
  int modifiers=InputCompat.legacyModifiers(event.modifiers());
  if (ShoppingListMenuExtension.handleGlobalKeyInput(key,event.keycode(),action,modifiers)) { ci.cancel(); return; }
  new KeyInputEvent(key,event.keycode(),action,modifiers).post();
  if (BankOverlay.currentOverlayType != BankOverlayType.NONE &&
      ((BankOverlay2.isAnyTextInputFocused() && client.options.keyInventory.matches(event)) ||
       (InputCompat.KEY_1<=key && key<=InputCompat.KEY_9))) ci.cancel();
 }
 @Inject(method="charTyped", at=@At("HEAD"), cancellable=true, require=1)
 private void wynnextras$charTyped(long window, CharacterEvent event, CallbackInfo ci) {
  if(window != Minecraft.getInstance().getWindow().handle()) return;
  char character=(char)event.codepoint();
  if(ShoppingListMenuExtension.handleGlobalCharTyped(character) || BankOverlay.handleScreenCharTyped(character)) { ci.cancel(); return; }
  new CharInputEvent(character).post();
 }
}

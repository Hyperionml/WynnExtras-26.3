package julianh06.wynnextras.mixin;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.misc.GuildRaidBlockOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import julianh06.wynnextras.compat.client.InputCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class PartyFinderClickBlockMixin {
    private static final String PARTY_FINDER_TITLE = "\uDAFF\uDFE1\uE00C";

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V",
            at = @At("HEAD"), cancellable = true)
    private void blockGuildRaidClick(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci) {
        if (!WynnExtrasConfig.INSTANCE.shiftDisableGuildRaid) return;
        if (slot == null || !slot.hasItem()) return;

        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        if (!self.getTitle().getString().equals(PARTY_FINDER_TITLE)) return;

        String itemNameLower = slot.getItem().getHoverName().getString().toLowerCase();
        if (!itemNameLower.contains("guild raid")) return;

        long window = Minecraft.getInstance().getWindow().handle();
        boolean shiftHeld = InputCompat.keyState(window, InputCompat.KEY_LEFT_SHIFT) == InputCompat.PRESS
                || InputCompat.keyState(window, InputCompat.KEY_RIGHT_SHIFT) == InputCompat.PRESS;

        if (!shiftHeld) {
            ci.cancel();
            GuildRaidBlockOverlay.trigger();
        }
    }
}

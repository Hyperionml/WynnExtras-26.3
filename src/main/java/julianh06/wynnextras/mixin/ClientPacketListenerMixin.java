package julianh06.wynnextras.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import julianh06.wynnextras.event.SetEntityDataEvent;
import julianh06.wynnextras.features.bankoverlay.BankOverlay2;
import julianh06.wynnextras.features.chat.ChatManager;
import julianh06.wynnextras.features.misc.ServerTpsHud;
import julianh06.wynnextras.wynncraft.state.RaidState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void wynnExtras$measureServerTps(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerTpsHud.onWorldTimeUpdate();
    }

    @Inject(method = "setTitleText", at = @At("TAIL"))
    private void wynnExtras$observeRaidTitle(ClientboundSetTitleTextPacket packet, CallbackInfo ci) {
        RaidState.observeTitle(packet.text());
    }

    @Inject(
            method = "handleSystemChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                    shift = At.Shift.AFTER))
    private void wynnExtras$observeGameMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        ChatManager.observeIncomingMessage(packet.content());
    }

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    private void wynnExtras$continueBankPageJump(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        BankOverlay2.onBankContainerUpdate(packet.containerId(), packet.stateId(), -1);
        BankOverlay2.onBankPageNavigationUpdate(packet.containerId(), packet.stateId(), -1);
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void wynnExtras$continueBankPageJump(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        BankOverlay2.onBankContainerUpdate(packet.getContainerId(), packet.getStateId(), packet.getSlot());
        if (packet.getSlot() == 51 || packet.getSlot() == 52) {
            BankOverlay2.onBankPageNavigationUpdate(packet.getContainerId(), packet.getStateId(), packet.getSlot());
        }
    }

    @ModifyArg(
            method = "handleSetEntityData(Lnet/minecraft/network/protocol/game/ClientboundSetEntityDataPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V"),
            index = 0)
    private List<SynchedEntityData.DataValue<?>> handleSetEntityDataPre(
            List<SynchedEntityData.DataValue<?>> packedItems,
            @Local(argsOnly = true) ClientboundSetEntityDataPacket packet) {
        if (!isRenderThread()) return packedItems;

        SetEntityDataEvent event = new SetEntityDataEvent(packet);
        event.post();
        return event.getPackedItems();
    }

    @Unique
    private static boolean isRenderThread() {
        return Minecraft.getInstance().isSameThread();
    }
}

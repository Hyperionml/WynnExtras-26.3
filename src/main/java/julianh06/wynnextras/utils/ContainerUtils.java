package julianh06.wynnextras.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ContainerUtils {
    private ContainerUtils() {}



    public static void clickOnSlot(int slot, int syncId, int mouseButton, List<ItemStack> stacks) {
        click(slot, syncId, 0, mouseButton, ContainerInput.PICKUP, stacks);
    }

    public static void clickOnSlot(int slot, int syncId, int revision, int mouseButton, List<ItemStack> stacks) {
        click(slot, syncId, revision, mouseButton, ContainerInput.PICKUP, stacks);
    }

    public static void shiftClickOnSlot(int slot, int syncId, int mouseButton, List<ItemStack> stacks) {
        click(slot, syncId, 0, mouseButton, ContainerInput.QUICK_MOVE, stacks);
    }

    public static void shiftClickOnSlot(int slot, int syncId, int revision, int mouseButton, List<ItemStack> stacks) {
        click(slot, syncId, revision, mouseButton, ContainerInput.QUICK_MOVE, stacks);
    }

    public static void pressKeyOnSlot(int slot, int syncId, int hotbarKey, List<ItemStack> stacks) {
        click(slot, syncId, 0, hotbarKey, ContainerInput.SWAP, stacks);
    }

    public static void pressKeyOnSlot(int slot, int syncId, int revision, int hotbarKey, List<ItemStack> stacks) {
        click(slot, syncId, revision, hotbarKey, ContainerInput.SWAP, stacks);
    }

    public static boolean clickOnSlotWithoutPrediction(int slot, int syncId, int revision, int mouseButton, List<ItemStack> stacks) {
        if (MinecraftUtils.mc() == null || stacks == null) return false;
        if (MinecraftUtils.mc().getConnection() == null || slot < 0 || slot >= stacks.size()) return false;
        try {
            HashedPatchMap.HashGenerator hasher = MinecraftUtils.mc().getConnection().decoratedHashOpsGenenerator();
            Int2ObjectOpenHashMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<>();
            modifiedStacks.put(slot, HashedStack.create(stacks.get(slot), hasher));
            MinecraftUtils.mc().getConnection().send(new ServerboundContainerClickPacket(
                    syncId,
                    revision,
                    (short) slot,
                    (byte) mouseButton,
                    ContainerInput.PICKUP,
                    modifiedStacks,
                    HashedStack.EMPTY
            ));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void click(int slot, int syncId, int revision, int button, ContainerInput action, List<ItemStack> stacks) {
        if (MinecraftUtils.mc() == null || stacks == null) return;
        if (MinecraftUtils.mc().getConnection() == null || slot < 0 || slot >= stacks.size()) return;
        try {
            HashedPatchMap.HashGenerator hasher = MinecraftUtils.mc().getConnection().decoratedHashOpsGenenerator();
            Int2ObjectOpenHashMap<HashedStack> modifiedStacks = new Int2ObjectOpenHashMap<HashedStack>();
            modifiedStacks.put(slot, HashedStack.create(ItemStack.EMPTY, hasher));
            HashedStack clickedStack = HashedStack.create(stacks.get(slot), hasher);
            MinecraftUtils.mc().getConnection().send(new ServerboundContainerClickPacket(
                    syncId,
                    revision,
                    (short) slot,
                    (byte) button,
                    action,
                    modifiedStacks,
                    clickedStack
            ));
        } catch (Throwable ignored) {}
    }
}

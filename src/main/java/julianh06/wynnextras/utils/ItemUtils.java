package julianh06.wynnextras.utils;

import julianh06.wynnextras.wynncraft.item.WynnItemEncoding;
import julianh06.wynnextras.wynncraft.item.WynnItemParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import java.util.List;

public class ItemUtils {
    public static String getTier(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return WynnItemParser.parse(itemStack).map(item -> item.tier().name()).orElse(null);
    }

    public static boolean isTier(ItemStack itemStack, String tier) {
        String itemTier = getTier(itemStack);
        return itemTier != null && itemTier.equals(tier);
    }

    public static String itemStackToItemString(ItemStack itemStack) {
        return WynnItemParser.parse(itemStack).flatMap(WynnItemEncoding::encode).orElse(null);
    }

    public static Float getFirsCustomModelDataFloat(ItemStack itemStack) {
        CustomModelData modelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (modelData == null) return null;
        List<Float> floats = modelData.floats();
        if (floats == null || floats.isEmpty()) return null;
        return floats.getFirst();
    }
}

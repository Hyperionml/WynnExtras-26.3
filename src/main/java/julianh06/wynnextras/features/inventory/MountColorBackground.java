package julianh06.wynnextras.features.inventory;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.utils.render.RenderUtils;
import julianh06.wynnextras.wynncraft.item.MountColorParser;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class MountColorBackground {
    private record CacheEntry(ItemLore lore, CustomModelData modelData,
                              int count, ItemStack renderedStack) {}

    private static final Identifier TIER_BACKGROUND =
            Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/gui/layer/tier/full.png");
    private static final Map<ItemStack, CacheEntry> CACHE = new WeakHashMap<>();

    private MountColorBackground() {}

    public static ItemStack apply(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;

        ItemLore lore = stack.get(DataComponents.LORE);
        CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        synchronized (CACHE) {
            CacheEntry cached = CACHE.get(stack);
            if (cached != null && cached.lore() == lore
                    && cached.modelData() == modelData && cached.count() == stack.getCount()) {
                return cached.renderedStack();
            }
        }

        return MountColorParser.parse(stack).map(colors -> {
            List<String> strings = modelData == null ? List.of() : modelData.strings();
            if (strings.isEmpty() || strings.getFirst().isEmpty()) return stack;

            ItemStack copy = stack.copy();
            List<String> withRarity = new ArrayList<>(strings);
            withRarity.set(0, "");
            copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                    modelData == null ? List.of() : modelData.floats(),
                    modelData == null ? List.of() : modelData.flags(),
                    withRarity,
                    modelData == null ? List.of() : modelData.colors()
            ));
            synchronized (CACHE) {
                CACHE.put(stack, new CacheEntry(lore, modelData, stack.getCount(), copy));
            }
            return copy;
        }).orElse(stack);
    }

    public static void draw(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
        if (!WynnExtrasConfig.INSTANCE.mountPrimaryColorBackground) return;

        MountColorParser.parse(stack).ifPresent(colors -> {
            RenderUtils.drawTexturedRect(context, TIER_BACKGROUND, colors.primaryColor(),
                    x - 8, y - 8, 32, 32, 32, 32);
            context.nextStratum();
        });
    }
}

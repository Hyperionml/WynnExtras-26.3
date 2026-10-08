package julianh06.wynnextras.features.shoppinglist.ui;

import julianh06.wynnextras.features.shoppinglist.model.RequirementType;
import julianh06.wynnextras.features.shoppinglist.service.ShoppingListHaveCount;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public record ShoppingListRowDetail(
        String displayNameWithTier,
        String typeLabel,
        int have,
        int need,
        int inventory,
        int accountBank,
        int characterBank,
        int miscBucket,
        boolean bankCacheAvailable,
        boolean bankCachePossiblyIncomplete,
        String tradeMarketQuery) {
    public ShoppingListRowDetail {
        displayNameWithTier = displayNameWithTier == null ? "" : displayNameWithTier;
        typeLabel = typeLabel == null ? "" : typeLabel;
        have = Math.max(0, have);
        need = Math.max(0, need);
        inventory = Math.max(0, inventory);
        accountBank = Math.max(0, accountBank);
        characterBank = Math.max(0, characterBank);
        miscBucket = Math.max(0, miscBucket);
        tradeMarketQuery = tradeMarketQuery == null ? "" : tradeMarketQuery;
    }

    public static ShoppingListRowDetail from(ShoppingListFormatter.Row row, ShoppingListHaveCount count) {
        ShoppingListHaveCount safeCount = count == null ? new ShoppingListHaveCount(0, 0, 0, false, false) : count;
        return new ShoppingListRowDetail(
                row.displayNameWithTier(),
                row.type() == RequirementType.MATERIAL ? "Material" : "Ingredient",
                safeCount.total(),
                row.needCount(),
                safeCount.inventory(),
                safeCount.accountBank(),
                safeCount.characterBank(),
                safeCount.miscBucket(),
                safeCount.bankCacheAvailable(),
                safeCount.bankCachePossiblyIncomplete(),
                row.tradeMarketQuery());
    }

    public List<String> tooltipLines() {
        return tooltipLines(ShoppingListMenuRenderPolicy.ShoppingListRowPrimaryAction.TRADE_MARKET_SEARCH);
    }

    public List<String> tooltipLines(ShoppingListMenuRenderPolicy.ShoppingListRowPrimaryAction primaryAction) {
        return tooltipText(primaryAction).stream().map(Component::getString).toList();
    }

    public List<Component> tooltipText(ShoppingListMenuRenderPolicy.ShoppingListRowPrimaryAction primaryAction) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(displayNameWithTier).withStyle(ChatFormatting.AQUA));
        lines.add(quantityLine("Have", have, ChatFormatting.GREEN));
        lines.add(quantityLine("Need", need, ChatFormatting.RED));
        if (inventory > 0) lines.add(sourceLine("Inventory", inventory));
        if (accountBank > 0) lines.add(sourceLine("Account Bank", accountBank));
        if (characterBank > 0) lines.add(sourceLine("Character Bank", characterBank));
        if (miscBucket > 0) lines.add(sourceLine("Misc Bucket", miscBucket));
        String cacheStatus = bankCacheStatusLine();
        if (cacheStatus != null) {
            lines.add(Component.literal(cacheStatus).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.literal(switch (primaryAction) {
            case BANK_OVERLAY_SEARCH -> "Left-click: Search in Bank Overlay";
            case TRADE_MARKET_SEARCH -> "Left-click: Search in Trade Market";
            case COPY_ONLY -> "Left-click/Right-click: Copy";
            case NONE -> "Left-click: Unavailable";
        }).withStyle(ChatFormatting.GRAY));
        if(primaryAction != ShoppingListMenuRenderPolicy.ShoppingListRowPrimaryAction.COPY_ONLY) lines.add(Component.literal("Right-click: Copy").withStyle(ChatFormatting.GRAY));
        return List.copyOf(lines);
    }

    private static Component quantityLine(String label, int count, ChatFormatting countColor) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(Integer.toString(count)).withStyle(countColor))
                .append(Component.literal(" (" + ShoppingListQuantityFormatter.formatStackBreakdown(count) + ")")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static Component sourceLine(String label, int count) {
        return Component.literal(label + ": ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(Integer.toString(count)).withStyle(ChatFormatting.WHITE));
    }

    public String bankCacheStatusLine() {
        if (!bankCacheAvailable) {
            return "Bank cache unavailable.";
        }
        if (bankCachePossiblyIncomplete) {
            return "Bank cache may be incomplete.";
        }
        return null;
    }
}

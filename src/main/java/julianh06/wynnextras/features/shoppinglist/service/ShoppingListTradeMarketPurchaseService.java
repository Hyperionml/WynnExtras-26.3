package julianh06.wynnextras.features.shoppinglist.service;

import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.shoppinglist.ShoppingListFeature;
import julianh06.wynnextras.features.shoppinglist.cart.ShoppingEntry;
import julianh06.wynnextras.features.shoppinglist.model.RequirementType;
import julianh06.wynnextras.features.shoppinglist.util.ShoppingListMaterialNameNormalizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import julianh06.wynnextras.compat.client.InputCompat;

import java.util.Map;

public final class ShoppingListTradeMarketPurchaseService {
    private static final String PURCHASE_MENU_TITLE_PREFIX = "\uDAFF\uDFE8\uE015";
    private static final int PURCHASE_ITEM_SLOT = 22;
    private static final int AMOUNT_SLOT = 31;
    private static final long PENDING_CONTEXT_TIMEOUT_MS = 5_000L;
    private static final ShoppingListHaveCountService HAVE_COUNT_SERVICE = new ShoppingListHaveCountService();

    private static PurchaseContext pendingContext;
    private static long pendingContextCreatedAtMs;
    private static ChatScreen amountPromptScreen;

    private ShoppingListTradeMarketPurchaseService() {}

    public static void handleAmountSlotClick(AbstractContainerScreen<?> screen, Slot focusedSlot, int button) {
        if (button != InputCompat.MOUSE_BUTTON_LEFT || screen == null || screen.getTitle() == null
                || !screen.getTitle().getString().startsWith(PURCHASE_MENU_TITLE_PREFIX)
                || focusedSlot == null || focusedSlot.index != AMOUNT_SLOT) {
            clear();
            return;
        }

        pendingContext = findContext(screen);
        pendingContextCreatedAtMs = pendingContext == null ? 0L : System.currentTimeMillis();
        amountPromptScreen = null;
    }

    public static PurchaseContext currentContext() {
        Minecraft client = Minecraft.getInstance();
        if (pendingContext == null || client == null) {
            return null;
        }
        if (amountPromptScreen != null) {
            if (client.gui.screen() == amountPromptScreen) {
                return pendingContext;
            }
            clear();
            return null;
        }
        if (!(client.gui.screen() instanceof ChatScreen chatScreen)) {
            if (System.currentTimeMillis() - pendingContextCreatedAtMs > PENDING_CONTEXT_TIMEOUT_MS) {
                clear();
            }
            return null;
        }
        if (System.currentTimeMillis() - pendingContextCreatedAtMs > PENDING_CONTEXT_TIMEOUT_MS) {
            clear();
            return null;
        }
        amountPromptScreen = chatScreen;
        return pendingContext;
    }

    public static void clearContext() {
        clear();
    }

    public static boolean submitAmount(int amount) {
        PurchaseContext context = currentContext();
        Minecraft client = Minecraft.getInstance();
        if (context == null || amount <= 0 || client == null || client.player == null
                || client.player.connection == null) {
            return false;
        }

        String message = Integer.toString(amount);
        client.player.connection.sendChat(message);
        if (client.gui != null) {
            client.gui.hud.getChat().addRecentChat(message);
        }
        clear();
        client.gui.setScreen(null);
        return true;
    }

    private static PurchaseContext findContext(AbstractContainerScreen<?> screen) {
        if (screen.getMenu() == null || screen.getMenu().slots.size() <= PURCHASE_ITEM_SLOT) {
            return null;
        }
        ItemStack purchaseStack = screen.getMenu().slots.get(PURCHASE_ITEM_SLOT).getItem();
        if (purchaseStack == null || purchaseStack.isEmpty()) {
            return null;
        }

        ShoppingListHaveCountService.StackSnapshot purchaseSnapshot = HAVE_COUNT_SERVICE.snapshot(purchaseStack);
        if (purchaseSnapshot == null) {
            return null;
        }
        for (Map.Entry<ShoppingEntry, Integer> cartEntry : ShoppingListFeature.shoppingCartService().cart().entries().entrySet()) {
            ShoppingEntry entry = cartEntry.getKey();
            if (!ShoppingListHaveCountService.matches(entry, purchaseSnapshot)) {
                continue;
            }
            int needed = ShoppingListRequirementCalculator.adjustedRequired(
                    entry,
                    cartEntry.getValue(),
                    WynnExtrasConfig.INSTANCE.shoppingListCraftMultiplier,
                    WynnExtrasConfig.INSTANCE.shoppingListProfessionSpeed);
            int have = HAVE_COUNT_SERVICE.count(entry).total();
            return new PurchaseContext(displayName(entry), needed, have);
        }
        return null;
    }

    private static String displayName(ShoppingEntry entry) {
        String cleanedName = ShoppingListTextCleaner.clean(entry.displayName());
        if (entry.type() != RequirementType.MATERIAL) {
            return cleanedName;
        }
        return ShoppingListMaterialNameNormalizer.baseName(cleanedName) + " T" + entry.materialTier();
    }

    private static void clear() {
        pendingContext = null;
        pendingContextCreatedAtMs = 0L;
        amountPromptScreen = null;
    }

    public record PurchaseContext(String itemName, int needed, int have) {
        public int remaining() {
            return Math.max(0, needed - have);
        }

        public boolean needsRemainingButton() {
            return have < needed;
        }
    }
}

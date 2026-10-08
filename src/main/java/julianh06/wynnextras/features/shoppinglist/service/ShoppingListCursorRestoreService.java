package julianh06.wynnextras.features.shoppinglist.service;

import julianh06.wynnextras.features.shoppinglist.ui.ShoppingListScreenContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import julianh06.wynnextras.compat.client.InputCompat;

public final class ShoppingListCursorRestoreService {
    private static final long RESTORE_TIMEOUT_MS = 10_000L;

    private static PendingRestore pendingRestore;

    private ShoppingListCursorRestoreService() {}

    public static void recordBeforeShoppingListTradeMarketSearch() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return;
        }

        try {
            double[] x = new double[1];
            double[] y = new double[1];
            InputCompat.cursorPosition(client.getWindow().handle(), x, y);
            pendingRestore = new PendingRestore(
                    x[0],
                    y[0],
                    false,
                    0L,
                    ScreenKey.none(),
                    false,
                    ScreenKey.none());
        } catch (RuntimeException ignored) {
            pendingRestore = null;
        }
    }

    public static boolean armForSearchResults() {
        if (pendingRestore == null) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        pendingRestore = pendingRestore.arm(System.currentTimeMillis(), screenKey(client));
        return true;
    }

    public static TickResult tick(Minecraft client) {
        if (client == null || client.getWindow() == null || pendingRestore == null) {
            return TickResult.NONE;
        }

        long now = System.currentTimeMillis();
        try {
            ScreenKey currentScreenKey = screenKey(client);
            boolean tradeMarketVisible = isTradeMarketScreen(client);
            boolean screenChanged = !currentScreenKey.equals(pendingRestore.screenAtSubmission());
            boolean stableResultsScreen = currentScreenKey.equals(pendingRestore.candidateResultsScreen());
            RestoreDecision decision = restoreDecision(
                    now,
                    pendingRestore.armedAtMs(),
                    pendingRestore.armedForResults(),
                    tradeMarketVisible,
                    pendingRestore.sawIntermediateScreen(),
                    screenChanged,
                    stableResultsScreen);
            switch (decision) {
                case RESTORE -> {
                    InputCompat.setCursorPosition(client.getWindow().handle(), pendingRestore.x(), pendingRestore.y());
                    pendingRestore = null;
                    return TickResult.RESTORED;
                }
                case SKIP_TIMEOUT -> {
                    pendingRestore = null;
                    return TickResult.TIMED_OUT;
                }
                case WAIT -> pendingRestore = pendingRestore.withObservedScreen(
                        tradeMarketVisible,
                        currentScreenKey,
                        pendingRestore.sawIntermediateScreen() || !tradeMarketVisible);
            }
        } catch (RuntimeException ignored) {
            pendingRestore = null;
        }
        return TickResult.NONE;
    }

    static RestoreDecision restoreDecision(long nowMs,
                                           long armedAtMs,
                                           boolean armedForResults,
                                           boolean tradeMarketVisible,
                                           boolean sawIntermediateScreen,
                                           boolean screenChangedFromSubmission,
                                           boolean stableResultsScreen) {
        if (!armedForResults) {
            return RestoreDecision.WAIT;
        }
        if (nowMs - armedAtMs > RESTORE_TIMEOUT_MS) {
            return RestoreDecision.SKIP_TIMEOUT;
        }
        if (!tradeMarketVisible) {
            return RestoreDecision.WAIT;
        }
        if (!sawIntermediateScreen && !screenChangedFromSubmission) {
            return RestoreDecision.WAIT;
        }
        if (!stableResultsScreen) {
            return RestoreDecision.WAIT;
        }
        return RestoreDecision.RESTORE;
    }

    public static void clearPending() {
        pendingRestore = null;
    }

    private static boolean isTradeMarketScreen(Minecraft client) {
        if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }
        ShoppingListScreenContext context = ShoppingListScreenContext.detect(screen, false);
        return context == ShoppingListScreenContext.TRADE_MARKET
                || context == ShoppingListScreenContext.TRADE_MARKET_FILTER
                || context == ShoppingListScreenContext.TRADE_MARKET_DETAIL;
    }

    enum RestoreDecision {
        WAIT,
        RESTORE,
        SKIP_TIMEOUT
    }

    enum TickResult {
        NONE,
        RESTORED,
        TIMED_OUT
    }

    private static ScreenKey screenKey(Minecraft client) {
        if (client == null || client.gui.screen() == null) {
            return ScreenKey.none();
        }
        int syncId = -1;
        if (client.gui.screen() instanceof AbstractContainerScreen<?> handledScreen
                && handledScreen.getMenu() != null) {
            syncId = handledScreen.getMenu().containerId;
        }
        return new ScreenKey(
                client.gui.screen().getClass().getName(),
                System.identityHashCode(client.gui.screen()),
                syncId);
    }

    private record PendingRestore(
            double x,
            double y,
            boolean armedForResults,
            long armedAtMs,
            ScreenKey screenAtSubmission,
            boolean sawIntermediateScreen,
            ScreenKey candidateResultsScreen) {
        PendingRestore arm(long nowMs, ScreenKey screenAtSubmission) {
            return new PendingRestore(
                    x,
                    y,
                    true,
                    nowMs,
                    screenAtSubmission,
                    false,
                    ScreenKey.none());
        }

        PendingRestore withObservedScreen(boolean tradeMarketVisible,
                                          ScreenKey currentScreen,
                                          boolean sawIntermediateScreen) {
            ScreenKey candidate = tradeMarketVisible ? currentScreen : ScreenKey.none();
            return new PendingRestore(
                    x,
                    y,
                    armedForResults,
                    armedAtMs,
                    screenAtSubmission,
                    sawIntermediateScreen,
                    candidate);
        }
    }

    private record ScreenKey(String screenClass, int screenIdentity, int syncId) {
        static ScreenKey none() {
            return new ScreenKey("", 0, -1);
        }
    }
}

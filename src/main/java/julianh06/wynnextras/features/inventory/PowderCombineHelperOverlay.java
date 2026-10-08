package julianh06.wynnextras.features.inventory;

import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.utils.ContainerUtils;
import julianh06.wynnextras.utils.HandledScreenAccess;
import julianh06.wynnextras.utils.TickScheduler;
import julianh06.wynnextras.utils.UI.WEMenuExtension;
import julianh06.wynnextras.utils.UI.Widget;
import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.wynncraft.menu.MenuType;
import julianh06.wynnextras.wynncraft.menu.WynncraftMenuService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PowderCombineHelperOverlay extends WEMenuExtension {
    private static final int POWDER_MASTER_TAB_SLOT = 18;
    private static final int ITEM_UPGRADER_TAB_SLOT = 16;
    private static final int FIRST_INPUT_SLOT = 11;
    private static final int LAST_INPUT_SLOT = 14;
    private static final int OUTPUT_SLOT = 15;
    private static final int COMBINE_SLOT = 17;
    private static final String EMPTY_INPUT_NAME = "Empty Powder Slot";
    private static final String EMPTY_OUTPUT_NAME = "No Result";
    private static final int BUTTON_HEIGHT = 14;
    private static final int ROW_GAP = 2;
    private static final int HEADER_HEIGHT = 16;
    private static final int PANEL_PADDING = 4;
    private static final long ACTION_DELAY_MS = 180;
    private static final long PHASE_TIMEOUT_MS = 5_000;
    private static final long COMBINE_RESULT_TIMEOUT_MS = 15_000;
    private static final long TASK_TIMEOUT_MS = 20_000;
    private static final String[] TIER_NAMES = {"I", "II", "III", "IV", "V", "VI", "VII"};

    private static final Element[] ELEMENTS = {
            new Element("Earth", "✤", false, 0xFF55AA55, 0xFF16351B),
            new Element("Thunder", "✦", false, 0xFFFFFF55, 0xFF3B3715),
            new Element("Water", "\uE004", true, 0xFF55FFFF, 0xFF15353B),
            new Element("Fire", "✹", false, 0xFFFF5555, 0xFF3B1717),
            new Element("Air", "❋", false, 0xFFFFFFFF, 0xFF303030)
    };

    private static CombineTask activeTask;
    private static String status = "";
    private static long statusUntil;

    private final PanelWidget panelWidget = new PanelWidget();
    private final List<ElementHeaderWidget> headerWidgets = new ArrayList<>();
    private final List<PowderButton> buttons = new ArrayList<>();
    private final StatusWidget statusWidget = new StatusWidget();

    public PowderCombineHelperOverlay() {
        rootWidgets.add(panelWidget);
        for (Element element : ELEMENTS) {
            ElementHeaderWidget headerWidget = new ElementHeaderWidget(element);
            headerWidgets.add(headerWidget);
            rootWidgets.add(headerWidget);
            for (int targetTier = 2; targetTier <= 7; targetTier++) {
                PowderButton button = new PowderButton(element, targetTier);
                buttons.add(button);
                rootWidgets.add(button);
            }
        }
        rootWidgets.add(statusWidget);
    }

    public static boolean isSupportedScreen() {
        return WynncraftMenuService.isCurrentAny(MenuType.POWDER_MASTER_REMOVE, MenuType.POWDER_MASTER_COMBINE,
                MenuType.ITEM_UPGRADER, MenuType.ITEM_UPGRADER_COMBINE);
    }

    public static void cancelActiveTask() {
        activeTask = null;
        status = "";
        statusUntil = 0;
    }

    public static void onHandledScreenClosed() {
        if (activeTask == null) return;
        TickScheduler.runAfterTicks(2, () -> {
            boolean supportedScreen = isSupportedScreen();
            if (!supportedScreen) cancelActiveTask();
        });
    }

    @Override
    protected void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (!(Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?> screen) || !isSupportedScreen()) {
            return;
        }

        updateTask(screen);
        updateLayout(screen);
    }

    @Override
    protected void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
    }

    private void updateLayout(AbstractContainerScreen<?> screen) {
        int panelWidth;
        try {
            panelWidth = Math.clamp(HandledScreenAccess.backgroundWidth(screen), 150, screen.width - 4);
        } catch (Exception e) {
            panelWidth = 100;
        }

        int panelX = HandledScreenAccess.x(screen) + (HandledScreenAccess.backgroundWidth(screen) - panelWidth) / 2;
        AbstractContainerMenu handler = screen.getMenu();
        boolean[][] visibleButtons = new boolean[ELEMENTS.length][6];
        int maxVisibleRows = 0;
        for (int column = 0; column < ELEMENTS.length; column++) {
            int visibleRows = 0;
            for (int targetTier = 2; targetTier <= 7; targetTier++) {
                boolean visible = countPowder(handler, ELEMENTS[column], targetTier - 1) >= 4;
                visibleButtons[column][targetTier - 2] = visible;
                if (visible) visibleRows++;
            }
            maxVisibleRows = Math.max(maxVisibleRows, visibleRows);
        }
        boolean transientStatus = !status.isEmpty() && System.currentTimeMillis() < statusUntil;
        boolean noPowdersToCombine = activeTask == null && !transientStatus && maxVisibleRows == 0;
        boolean showStatus = noPowdersToCombine || activeTask != null || transientStatus;
        int panelHeight = HEADER_HEIGHT + maxVisibleRows * (BUTTON_HEIGHT + ROW_GAP) + (showStatus ? 16 : PANEL_PADDING);
        int panelY = HandledScreenAccess.y(screen) + HandledScreenAccess.backgroundHeight(screen) + 12;

        int columnWidth = panelWidth / ELEMENTS.length;
        panelWidget.setBounds(panelX, panelY, panelWidth, panelHeight);
        statusWidget.setBounds(panelX, panelY + panelHeight - 16, panelWidth, 16);
        statusWidget.setVisible(showStatus);
        statusWidget.setText(noPowdersToCombine ? "No powders to combine in your inventory." : status);
        for (int column = 0; column < ELEMENTS.length; column++) {
            headerWidgets.get(column).setBounds(panelX + column * columnWidth, panelY, columnWidth, HEADER_HEIGHT);
            int visibleRow = 0;
            for (int targetTier = 2; targetTier <= 7; targetTier++) {
                PowderButton button = buttons.get(column * 6 + targetTier - 2);
                boolean visible = visibleButtons[column][targetTier - 2];
                int x = panelX + column * columnWidth + PANEL_PADDING;
                int y = panelY + HEADER_HEIGHT + visibleRow * (BUTTON_HEIGHT + ROW_GAP);
                button.setBounds(x, y, columnWidth - PANEL_PADDING * 2, BUTTON_HEIGHT);
                button.setVisible(visible);
                button.setEnabled(activeTask == null);
                if (visible) visibleRow++;
            }
        }
    }

    private static void startTask(Element element, int targetTier) {
        if (activeTask != null || !isSupportedScreen()) {
            return;
        }
        long now = System.currentTimeMillis();
        activeTask = new CombineTask(element, targetTier, Phase.ENSURE_COMBINE_TAB, now, now, 0, 0);
        status = "Combining " + element.name() + " " + TIER_NAMES[targetTier - 1] + "...";
    }

    private static void updateTask(AbstractContainerScreen<?> screen) {
        CombineTask task = activeTask;
        if (task == null) return;

        long now = System.currentTimeMillis();
        if (now - task.startedAt() > TASK_TIMEOUT_MS) {
            failTask("Combine timed out");
            return;
        }

        MenuType menuType = WynncraftMenuService.currentType();
        if (!isSupportedMenuType(menuType)) {
            return;
        }
        if (now - task.phaseStartedAt() < ACTION_DELAY_MS) return;

        AbstractContainerMenu handler = screen.getMenu();
        switch (task.phase()) {
            case ENSURE_COMBINE_TAB -> {
                if (isCombineMenu(menuType)) {
                    if (hasOutput(handler)) {
                        click(handler, OUTPUT_SLOT, ContainerInput.PICKUP);
                        setPhase(Phase.WAIT_EXISTING_OUTPUT, now, 0);
                    } else {
                        setPhase(Phase.SELECT_SOURCE, now, 0);
                    }
                } else if (click(handler, tabSlot(menuType), ContainerInput.PICKUP)) {
                    setPhase(Phase.WAIT_COMBINE_TAB, now, 0);
                }
            }
            case WAIT_COMBINE_TAB -> {
                if (isCombineMenu(menuType)) {
                    setPhase(Phase.ENSURE_COMBINE_TAB, now, 0);
                } else if (phaseTimedOut(task, now)) {
                    failTask("Could not open combine tab");
                }
            }
            case WAIT_EXISTING_OUTPUT -> {
                if (!hasOutput(handler) && handler.getCarried().isEmpty()) {
                    setPhase(Phase.FLUSH_INPUTS, now, 0);
                } else if (!handler.getCarried().isEmpty()) {
                    int destination = findCursorDestination(handler);
                    if (destination >= 0 && click(handler, destination, ContainerInput.PICKUP)) {
                        setPhase(Phase.WAIT_EXISTING_OUTPUT, now, 0);
                    }
                } else if (phaseTimedOut(task, now)) {
                    failTask("Could not collect existing output");
                }
            }
            case SELECT_SOURCE -> {
                int sourceCount = countPowder(handler, task.element(), task.targetTier() - 1);
                if (sourceCount < 4) {
                    finishTask();
                    return;
                }
                setPhase(Phase.WAIT_INPUTS, now, 0);
            }
            case WAIT_INPUTS -> {
                if (inputsFilled(handler)) {
                    if (hasLoreLine(handler, COMBINE_SLOT, "Cannot afford to upgrade")) {
                        failTask("Cannot afford to upgrade");
                        return;
                    }
                    int sourceBeforeCombine = countPowder(handler, task.element(), task.targetTier() - 1);
                    int combineClickRevision = handler.getStateId();
                    if (click(handler, COMBINE_SLOT, ContainerInput.QUICK_MOVE)) {
                        setCombineResultPhase(now, sourceBeforeCombine, combineClickRevision);
                    }
                } else {
                    int powderSlot = findPowderSlot(handler, task.element(), task.targetTier() - 1);
                    if (powderSlot < 0) {
                        failTask("Powder stack not found");
                    } else if (click(handler, powderSlot, ContainerInput.PICKUP)) {
                        setPhase(Phase.WAIT_INPUTS, now, 0);
                    }
                }
            }
            case WAIT_COMBINE_RESULT -> {
                int sourceCount = countPowder(handler, task.element(), task.targetTier() - 1);
                if (sourceCount < task.sourceBeforeCombine()) {
                    if (hasOutput(handler)) {
                        if (click(handler, OUTPUT_SLOT, ContainerInput.PICKUP)) {
                            setPhase(Phase.WAIT_OUTPUT_COLLECTION, now, 0);
                        }
                    } else {
                        status = "Waiting for server output...";
                        if (now - task.phaseStartedAt() > COMBINE_RESULT_TIMEOUT_MS) {
                            failTask("Combined powder output did not arrive");
                        }
                    }
                } else if (handler.getStateId() > task.combineClickRevision() && inputsFilled(handler)) {
                    if (hasLoreLine(handler, COMBINE_SLOT, "Cannot afford to upgrade")) {
                        failTask("Cannot afford to upgrade");
                        return;
                    }
                    int retryRevision = handler.getStateId();
                    if (click(handler, COMBINE_SLOT, ContainerInput.QUICK_MOVE)) {
                        setCombineResultPhase(now, task.sourceBeforeCombine(), retryRevision);
                    }
                } else if (phaseTimedOut(task, now)) {
                    failTask("Powders could not be combined");
                }
            }
            case WAIT_OUTPUT_COLLECTION -> {
                if (!handler.getCarried().isEmpty()) {
                    int destination = findCursorDestination(handler);
                    if (destination >= 0 && click(handler, destination, ContainerInput.PICKUP)) {
                        setPhase(Phase.WAIT_OUTPUT_COLLECTION, now, 0);
                    }
                } else if (!hasOutput(handler)) {
                    setPhase(Phase.FLUSH_INPUTS, now, 0);
                } else if (phaseTimedOut(task, now)) {
                    failTask("Could not collect combined powders");
                }
            }
            case FLUSH_INPUTS -> {
                if (!hasAnyInput(handler)) {
                    if (countPowder(handler, task.element(), task.targetTier() - 1) >= 4) {
                        setPhase(Phase.SELECT_SOURCE, now, 0);
                    } else {
                        finishTask();
                    }
                } else if (click(handler, FIRST_INPUT_SLOT, ContainerInput.PICKUP)) {
                    setPhase(Phase.FLUSH_INPUTS, now, 0);
                }
            }
        }
    }

    private static boolean inputsFilled(AbstractContainerMenu handler) {
        if (handler == null || handler.slots.size() <= LAST_INPUT_SLOT) return false;
        for (int slot = FIRST_INPUT_SLOT; slot <= LAST_INPUT_SLOT; slot++) {
            if (isInputEmpty(handler.getSlot(slot).getItem())) return false;
        }
        return true;
    }

    private static boolean hasAnyInput(AbstractContainerMenu handler) {
        if (handler == null || handler.slots.size() <= LAST_INPUT_SLOT) return false;
        for (int slot = FIRST_INPUT_SLOT; slot <= LAST_INPUT_SLOT; slot++) {
            if (!isInputEmpty(handler.getSlot(slot).getItem())) return true;
        }
        return false;
    }

    private static boolean isInputEmpty(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        String name = ChatFormatting.stripFormatting(stack.getHoverName().getString());
        return name != null && EMPTY_INPUT_NAME.equalsIgnoreCase(name.trim());
    }

    private static boolean hasOutput(AbstractContainerMenu handler) {
        if (!hasStack(handler, OUTPUT_SLOT)) return false;
        String name = ChatFormatting.stripFormatting(handler.getSlot(OUTPUT_SLOT).getItem().getHoverName().getString());
        return name == null || !EMPTY_OUTPUT_NAME.equalsIgnoreCase(name.trim());
    }

    private static boolean isSupportedMenuType(MenuType menuType) {
        return menuType == MenuType.POWDER_MASTER_REMOVE || menuType == MenuType.POWDER_MASTER_COMBINE
                || menuType == MenuType.ITEM_UPGRADER || menuType == MenuType.ITEM_UPGRADER_COMBINE;
    }

    private static boolean isCombineMenu(MenuType menuType) {
        return menuType == MenuType.POWDER_MASTER_COMBINE || menuType == MenuType.ITEM_UPGRADER_COMBINE;
    }

    private static int tabSlot(MenuType menuType) {
        return menuType == MenuType.ITEM_UPGRADER ? ITEM_UPGRADER_TAB_SLOT : POWDER_MASTER_TAB_SLOT;
    }

    private static boolean hasStack(AbstractContainerMenu handler, int slot) {
        return handler != null && slot >= 0 && slot < handler.slots.size() && handler.getSlot(slot).hasItem();
    }

    private static boolean hasLoreLine(AbstractContainerMenu handler, int slot, String text) {
        if (!hasStack(handler, slot)) return false;
        ItemLore lore = handler.getSlot(slot).getItem().get(DataComponents.LORE);
        if (lore == null) return false;
        for (Component line : lore.lines()) {
            if (line.getString().contains(text)) return true;
        }
        return false;
    }

    private static int countPowder(AbstractContainerMenu handler, Element element, int tier) {
        if (handler == null) return 0;
        int count = 0;
        for (Slot slot : handler.slots) {
            if (!(slot.container instanceof Inventory)) continue;
            if (isPowder(slot.getItem(), element, tier)) count += slot.getItem().getCount();
        }
        return count;
    }

    private static int findPowderSlot(AbstractContainerMenu handler, Element element, int tier) {
        if (handler == null) return -1;
        for (Slot slot : handler.slots) {
            if (slot.container instanceof Inventory && isPowder(slot.getItem(), element, tier)) return slot.index;
        }
        return -1;
    }

    private static boolean isPowder(ItemStack stack, Element element, int tier) {
        if (stack == null || stack.isEmpty() || tier < 1 || tier > TIER_NAMES.length) return false;
        String name = stack.getHoverName().getString().trim().toLowerCase(Locale.ROOT);
        String suffix = (element.name() + " " + TIER_NAMES[tier - 1]).toLowerCase(Locale.ROOT);
        String powderSuffix = (element.name() + " Powder " + TIER_NAMES[tier - 1]).toLowerCase(Locale.ROOT);
        return name.endsWith(suffix) || name.endsWith(powderSuffix);
    }

    private static int findCursorDestination(AbstractContainerMenu handler) {
        if (handler == null || handler.getCarried().isEmpty()) return -1;
        ItemStack cursor = handler.getCarried();
        int emptySlot = -1;
        for (Slot slot : handler.slots) {
            if (!(slot.container instanceof Inventory)) continue;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                if (emptySlot < 0) emptySlot = slot.index;
            } else if (ItemStack.isSameItemSameComponents(stack, cursor) && stack.getCount() < stack.getMaxStackSize()) {
                return slot.index;
            }
        }
        return emptySlot;
    }

    private static boolean click(AbstractContainerMenu handler, int slot, ContainerInput actionType) {
        Minecraft client = Minecraft.getInstance();
        if (handler == null || client.player == null || client.gameMode == null) {
            return false;
        }
        if (slot < 0 || slot >= handler.slots.size()) {
            return false;
        }
        if (client.player.containerMenu.containerId != handler.containerId) {
            return false;
        }
        if (actionType == ContainerInput.QUICK_MOVE) {
            ContainerUtils.shiftClickOnSlot(slot, handler.containerId, handler.getStateId(), 0, handler.getItems());
        } else {
            ContainerUtils.clickOnSlot(slot, handler.containerId, handler.getStateId(), 0, handler.getItems());
        }
        return true;
    }

    private static boolean phaseTimedOut(CombineTask task, long now) {
        return now - task.phaseStartedAt() > PHASE_TIMEOUT_MS;
    }

    private static void setPhase(Phase phase, long now, int sourceBeforeCombine) {
        CombineTask task = activeTask;
        if (task == null) return;
        activeTask = new CombineTask(task.element(), task.targetTier(), phase, task.startedAt(), now, sourceBeforeCombine, 0);
    }

    private static void setCombineResultPhase(long now, int sourceBeforeCombine, int combineClickRevision) {
        CombineTask task = activeTask;
        if (task == null) return;
        activeTask = new CombineTask(task.element(), task.targetTier(), Phase.WAIT_COMBINE_RESULT,
                task.startedAt(), now, sourceBeforeCombine, combineClickRevision);
    }

    private static void finishTask() {
        CombineTask task = activeTask;
        if (task == null) return;
        status = "Combined " + task.element().name() + " " + TIER_NAMES[task.targetTier() - 1];
        statusUntil = System.currentTimeMillis() + 2_000;
        activeTask = null;
    }

    private static void failTask(String message) {
        status = message;
        statusUntil = System.currentTimeMillis() + 3_000;
        activeTask = null;
    }

    private record Element(String name, String symbol, boolean commonFont, int textColor, int backgroundColor) {
        private Component symbolText() {
            Component text = Component.literal(symbol);
            if (!commonFont) return text;
            return text.copy().withStyle(Style.EMPTY.withFont(
                    new FontDescription.Resource(Identifier.fromNamespaceAndPath("minecraft", "common"))));
        }

        private Component combineTooltip(int targetTier) {
            int color = textColor & 0xFFFFFF;
            return Component.empty()
                    .append(Component.literal("Click to combine your "))
                    .append(symbolText().copy().withStyle(style -> style.withColor(color)))
                    .append(Component.literal(" " + name + " Powder " + TIER_NAMES[targetTier - 2])
                            .withStyle(style -> style.withColor(color)))
                    .append(Component.literal(" to "))
                    .append(Component.literal(TIER_NAMES[targetTier - 1]).withStyle(style -> style.withColor(color)));
        }
    }

    private record CombineTask(Element element, int targetTier, Phase phase, long startedAt, long phaseStartedAt,
                               int sourceBeforeCombine, int combineClickRevision) {
    }

    private enum Phase {
        ENSURE_COMBINE_TAB,
        WAIT_COMBINE_TAB,
        WAIT_EXISTING_OUTPUT,
        SELECT_SOURCE,
        WAIT_INPUTS,
        WAIT_COMBINE_RESULT,
        WAIT_OUTPUT_COLLECTION,
        FLUSH_INPUTS
    }

    private static final class PanelWidget extends Widget {
        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            ui.drawCenteredText(WynnExtras.addWynnExtrasPrefix("§6Powder combine helper"), x + width / 2f, y - 4,
                    CustomColor.fromHexString("FFFFFF"), 0.9f);
        }
    }

    private static final class ElementHeaderWidget extends Widget {
        private final Element element;

        private ElementHeaderWidget(Element element) {
            this.element = element;
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            ui.drawCenteredText(element.symbolText(), x + width / 2f, y + height / 2f,
                    CustomColor.fromInt(element.textColor()), 1f);
        }
    }

    private static final class StatusWidget extends Widget {
        private String text = "";

        private void setText(String text) {
            this.text = text == null ? "" : text;
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            CustomColor color = activeTask == null ? CustomColor.fromInt(0xFFAAAAAA) : CustomColor.WHITE;
            ui.drawCenteredText(text, x + width / 2f, y + height / 2f, color, 0.75f);
        }
    }

    private static final class PowderButton extends Widget {
        private final Element element;
        private final int targetTier;

        private PowderButton(Element element, int targetTier) {
            this.element = element;
            this.targetTier = targetTier;
        }

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            int background = hovered && enabled ? brighten(element.backgroundColor()) : element.backgroundColor();
            ui.drawRect(x, y, width, height, CustomColor.fromInt(background));
            ui.drawCenteredText(TIER_NAMES[targetTier - 1], x + width / 2f, y + height / 2f,
                    CustomColor.fromInt(enabled ? 0xFFFFFFFF : 0xFF888888), 1);
            if(hovered) {
                ctx.setTooltipForNextFrame(element.combineTooltip(targetTier), mouseX, mouseY);
            }
        }

        @Override
        protected boolean onClick(int button) {
            if (button != 0 || !enabled) return false;
            startTask(element, targetTier);
            return true;
        }

        private static int brighten(int color) {
            int red = Math.min(255, ((color >> 16) & 0xFF) + 24);
            int green = Math.min(255, ((color >> 8) & 0xFF) + 24);
            int blue = Math.min(255, (color & 0xFF) + 24);
            return (color & 0xFF000000) | red << 16 | green << 8 | blue;
        }
    }
}

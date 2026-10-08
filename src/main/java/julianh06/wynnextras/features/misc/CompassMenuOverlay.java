package julianh06.wynnextras.features.misc;

import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.TooltipUtils;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.features.loader.SkillPointLoader;
import julianh06.wynnextras.features.tomes.TomeState;
import julianh06.wynnextras.utils.HandledScreenAccess;
import julianh06.wynnextras.utils.UI.WEMenuExtension;
import julianh06.wynnextras.utils.UI.Widget;
import julianh06.wynnextras.wynncraft.menu.MenuType;
import julianh06.wynnextras.wynncraft.menu.WynncraftMenuService;
import julianh06.wynnextras.wynncraft.state.SkillPoint;
import julianh06.wynnextras.wynncraft.state.SkillPointState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import julianh06.wynnextras.wynncraft.item.ItemCategory;
import julianh06.wynnextras.wynncraft.item.WynnItemData;
import julianh06.wynnextras.wynncraft.item.WynnItemParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


public class CompassMenuOverlay extends WEMenuExtension {
    AutoAssignButton autoAssignButton;
    List<ItemWidget> itemWidgets = new ArrayList<>();
    static ItemStack hoveredItem = ItemStack.EMPTY;

    static boolean selectingWeapon = false;
    static ItemStack selectedWeapon = null;
    private static StatusType statusType = StatusType.IDLE;
    private static String statusPrimary = "";
    private static String statusSecondary = "";

    public enum StatusType {
        IDLE,
        INFO,
        PROGRESS,
        SUCCESS,
        ERROR
    }

    public CompassMenuOverlay() {
        for (int i = 0; i < 4; i++) {
            ItemWidget itemWidget = new ItemWidget();
            itemWidgets.add(itemWidget);
            rootWidgets.add(itemWidget);
        }

        autoAssignButton = new AutoAssignButton();
        rootWidgets.add(autoAssignButton);
    }

    @Override
    protected void drawBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        hoveredItem = ItemStack.EMPTY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (handled) return true;

        if (!selectingWeapon) return false;
        if (!(MinecraftUtils.screen() instanceof AbstractContainerScreen<?> screen)) return false;

        Slot focused = HandledScreenAccess.focusedSlot(screen);
        if (focused == null || !focused.hasItem()) return false;

        ItemStack clicked = focused.getItem();
        WynnItemData item = WynnItemParser.parse(clicked).orElse(null);
        if (item == null || item.category() != ItemCategory.GEAR || !item.gearType().isWeapon()) {
            setStatus(StatusType.ERROR, "That is not a valid weapon.", "Select a weapon item or skip selection.");
            return true;
        }

        selectedWeapon = clicked;
        selectingWeapon = false;
        startAssignment();
        return true;
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (!WynncraftMenuService.isCurrent(MenuType.CHARACTER_INFO)) return;
        if(!(MinecraftUtils.screen() instanceof AbstractContainerScreen<?> screen)) return;

        float xStart = hsX(screen);
        float yStart = hsY(screen) + hsHeight(screen);
        float backgroundWidth = hsWidth(screen);
        int buttonWidth = 133;

        int itemWidth = 17;
        int itemHeight = itemWidth;
        float itemXStart = xStart + 8;
        float itemYStart = yStart + 20;

        autoAssignButton.setBounds((int) (xStart + (backgroundWidth - buttonWidth) / 2f), (int) (itemYStart + 22), buttonWidth, 17);

        ui.drawCenteredText(WynnExtras.addWynnExtrasPrefix("§6Skillpoint helper:"), xStart + backgroundWidth / 2f, yStart + 8, CustomColor.fromHexString("FFFFFF"), 1f);
        CustomColor statusColor = statusColor();
        if (!statusPrimary.isEmpty()) {
            ui.drawCenteredText(statusPrimary, xStart + backgroundWidth / 2f, itemYStart + 47, statusColor, 0.72f);
        }
        if (!statusSecondary.isEmpty()) {
            ui.drawCenteredText(statusSecondary, xStart + backgroundWidth / 2f, itemYStart + 54, statusColor, 0.67f);
        }

        backgroundWidth -= 32;
        for(int i = 0; i < 4; i++) {
            ItemStack item = MinecraftUtils.player().getItemBySlot(EquipmentSlot.BY_ID.apply(4 - i));
            itemWidgets.get(i).setBounds((int) (itemXStart + i * backgroundWidth / 3f), (int) itemYStart, itemWidth, itemHeight);
            itemWidgets.get(i).setItem(item);
        }
    }

    @Override
    protected void drawForeground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
    }

    public void renderHoveredTooltip(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        if(hoveredItem.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        List<Component> tooltip = hoveredItem.getTooltipLines(Item.TooltipContext.EMPTY, mc.player, TooltipFlag.NORMAL);
        drawTooltip(mc.font, TooltipUtils.getClientTooltipComponent(tooltip), mouseX + 14, mouseY, ctx);
    }

    private static void drawTooltip(net.minecraft.client.gui.Font textRenderer, List<ClientTooltipComponent> components, int x, int y, GuiGraphicsExtractor context) {
        if (components.isEmpty()) return;

        int width = 0;
        int height = components.size() == 1 ? -2 : 0;
        ClientTooltipComponent component;
        for (Iterator<ClientTooltipComponent> iterator = components.iterator(); iterator.hasNext(); height += component.getHeight(textRenderer)) {
            component = iterator.next();
            width = Math.max(width, component.getWidth(textRenderer));
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (x + width > screenWidth) x = Math.max(4, screenWidth - width - 4);
        if (y + height > screenHeight) y = Math.max(4, screenHeight - height - 4);

        TooltipRenderUtil.extractTooltipBackground(context, x, y, width, height, null);

        int textY = y;
        for (int i = 0; i < components.size(); i++) {
            component = components.get(i);
            component.extractText(context, textRenderer, x, textY);
            textY += component.getHeight(textRenderer) + (i == 0 ? 2 : 0);
        }

        int itemY = y;
        for (int i = 0; i < components.size(); i++) {
            component = components.get(i);
            component.extractImage(textRenderer, x, itemY, width, height, context);
            itemY += component.getHeight(textRenderer) + (i == 0 ? 2 : 0);
        }
    }

    private static class ItemWidget extends Widget {
        ItemStack item;

        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            if(item == null) return;

            ctx.item(item, (int) (x / ui.getScaleFactor()), (int) (y / ui.getScaleFactor()));

            if(hovered) {
                hoveredItem = item;
                ui.drawRect(x - 0.5f, y - 0.25f, width, height, CustomColor.fromHexString("FFFFFF").withAlpha(0.25f));
            }

        }

        public void setItem(ItemStack item) {
            this.item = item;
        }
    }

    private static class AutoAssignButton extends Widget {
        @Override
        protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
            if (!WynncraftMenuService.isCurrent(MenuType.CHARACTER_INFO)) return;
            ui.drawButton(x, y, width, height, hovered);
            if (selectingWeapon) {
                ui.drawCenteredText("Skip weapon selection", x + width / 2f, y + height / 2f, CustomColor.fromHexString("FFFFFF"), 1f);
            } else {
                ui.drawCenteredText("Auto assign skill points", x + width / 2f, y + height / 2f, CustomColor.fromHexString("FFFFFF"), 1f);
            }
        }

        @Override
        protected boolean onClick(int button) {
            if (SkillPointLoader.getInstance().isLoading()) {
                setStatus(StatusType.INFO, "Skill point assignment is already running.");
                return true;
            }
            if (selectingWeapon) {
                selectedWeapon = null;
                selectingWeapon = false;
                startAssignment();
                return true;
            }

            selectedWeapon = null;
            selectingWeapon = true;
            setStatus(StatusType.INFO, "Click on a weapon in your inventory", "or click Skip weapon selection.");
            return true;
        }
    }

    private static void startAssignment() {
        setStatus(StatusType.INFO, "Calculating required skill points...");
        int[] required = calculateRequiredSkillPoints(selectedWeapon);
        if (required == null) {
            setStatus(StatusType.ERROR, "No skill point requirements found.");
            return;
        }

        SkillPointLoader.getInstance().load(required[0], required[1], required[2], required[3], required[4]);
    }

    private static int[] calculateRequiredSkillPoints(ItemStack weapon) {
        List<SolvableItem> nonWeaponItems = new ArrayList<>();

        //Armor
        for (int i = 0; i < 4; i++) {
            ItemStack stack = MinecraftUtils.player().getItemBySlot(EquipmentSlot.BY_ID.apply(4 - i));
            SolvableItem si = toSolvableItem(stack);
            if (si != null) nonWeaponItems.add(si);
        }

        //Accessories
        for (int i = 9; i < 13; i++) {
            ItemStack stack = MinecraftUtils.player().getInventory().getItem(i);
            SolvableItem si = toSolvableItem(stack);
            if (si != null) nonWeaponItems.add(si);
        }

        SolvableItem weaponItem = (weapon != null && !weapon.isEmpty()) ? toSolvableItem(weapon) : null;

        if (nonWeaponItems.isEmpty() && weaponItem == null) return null;

        int[] best = null;
        List<SolvableItem> bestOrder = null;

        int n = nonWeaponItems.size();
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) indices[i] = i;

        List<List<Integer>> permutations = new ArrayList<>();
        generatePermutations(indices, 0, permutations);
        int[] tomeBonuses = TomeState.guildSkillBonuses();

        for (List<Integer> perm : permutations) {
            List<SolvableItem> order = new ArrayList<>();
            for (int idx : perm) order.add(nonWeaponItems.get(idx));

            int[] candidate = evaluateOrder(order, weaponItem, tomeBonuses);

            if (best == null || isBetter(candidate, best)) {
                best = candidate;
                bestOrder = new ArrayList<>(order);
            }
        }

        if (best == null) best = new int[5];
        //logBestOrder(bestOrder != null ? bestOrder : List.of(), weaponItem, best);
        return best;
    }

    private static int[] evaluateOrder(List<SolvableItem> order, SolvableItem weapon, int[] baseBonuses) {
        int[] current = Arrays.copyOf(baseBonuses, 5);
        int[] assigned = new int[5];

        for (SolvableItem entry : order) {
            for (int i = 0; i < 5; i++) {
                if (entry.reqs[i] <= 0) continue;
                if (current[i] < entry.reqs[i]) {
                    int diff = entry.reqs[i] - current[i];
                    assigned[i] += diff;
                    current[i] += diff;
                }
            }
            for (int i = 0; i < 5; i++) {
                if (entry.bonuses[i] != 0) {
                    current[i] += entry.bonuses[i];
                }
            }
        }

        if (weapon != null) {
            for (int i = 0; i < 5; i++) {
                if (weapon.reqs[i] <= 0) continue;
                if (current[i] < weapon.reqs[i]) {
                    int diff = weapon.reqs[i] - current[i];
                    assigned[i] += diff;
                    current[i] += diff;
                }
            }
        }

        for (SolvableItem entry : order) {
            for (int i = 0; i < 5; i++) {
                if (entry.reqs[i] <= 0) continue;
                int effectiveReq = entry.reqs[i] + Math.max(0, entry.bonuses[i]);
                if (current[i] < effectiveReq) {
                    int diff = effectiveReq - current[i];
                    assigned[i] += diff;
                    current[i] += diff;
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            assigned[i] = Math.min(150, Math.max(0, assigned[i]));
        }

        return assigned;
    }

    private static void logBestOrder(List<SolvableItem> order, SolvableItem weapon, int[] assigned) {
        String[] skillNames = {"STR", "DEX", "INT", "DEF", "AGI"};
        WynnExtras.LOGGER.info("[WE-BEST] === Best order found ===");
        for (SolvableItem si : order) {
            WynnExtras.LOGGER.info("[WE-BEST]   " + si.name
                    + " reqs=" + Arrays.toString(si.reqs)
                    + " bonuses=" + Arrays.toString(si.bonuses));
        }
        if (weapon != null) WynnExtras.LOGGER.info("[WE-BEST]   WEAPON " + weapon.name);
        WynnExtras.LOGGER.info("[WE-BEST]   result=" + Arrays.toString(assigned)
                + " total=" + (assigned[0]+assigned[1]+assigned[2]+assigned[3]+assigned[4]));
    }

    private static boolean isBetter(int[] candidate, int[] best) {
        int sumC = 0, sumB = 0;
        for (int i = 0; i < 5; i++) { sumC += candidate[i]; sumB += best[i]; }
        return sumC < sumB;
    }

    private static void generatePermutations(int[] arr, int start, List<List<Integer>> result) {
        if (start == arr.length) {
            List<Integer> perm = new ArrayList<>();
            for (int v : arr) perm.add(v);
            result.add(perm);
            return;
        }
        for (int i = start; i < arr.length; i++) {
            int tmp = arr[start]; arr[start] = arr[i]; arr[i] = tmp;
            generatePermutations(arr, start + 1, result);
            tmp = arr[start]; arr[start] = arr[i]; arr[i] = tmp;
        }
    }

    private static class SolvableItem {
        String name = "?";
        int[] reqs = new int[5];
        int[] bonuses = new int[5];
    }

    private static SolvableItem toSolvableItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Optional<WynnItemData> data = WynnItemParser.parse(stack);
        if (data.isEmpty()) return null;

        SolvableItem si = new SolvableItem();
        si.reqs = data.get().requirementsArray();
        si.bonuses = data.get().bonusesArray();
        si.name = data.get().name();

        return si;
    }

    public static boolean isSelectingWeapon() {
        return selectingWeapon;
    }

    public static void setSelectingWeapon(boolean selectingWeapon) {
        CompassMenuOverlay.selectingWeapon = selectingWeapon;
    }

    public static void setStatus(StatusType type, String primary) {
        setStatus(type, primary, "");
    }

    public static void setStatus(StatusType type, String primary, String secondary) {
        statusType = type == null ? StatusType.IDLE : type;
        statusPrimary = primary == null ? "" : primary;
        statusSecondary = secondary == null ? "" : secondary;
    }

    public static void clearStatus() {
        setStatus(StatusType.IDLE, "", "");
    }

    private static CustomColor statusColor() {
        return switch (statusType) {
            case IDLE -> CustomColor.fromHexString("B7B7B7");
            case INFO -> CustomColor.fromHexString("FFE080");
            case PROGRESS -> CustomColor.fromHexString("55FFFF");
            case SUCCESS -> CustomColor.fromHexString("55FF55");
            case ERROR -> CustomColor.fromHexString("FF5555");
        };
    }
}
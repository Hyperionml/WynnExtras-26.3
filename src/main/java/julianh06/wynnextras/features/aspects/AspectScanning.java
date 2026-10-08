package julianh06.wynnextras.features.aspects;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.core.ResetTimeConfig;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.features.abilitytree.TreeLoader;
import julianh06.wynnextras.features.inventory.BankOverlay;
import julianh06.wynnextras.utils.WynncraftApiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import julianh06.wynnextras.compat.client.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import julianh06.wynnextras.features.raid.RaidLootConfig;
import julianh06.wynnextras.features.raid.RaidLootData;
import java.time.ZonedDateTime;
import java.util.*;


public class AspectScanning {
    static int SearchedPages = 0;
    public static final Map<String, Tuple<String, String>> allAspects = new HashMap<>();

    private static final Map<String, Long> lastPersonalUploadTime = new HashMap<>();
    private static final long PERSONAL_UPLOAD_COOLDOWN_MS = 60_000;

    private static ZonedDateTime lastGambitUploadReset = null;

    // Reward chest AspectScanning collection
    private static final List<String> collectedRewardAspects = new ArrayList<>();

    // Reward chest coordinates for raid detection
    private static final Map<String, double[]> REWARD_CHEST_COORDS = Map.of(
            "NOTG", new double[]{10342, 41, 3111},
            "NOL", new double[]{11005, 58, 2909},
            "TCC", new double[]{10817, 45, 3901},
            "TNA", new double[]{24489, 8, -23878},
            "TWP", new double[]{-19065, 125, -1819}
    );

    public static Map<String, Tuple<String, String>> aspectsToUpload = new HashMap<>();

    public static void openMenu(Minecraft client, Player player) {
        int currentSlot = player.getInventory().getSelectedSlot();
        player.getInventory().setSelectedSlot(7);

        // Just click normally - this opens the character menu
        client.gameMode.useItem(player, InteractionHand.MAIN_HAND);

        player.getInventory().setSelectedSlot(currentSlot);

        // Set flag to click on "Ability Tree" when menu opens
        maintracking.setNeedToClickAbilityTree(true);
        maintracking.setAspectScanreq(true);
    }

    public static Map<String, Tuple<String, String>> AspectsInMenu() {
        Screen currScreen = Minecraft.getInstance().gui.screen();
        AbstractContainerScreen<?> screen = (currScreen instanceof AbstractContainerScreen) ? (AbstractContainerScreen<?>) currScreen : null;
        Map<String, Tuple<String, String>> result = new HashMap<>();
        if (screen == null) {
            return result;
        } else {
            // On the first page (SearchedPages == 0), scan the 5 active aspects in center slots FIRST
            if (SearchedPages == 0) {
                int[] centerSlots = {4, 11, 15, 18, 26};

                for (int slotIdx : centerSlots) {
                    if (slotIdx >= screen.getMenu().slots.size()) continue;
                    Slot slot = screen.getMenu().slots.get(slotIdx);
                    if (!slot.hasItem()) continue;

                    List<Component> tooltips = slot.getItem().getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
                    String name = null;
                    String rarity = "";

                    for (Component tooltip : tooltips) {
                        String s = tooltip.getString().replaceAll("§.", "").trim();
                        if (name == null && (s.contains("Aspect") || s.contains("Embodiment"))) {
                            name = s;

                            // Get rarity from color
                            if (slot.getItem().getCustomName() != null &&
                                slot.getItem().getCustomName().getStyle() != null &&
                                slot.getItem().getCustomName().getStyle().getColor() != null) {
                                String hexCode = slot.getItem().getCustomName().getStyle().getColor().formatValue();
                                if(hexCode.equals("#AA00AA")) rarity = "Mythic";
                                else if(hexCode.equals("#FF5555")) rarity = "Fabled";
                                else if(hexCode.equals("#55FFFF")) rarity = "Legendary";
                            }
                        }
                    }

                    String bestTierLine = null;
                    for (Component tooltip : tooltips) {
                        String candidate = extractBestTierLine(tooltip).trim();
                        if (candidate.contains("Tier") &&
                                (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                            if (bestTierLine == null || candidate.length() > bestTierLine.length()) {
                                bestTierLine = candidate;
                            }
                        }
                    }

                    if (name != null && bestTierLine != null && !bestTierLine.isEmpty()) {
                        bestTierLine = bestTierLine.replaceAll("^[^A-Za-z0-9]*", "");
                        bestTierLine = bestTierLine.replaceAll("(\\[MAX])\\1+", "$1");
                        result.put(name, new Tuple<>(bestTierLine, rarity));
                    }
                }

                String classId = BankOverlay.currentCharacterID;
                if (classId != null && !classId.isEmpty()) {
                    Map<String, String> activeMap = new LinkedHashMap<>();
                    for (Map.Entry<String, Tuple<String, String>> entry : result.entrySet()) {
                        activeMap.put(entry.getKey(), entry.getValue().getA());
                    }

                    LocalAspectStorage.saveActiveAspects(classId, activeMap);
                }

                // Add active aspects to global map immediately
                for (Map.Entry<String, Tuple<String, String>> entry : result.entrySet()) {
                    allAspects.put(entry.getKey(), entry.getValue());
                }

                // Clear result map so we don't add these aspects twice
                result.clear();

                // Now continue to scan the regular slots on this page (don't return early)
            }

            int[] slotsToRead = {36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53}; // fill slot indices

            int readCount = 0;
            for (int idx = 0; idx < slotsToRead.length; idx++) {
                int i = slotsToRead[idx];
                Slot slot = screen.getMenu().slots.get(i);

                // Stop early if we encounter empty slots before batch is full
                if (!slot.hasItem()) {
                    // If this happens before readCount == 18, stop the batch immediately
                    dummyfunction(screen);
                    break; // Exit loop early, immediately go to next page
                }

                List<Component> tooltips = slot.getItem().getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
                String name = null;
                String rarity = "";

                for (Component tooltip : tooltips) {
                    String s = tooltip.getString().replaceAll("§.", "").trim();
                    if (name == null && (s.contains("Aspect") || s.contains("Embodiment"))) {
                        name = s;

                        // Safe null checking for rarity detection
                        if (slot.getItem().getCustomName() != null &&
                            slot.getItem().getCustomName().getStyle() != null &&
                            slot.getItem().getCustomName().getStyle().getColor() != null) {
                            String hexCode = slot.getItem().getCustomName().getStyle().getColor().formatValue();
                            if(hexCode.equals("#AA00AA")) {
                                rarity = "Mythic";
                            } else if(hexCode.equals("#FF5555")) {
                                rarity = "Fabled";
                            }else if(hexCode.equals("#55FFFF")) {
                                rarity = "Legendary";
                            }
                        }
                    }
                }

                String bestTierLine = null;
                for (Component tooltip : tooltips) {
                    String candidate = extractBestTierLine(tooltip).trim();
                    if (candidate.contains("Tier") &&
                            (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                        if (bestTierLine == null || candidate.length() > bestTierLine.length()) {
                            bestTierLine = candidate;
                        }
                    }
                }

                if (name != null && bestTierLine != null && !bestTierLine.isEmpty()) {
                    bestTierLine = bestTierLine.replaceAll("^[^A-Za-z0-9]*", "");
                    bestTierLine = bestTierLine.replaceAll("(\\[MAX])\\1+", "$1");
                    result.put(name, new Tuple<>(bestTierLine, rarity));
                }

                readCount++;
                if (readCount == 18) {
                    // Add current page aspects to global map BEFORE calling dummyfunction
                    // This ensures page 6 aspects are included in the upload
                    for (Map.Entry<String, Tuple<String, String>> entry : result.entrySet()) {
                        allAspects.put(entry.getKey(), entry.getValue());
                    }
                    dummyfunction(screen);
                    break; // Done with this page, no need to check further
                }

            }

            // Add remaining aspects to global map (for pages with < 18 aspects)
            for (Map.Entry<String, Tuple<String, String>> entry : result.entrySet()) {
                allAspects.put(entry.getKey(), entry.getValue());
            }

            maintracking.setAspectScanreq(false);

            // Don't close screen early - let the normal pagination complete
            // Only close if we've searched all pages (0-6 = 7 pages total)
            if (SearchedPages > 6 && MinecraftUtils.mc().gui.screen() != null) {
                MinecraftUtils.mc().gui.screen().onClose();
            }
            return result;
        }
    }

    public static void scanCurrentPagePassive() {
        Screen currScreen = Minecraft.getInstance().gui.screen();
        AbstractContainerScreen<?> screen = (currScreen instanceof AbstractContainerScreen) ? (AbstractContainerScreen<?>) currScreen : null;
        if (screen == null) return;

        int[] centerSlots = {4, 11, 15, 18, 26};
        Map<String, String> activeMap = new LinkedHashMap<>();
        for (int slotIdx : centerSlots) {
            if (slotIdx >= screen.getMenu().slots.size()) continue;
            Slot slot = screen.getMenu().slots.get(slotIdx);
            if (!slot.hasItem()) continue;

            String name = null;
            String bestTierLine = null;
            List<Component> tooltips = slot.getItem().getTooltipLines(
                    Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);

            for (Component tooltip : tooltips) {
                String s = tooltip.getString().replaceAll("§.", "").trim();
                if (name == null && (s.contains("Aspect") || s.contains("Embodiment"))) name = s;
            }
            for (Component tooltip : tooltips) {
                String candidate = extractBestTierLine(tooltip).trim();
                if (candidate.contains("Tier") &&
                        (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                    if (bestTierLine == null || candidate.length() > bestTierLine.length()) bestTierLine = candidate;
                }
            }
            if (name != null && bestTierLine != null) {
                bestTierLine = bestTierLine.replaceAll("^[^A-Za-z0-9]*", "");
                activeMap.put(name, bestTierLine);
            }
        }

        if (!activeMap.isEmpty()) {
            String classId = BankOverlay.currentCharacterID;
            if (classId != null && !classId.isEmpty()) {
                LocalAspectStorage.saveActiveAspects(classId, activeMap);
            }
        }

        int[] slotsToRead = {36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53};
        for (int i : slotsToRead) {
            if (i >= screen.getMenu().slots.size()) break;
            Slot slot = screen.getMenu().slots.get(i);
            if (!slot.hasItem()) break;

            String name = null;
            String rarity = "";
            String bestTierLine = null;
            List<Component> tooltips = slot.getItem().getTooltipLines(
                    Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);

            for (Component tooltip : tooltips) {
                String s = tooltip.getString().replaceAll("§.", "").trim();
                if (name == null && (s.contains("Aspect") || s.contains("Embodiment"))) {
                    name = s;
                    if (slot.getItem().getCustomName() != null &&
                            slot.getItem().getCustomName().getStyle() != null &&
                            slot.getItem().getCustomName().getStyle().getColor() != null) {
                        String hex = slot.getItem().getCustomName().getStyle().getColor().formatValue();
                        if (hex.equals("#AA00AA")) rarity = "Mythic";
                        else if (hex.equals("#FF5555")) rarity = "Fabled";
                        else if (hex.equals("#55FFFF")) rarity = "Legendary";
                    }
                }
            }
            for (Component tooltip : tooltips) {
                String candidate = extractBestTierLine(tooltip).trim();
                if (candidate.contains("Tier") &&
                        (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                    if (bestTierLine == null || candidate.length() > bestTierLine.length()) bestTierLine = candidate;
                }
            }
            if (name != null && bestTierLine != null) {
                bestTierLine = bestTierLine.replaceAll("^[^A-Za-z0-9]*", "");
                allAspects.put(name, new Tuple<>(bestTierLine, rarity));
            }
        }
    }

    public static void AspectsInRaidChest() {
        Screen currScreen = Minecraft.getInstance().gui.screen();
        AbstractContainerScreen<?> screen = (currScreen instanceof AbstractContainerScreen) ? (AbstractContainerScreen<?>) currScreen : null;
        if (screen == null) return;

        if(!screen.getMenu().getSlot(4).hasItem()) {
            return;
        }

        boolean samePage = true;
        if(!maintracking.scanDone) {
            for (int i = 11; i < 16; i++) {
                int arrayIndex = i - 11; // Map slot 11-15 to array index 0-4
                Slot slot = screen.getMenu().slots.get(i);
                ItemStack cachedStack = maintracking.aspectsInChest[arrayIndex];

                // If cached is null, definitely not same page
                if (cachedStack == null) {
                    samePage = false;
                    break;
                }

                if (slot.getItem().isEmpty()) {
                    if (!cachedStack.isEmpty()) {
                        samePage = false;
                        break;
                    }
                } else {
                    // Safe null check for customName comparison
                    Component currentName = slot.getItem().getCustomName();
                    Component cachedName = cachedStack.getCustomName();
                    if (currentName == null || cachedName == null || !currentName.equals(cachedName)) {
                        samePage = false;
                        break;
                    }
                }
            }
        }

        // 2. RETURN TO FIRST PAGE PHASE
        if (maintracking.scanDone && !maintracking.returnedToFirstPage && !screen.getMenu().slots.get(10).getItem().isEmpty()) {
            if (maintracking.pagesToGoBack > 0) {
                PrevPageRaid(screen);
                maintracking.pagesToGoBack--;
            }
            return;
        } else if (maintracking.scanDone && maintracking.pagesToGoBack <= 0) {
            maintracking.returnedToFirstPage = true;
            maintracking.lastAspectRewardScan = System.currentTimeMillis();
            return;
        }

        if(samePage) {
            return;
        }

        maintracking.aspectsInChest = new ItemStack[5];

        for(int i = 11; i < 16; i++) {
            int arrayIndex = i - 11; // Map slot 11-15 to array index 0-4
            Slot slot = screen.getMenu().slots.get(i);

            maintracking.aspectsInChest[arrayIndex] = slot.getItem().copy();

            // Stop if empty
            if (!slot.hasItem()) break;

            String rarity = null;

            List<Component> tooltips = slot.getItem().getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL);
            String name = null;
            if (slot.getItem().getCustomName() != null &&
                    slot.getItem().getCustomName().getStyle() != null &&
                    slot.getItem().getCustomName().getStyle().getColor() != null) {
                String hexCode = slot.getItem().getCustomName().getStyle().getColor().formatValue();
                RaidLootData data = RaidLootConfig.INSTANCE.data;
                String currentRaid = detectRaidFromPosition();
                if (hexCode.equals("#AA00AA")) {
                    rarity = "Mythic";
                    data.mythicAspects ++;
                    data.getOrCreateRaidData(currentRaid).mythicAspects ++;
                    data.sessionData.mythicAspects++;
                    data.getOrCreateSessionRaidData(currentRaid).mythicAspects++;
                    data.latestData.mythicAspects++;
                } else if (hexCode.equals("#FF5555")) {
                    rarity = "Fabled";
                    data.fabledAspects ++;
                    data.getOrCreateRaidData(currentRaid).fabledAspects ++;
                    data.sessionData.fabledAspects++;
                    data.getOrCreateSessionRaidData(currentRaid).fabledAspects++;
                    data.latestData.fabledAspects++;
                } else if (hexCode.equals("#55FFFF")) {
                    rarity = "Legendary";
                    data.legendaryAspects ++;
                    data.getOrCreateRaidData(currentRaid).legendaryAspects ++;
                    data.sessionData.legendaryAspects++;
                    data.getOrCreateSessionRaidData(currentRaid).legendaryAspects++;
                    data.latestData.legendaryAspects++;
                }
            }

            for (Component tooltip : tooltips) {
                String s = tooltip.getString().replaceAll("§.", "").trim();
                if (s.contains("Aspect") || s.contains("Embodiment")) {
                    name = s;
                    break;
                }
            }

            if (name == null) break;

            String bestTierLine = null;
            for (Component tooltip : tooltips) {
                String candidate = extractBestTierLine(tooltip).trim();
                if (candidate.contains("Tier") &&
                        (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                    if (bestTierLine == null || candidate.length() > bestTierLine.length()) {
                        bestTierLine = candidate;
                    }
                }
            }

            if(bestTierLine == null) continue;

            aspectsToUpload.put(name, new Tuple<>(bestTierLine, rarity));
        }

        // 1. SCANNING PHASE
        if (!maintracking.scanDone) {
            if (!screen.getMenu().slots.get(16).getItem().isEmpty()) {
                NextPageRaid(screen);
                maintracking.pagesToGoBack++;
            } else {
                RaidLootConfig.INSTANCE.save();
                maintracking.scanDone = true;
                maintracking.returnedToFirstPage = false;

                if (!aspectsToUpload.isEmpty()) {
                    WynncraftApiHandler.processAspects(aspectsToUpload);
                }
            }
        }
    }

    private static String detectRaidFromPosition() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return "UNKNOWN";

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        String closest = "UNKNOWN";
        double minDist = Double.MAX_VALUE;

        for (Map.Entry<String, double[]> entry : REWARD_CHEST_COORDS.entrySet()) {
            double[] pos = entry.getValue();
            double dist = Math.sqrt(Math.pow(px - pos[0], 2) + Math.pow(py - pos[1], 2) + Math.pow(pz - pos[2], 2));
            if (dist < minDist) {
                minDist = dist;
                closest = entry.getKey();
            }
        }

        // Sanity check: if player is too far from any known chest, return UNKNOWN
        return minDist < 100 ? closest : "UNKNOWN";
    }

    /**
     * Reset collected reward aspects (called when screen closes)
     */
    public static void resetRewardAspects() {
        if (!collectedRewardAspects.isEmpty()) {
            WynnExtras.LOGGER.info("[WynnExtras] Clearing " + collectedRewardAspects.size() + " collected reward aspects");
            collectedRewardAspects.clear();
        }
    }

    private static String extractBestTierLine(Component t) {
        String self = t.getString().replaceAll("§.", "").trim();
        boolean relevant = !self.isEmpty() && (self.contains("Tier") || self.contains(">>>") ||
                self.matches(".*\\[\\d+/\\d+\\].*") || self.contains("[MAX]"));
        StringBuilder out = new StringBuilder();
        if (relevant) out.append(self);

        StringBuilder deepest = new StringBuilder();
        if (t.getSiblings() != null) {
            for (Component sib : t.getSiblings()) {
                String sub = extractBestTierLine(sib).trim();
                if (sub.length() > deepest.length()) {
                    deepest.setLength(0); deepest.append(sub);
                }
            }
        }
        return deepest.length() > out.length() ? deepest.toString() : out.toString();
    }

    private static void dummyfunction(AbstractContainerScreen<?> screen) {
        //WynnExtras.LOGGER.info("dummyfunction called after 18 slots read");
        SearchedPages++;
        TreeLoader.clickOnNameInInventory("Next Page",screen,Minecraft.getInstance());
        if(SearchedPages<=6){
            maintracking.setNextPage(true);
            // Show progress message matching UI page numbers (don't show for page 0 which is active aspects)
            if(SearchedPages > 0) {
                MinecraftUtils.sendMessageToClient(WynnExtras.addWynnExtrasPrefix("§7Scanning page " + (SearchedPages) + "/6..."));
            }
        }
        else{
            // Create a copy of the map to avoid it being cleared before async upload completes
            Map<String, Tuple<String, String>> aspectsCopy = new HashMap<>(allAspects);
            WynncraftApiHandler.processAspects(aspectsCopy);
            resetAllAspects();
            SearchedPages = 0;
        }
    }
    private static void NextPageRaid(AbstractContainerScreen<?> screen) {
        WynnExtras.LOGGER.info("[WynnExtras] Clicking next page in reward chest");
        TreeLoader.clickOnNameInInventory("Next Page",screen,Minecraft.getInstance());
        maintracking.GuiSettleTicks = 0;
    }

    public static void PrevPageRaid(AbstractContainerScreen<?> screen) {
        WynnExtras.LOGGER.info("[WynnExtras] Clicking previous page in reward chest");
        TreeLoader.clickOnNameInInventory("Previous Page",screen,Minecraft.getInstance());
        maintracking.GuiSettleTicks = 0;
    }
    public static void setSearchedPages(int searchedPages) {
        SearchedPages = searchedPages;
    }
    public static int getSearchedPages() {
        return SearchedPages;
    }
    public static void resetAllAspects() {
        allAspects.clear();
    }

    /**
     * Detects and displays all 4 daily gambits from the Party Finder menu
     * Gambit format: "Name Gambit" or "Name Name's Gambit"
     * Example: "Glutton's Gambit", "Dull Blade's Gambit"
     * Description is between "Refreshes in X hours" and "Rewards for enabling"
     * Also saves to GambitData for the GUI
     */
    public static void detectGambit(AbstractContainerScreen<?> screen) {
        if (screen == null) return;

        try {
            List<String> gambitsForChat = new ArrayList<>();
            List<GambitData.GambitEntry> gambitsForSave = new ArrayList<>();

            // Search through inventory slots for all gambit items
            for (Slot slot : screen.getMenu().slots) {
                if (!slot.hasItem()) continue;

                ItemStack stack = slot.getItem();
                if (stack.getCustomName() == null) continue;

                String itemName = stack.getCustomName().getString().replaceAll("§.", "").trim();

                // Check if item name contains "Gambit"
                if (itemName.contains("Gambit")) {
                    // Get the tooltip for description
                    List<Component> tooltips = stack.getTooltipLines(Item.TooltipContext.EMPTY,
                        Minecraft.getInstance().player, TooltipFlag.NORMAL);

                    StringBuilder description = new StringBuilder();
                    boolean inDescriptionSection = false;

                    for (Component tooltip : tooltips) {
                        String line = tooltip.getString().replaceAll("§.", "").trim();

                        // Start collecting after "Refreshes in" line
                        if (line.contains("Refreshes in")) {
                            inDescriptionSection = true;
                            continue;
                        }

                        // Stop collecting at "Rewards for enabling" or "Rewards for Enabling"
                        if (line.contains("Rewards for enabling") || line.contains("Rewards for Enabling")) {
                            break;
                        }

                        // Collect description lines
                        if (inDescriptionSection && !line.isEmpty()) {
                            if (description.length() > 0) description.append(" ");
                            description.append(line);
                        }
                    }

                    // Only add if we have both name and description
                    if (description.length() > 0) {
                        gambitsForChat.add("§e" + itemName + " §7- " + description.toString());
                        gambitsForSave.add(new GambitData.GambitEntry(itemName, description.toString()));
                    }
                }
            }

            // Save gambits locally
            if (!gambitsForSave.isEmpty()) {
                GambitData.INSTANCE.saveGambits(gambitsForSave);
            }

            // Upload to crowdsourcing API
            if (!gambitsForSave.isEmpty() && canUploadGambits()) {
                WynncraftApiHandler.uploadGambits(gambitsForSave);
                lastGambitUploadReset = ResetTimeConfig.INSTANCE.getCurrentGambitReset();
            }
        } catch (Exception e) {
            MinecraftUtils.sendMessageToClient(WynnExtras.addWynnExtrasPrefix("§cError detecting gambit: " + e.getMessage()));
        }
    }

    /**
     * Scans the raid preview chest for aspects and detects which raid is selected
     * Raid is detected from the screen title itself (last character changes per raid)
     * Also extracts user's AspectScanning tier and progress for each AspectScanning
     * Saves loot pool data locally for the GUI
     */
    public static void scanPreviewChest(AbstractContainerScreen<?> screen, String screenTitle) {
        if (screen == null) return;

        WynnExtras.LOGGER.info("[WynnExtras] scanPreviewChest called with title: " + screenTitle);

        try {
            // Detect raid from the last character of the title
            String selectedRaid = "Unknown";
            if (screenTitle.endsWith("\uF00B")) {
                selectedRaid = "NOTG";
            } else if (screenTitle.endsWith("\uF00C")) {
                selectedRaid = "NOL";
            } else if (screenTitle.endsWith("\uF00D")) {
                selectedRaid = "TCC";
            } else if (screenTitle.endsWith("\uF00E")) {
                selectedRaid = "TNA";
            } else if (screenTitle.endsWith("\uF04B")) {
                selectedRaid = "TWP";
            }

            Map<String, Tuple<String, String>> foundAspects = new HashMap<>();

            // Collect all aspects with their progress
            for (Slot slot : screen.getMenu().slots) {
                if (!slot.hasItem()) continue;

                ItemStack stack = slot.getItem();
                List<Component> tooltips = stack.getTooltipLines(Item.TooltipContext.EMPTY,
                    Minecraft.getInstance().player, TooltipFlag.NORMAL);

                String aspectName = null;
                String rarity = "";
                StringBuilder description = new StringBuilder();
                boolean foundName = false;
                boolean foundTier = false;

                for (Component tooltip : tooltips) {
                    String line = tooltip.getString().replaceAll("§.", "").trim();

                    // Find AspectScanning name
                    if (aspectName == null && (line.contains("Aspect") || line.contains("Embodiment"))) {
                        aspectName = line;
                        foundName = true;

                        // Detect rarity from color with safe null checking
                        if (stack.getCustomName() != null &&
                            stack.getCustomName().getStyle() != null &&
                            stack.getCustomName().getStyle().getColor() != null) {
                            String hexCode = stack.getCustomName().getStyle().getColor().formatValue();
                            if (hexCode.equals("#AA00AA")) {
                                rarity = "Mythic";
                            } else if (hexCode.equals("#FF5555")) {
                                rarity = "Fabled";
                            } else if (hexCode.equals("#55FFFF")) {
                                rarity = "Legendary";
                            }
                        }
                        continue;
                    }

                    // Check if we've reached tier info
                    if (foundName && (line.contains("Tier") || line.contains("[") || line.contains(">") || line.contains("Class Req:"))) {
                        foundTier = true;
                    }

                    // Collect all description lines (between name and tier info)
                    if (foundName && !foundTier && !line.isEmpty() &&
                        !line.contains("Aspect") && !line.contains("Embodiment")) {
                        if (description.length() > 0) {
                            description.append("\n");
                        }
                        description.append(line);
                    }
                }

                // Find best tier line
                String bestTierLine = null;
                for (Component tooltip : tooltips) {
                    String candidate = extractBestTierLine(tooltip).trim();
                    if (candidate.contains("Tier") &&
                            (candidate.contains(">>>") || candidate.matches(".*\\[\\d+/\\d+\\].*") || candidate.contains("[MAX]"))) {
                        if (bestTierLine == null || candidate.length() > bestTierLine.length()) {
                            bestTierLine = candidate;
                        }
                    }
                }

                if (aspectName != null && bestTierLine != null && !bestTierLine.isEmpty()) {
                    bestTierLine = bestTierLine.replaceAll("^[^A-Za-z0-9]*", "");
                    bestTierLine = bestTierLine.replaceAll("(\\[MAX])\\1+", "$1");
                    foundAspects.put(aspectName, new Tuple<>(bestTierLine, rarity));
                }
            }

            // Upload aspects
            if (!foundAspects.isEmpty() && !selectedRaid.equals("Unknown")) {
                if (canUploadPersonal(selectedRaid)) {
                    WynnExtras.LOGGER.info("[WynnExtras] Uploading personal aspect progress (" + foundAspects.size() + ")");
                    WynncraftApiHandler.processAspects(foundAspects);
                    lastPersonalUploadTime.put(selectedRaid, System.currentTimeMillis());
                } else {
                    WynnExtras.LOGGER.info("[WynnExtras] Personal progress upload skipped (cooldown)");
                }
            }
        } catch (Exception e) {
            MinecraftUtils.sendMessageToClient(WynnExtras.addWynnExtrasPrefix("§cError scanning preview chest: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private static boolean canUploadGambits() {
        ZonedDateTime currentReset = ResetTimeConfig.INSTANCE.getCurrentGambitReset();
        return lastGambitUploadReset == null || currentReset.isAfter(lastGambitUploadReset);
    }

    private static boolean canUploadPersonal(String raid) {
        Long last = lastPersonalUploadTime.get(raid);
        return last == null || System.currentTimeMillis() - last >= PERSONAL_UPLOAD_COOLDOWN_MS;
    }
}
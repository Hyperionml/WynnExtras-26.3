package julianh06.wynnextras.features.inventory;

import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.core.command.Command;
import julianh06.wynnextras.annotations.WEModule;
import julianh06.wynnextras.event.CharInputEvent;
import julianh06.wynnextras.event.KeyInputEvent;
import julianh06.wynnextras.event.TickEvent;
import julianh06.wynnextras.event.WorldChangeEvent;
import julianh06.wynnextras.features.bankoverlay.BankOverlay2;
import julianh06.wynnextras.features.bankoverlay.BankOverlaySlotBridge;
import julianh06.wynnextras.features.bankoverlay.BankViewerScreen;
import julianh06.wynnextras.features.inventory.data.*;
import julianh06.wynnextras.features.misc.ClassSelectionOverlay;
import julianh06.wynnextras.utils.LunarCompat;
import julianh06.wynnextras.utils.overlays.EasyTextInput;
import julianh06.wynnextras.utils.UI.WEScreen;
import julianh06.wynnextras.wynncraft.menu.MenuType;
import julianh06.wynnextras.wynncraft.menu.WynncraftMenuService;
import julianh06.wynnextras.wynncraft.state.CharacterState;
import julianh06.wynnextras.wynncraft.item.WynnItemParser;
import julianh06.wynnextras.compat.wynntils.WynntilsBankAdapter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import julianh06.wynnextras.compat.client.InputCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WEModule
public class BankOverlay {
    private static final Command bankViewerCommand = new Command(
            "bank",
            "Opens the cached bank in read-only mode",
            context -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null) {
                    MinecraftUtils.sendMessageToClient(WynnExtras.addWynnExtrasPrefix("§cNo player is available."));
                    return 0;
                }

                AccountBankData.INSTANCE.load();
                BookshelfData.INSTANCE.load();
                MiscBucketData.INSTANCE.load();
                if (syncCurrentCharacterId()) CharacterBankData.INSTANCE.load();
                WEScreen.open(BankViewerScreen::new);
                return 1;
            });

    public static final NonNullList<Slot> playerInvSlots = NonNullList.create();
    public static final NonNullList<Slot> activeInvSlots = NonNullList.create();
    private static WynntilsBankAdapter.StorageHandle personalStorageUtils;

    public static BankData Pages;

    public static int activeInv = -1;

    public static ItemStack heldItem = ItemStack.EMPTY;

    public static final Map<Integer, List<WynntilsBankAdapter.AnnotationHandle>> annotationCache = new HashMap<>();

    private static EasyTextInput activeTextInput;

    public static volatile BankOverlayType currentOverlayType = BankOverlayType.NONE;
    public static volatile BankOverlayType expectedOverlayType = BankOverlayType.NONE;
    public static BankData currentData;
    public static String currentCharacterID;
    private static int currentMaxPages;

    public static boolean shouldWait = false;
    public static long shouldWaitSince = 0L;

    private static final boolean FORCE_MISSING_CHARACTER_ID_FOR_TESTING = false;

    private static boolean registeredScroll = false;
    private static Screen registeredScrollScreen = null;
    private static int registeredScrollSyncId = -1;
    private static long lastHeldWeaponCheckMs = 0;
    private static String lastPersistedHeldWeaponKey = "";

    public static WynntilsBankAdapter.StorageHandle getPersonalStorageUtils() {
        return personalStorageUtils;
    }

    public static void setPersonalStorageUtils(WynntilsBankAdapter.StorageHandle feature) {
        personalStorageUtils = feature;
    }

    public static void setActiveTextInput(EasyTextInput textInput) {
        activeTextInput = textInput;
    }

    public static void resetScrollRegistration() {
        registeredScroll = false;
        registeredScrollScreen = null;
        registeredScrollSyncId = -1;
    }

    public static int getCurrentMaxPages() {
        return currentMaxPages;
    }

    public static void setCurrentMaxPages(int maxPages) {
        currentMaxPages = maxPages;
    }

    public static boolean hasValidCurrentCharacterId() {
        if (FORCE_MISSING_CHARACTER_ID_FOR_TESTING) return false;
        return currentCharacterID != null && !currentCharacterID.isBlank() && !"null".equalsIgnoreCase(currentCharacterID);
    }

    public static boolean isCharacterBankMissingCharacterId() {
        return currentOverlayType == BankOverlayType.CHARACTER && !hasValidCurrentCharacterId();
    }

    public static boolean syncCurrentCharacterId() {
        String characterId = CharacterState.id().orElse(null);
        if (characterId == null || characterId.isBlank() || "-".equals(characterId) || "null".equalsIgnoreCase(characterId)) {
            return false;
        }
        if (characterId.equals(currentCharacterID)) {
            return true;
        }

        currentCharacterID = characterId;
        Pages = null;
        activeInvSlots.clear();
        annotationCache.clear();
        CharacterBankData.INSTANCE.load();
        if (currentOverlayType == BankOverlayType.CHARACTER) {
            currentData = CharacterBankData.INSTANCE;
        }
        WynnExtras.LOGGER.info("[WynnExtras] Synced character bank id: " + characterId);
        return true;
    }

    @SubscribeEvent
    public void onInput(KeyInputEvent event) {
        handleKeyInput(event);
    }

    @SubscribeEvent
    public void onChar(CharInputEvent event) {
        handleCharInput(event);
    }

    public static boolean handleScreenKeyPress(int key, int scanCode, int modifiers) {
        if (!LunarCompat.isLunarClient()) return false;
        if (currentOverlayType == BankOverlayType.NONE && !ClassSelectionOverlay.isTextInputActive()) return false;

        KeyInputEvent event = new KeyInputEvent(key, scanCode, InputCompat.PRESS, modifiers);
        handleKeyInput(event);
        return ClassSelectionOverlay.isTextInputActive()
                || (currentOverlayType != BankOverlayType.NONE && (BankOverlay2.isAnyTextInputFocused()
                || (InputCompat.KEY_1 <= key && key <= InputCompat.KEY_9)));
    }

    public static boolean handleScreenCharTyped(char character) {
        if (!LunarCompat.isLunarClient()) return false;
        if (currentOverlayType == BankOverlayType.NONE && !ClassSelectionOverlay.isTextInputActive()) return false;
        handleCharInput(new CharInputEvent(character));
        return ClassSelectionOverlay.isTextInputActive()
                || (currentOverlayType != BankOverlayType.NONE && BankOverlay2.isAnyTextInputFocused());
    }

    private static void handleKeyInput(KeyInputEvent event) {
        if (ClassSelectionOverlay.handleKeyInput(event)) return;
        if(event.getAction() == InputCompat.PRESS || event.getAction() == InputCompat.REPEAT) {
            BankOverlay2.handleKeyPressed(event.getKey(), event.getScanCode(), event.getModifiers());
        }
        if(activeTextInput != null) {
            activeTextInput.onInput(event);
        }
    }

    private static void handleCharInput(CharInputEvent event) {
        if (ClassSelectionOverlay.handleCharInput(event)) return;
        // Don't insert character if Ctrl is held (it's a shortcut like Ctrl+V)
        if (!isCtrlHeld()) {
            BankOverlay2.handleCharTyped(event.getCharacter());
        }
        if(activeTextInput != null && !isCtrlHeld()) {
            activeTextInput.onCharInput(event);
        }
    }

    private static boolean isCtrlHeld() {
        long window = net.minecraft.client.Minecraft.getInstance().getWindow().handle();
        return julianh06.wynnextras.compat.client.InputCompat.keyState(window, InputCompat.KEY_LEFT_CONTROL) == InputCompat.PRESS
            || julianh06.wynnextras.compat.client.InputCompat.keyState(window, InputCompat.KEY_RIGHT_CONTROL) == InputCompat.PRESS;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        BankOverlay2.saveCurrentPlayerInventorySnapshot();
        updateLastHeldWeapon();

        if(expectedOverlayType == BankOverlayType.NONE) return;
        if(expectedOverlayType == currentOverlayType) {
            activeInvSlots.clear();
            annotationCache.clear();
            expectedOverlayType = BankOverlayType.NONE;
            return;
        }
        updateOverlayType();
    }

    private static void updateLastHeldWeapon() {
        long now = System.currentTimeMillis();
        if (now - lastHeldWeaponCheckMs < 1000) return;
        lastHeldWeaponCheckMs = now;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) return;
        if (!MinecraftUtils.isOnWynncraft()) return;
        boolean syncedCharacterId = syncCurrentCharacterId();
        if (!syncedCharacterId && !hasValidCurrentCharacterId()) return;

        ItemStack held = client.player.getMainHandItem();
        boolean detectedWeapon = isWeapon(held);
        if (!detectedWeapon) return;
        ItemStack weapon = held;

        String key = currentCharacterID + "|" + getStackKey(weapon);
        if (key.equals(lastPersistedHeldWeaponKey)) return;

        CharacterBankData.saveLastHeldWeaponAsync(currentCharacterID, weapon);
        CrossClassBankSearch.invalidateClassSelectionWeaponCache();
        lastPersistedHeldWeaponKey = key;
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        return WynnItemParser.parse(stack).map(item -> item.gearType().isWeapon()).orElse(false);
    }

    private static String getStackKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "empty";
        return stack.getItem() + "|" + stack.getHoverName().getString() + "|" + stack.getComponents().hashCode();
    }

    public static void updateOverlayType() {
        if (BankOverlay2.isReadOnlyViewerActive()) return;
        if (WynncraftMenuService.isCurrent(MenuType.ACCOUNT_BANK)) {
            BankOverlay.currentOverlayType = BankOverlayType.ACCOUNT;
            BankOverlay.currentData = AccountBankData.INSTANCE;
            currentMaxPages = 21;
        } else if (WynncraftMenuService.isCurrent(MenuType.CHARACTER_BANK)) {
            BankOverlay.currentOverlayType = BankOverlayType.CHARACTER;
            BankOverlay.syncCurrentCharacterId();
            BankOverlay.currentData = CharacterBankData.INSTANCE;
            currentMaxPages = 12;
        } else if (WynncraftMenuService.isCurrent(MenuType.BOOKSHELF)) {
            BankOverlay.currentOverlayType = BankOverlayType.BOOKSHELF;
            BankOverlay.currentData = BookshelfData.INSTANCE;
            currentMaxPages = 12;
        } else if (WynncraftMenuService.isCurrent(MenuType.MISC_BUCKET)) {
            BankOverlay.currentOverlayType = BankOverlayType.MISC;
            BankOverlay.currentData = MiscBucketData.INSTANCE;
            currentMaxPages = 12;
        } else {
            BankOverlay.currentOverlayType = BankOverlayType.NONE;
            BankOverlay.currentData = null;
        }
    }

    public static void registerBankOverlay() {
        WynnExtras.LOGGER.info("Registering Bankoverlay for " + WynnExtras.MOD_ID);

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof AbstractContainerScreen<?>
                    || screen instanceof julianh06.wynnextras.features.bankoverlay.BankViewerScreen) {
                // Fallback for wheel dispatch that bypasses the native MouseHandler callback.
                ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) ->
                        !BankOverlay2.handleMouseScrolled(vertical));
            }
            if (!(screen instanceof AbstractContainerScreen<?>)) return;
            ScreenKeyboardEvents.allowKeyPress(screen).register((s, input) ->
                    !handleScreenKeyPress(julianh06.wynnextras.compat.client.InputCompat.legacyKey(input.key()), input.keycode(), julianh06.wynnextras.compat.client.InputCompat.legacyModifiers(input.modifiers())));
        });

        ClientTickEvents.START_CLIENT_TICK.register((tick) -> {
            Minecraft client = Minecraft.getInstance();
            if(client.player == null || client.level == null) { return; }

            AbstractContainerMenu currScreenHandler = MinecraftUtils.containerMenu();

            Screen currScreen = MinecraftUtils.mc().gui.screen();
            if(currScreen == null) {
                resetScrollRegistration();
                return;
            }

            if(currScreenHandler == null) {
                resetScrollRegistration();
                return;
            }

            if(registeredScroll && (registeredScrollScreen != currScreen || registeredScrollSyncId != currScreenHandler.containerId)) {
                resetScrollRegistration();
            }

            if(registeredScroll) return;
            if(expectedOverlayType != BankOverlayType.NONE && expectedOverlayType != currentOverlayType) return;
            updateOverlayType();

            String InventoryTitle = currScreen.getTitle().getString();
            if(InventoryTitle == null) { return; }

            if(BankOverlay.currentOverlayType != BankOverlayType.NONE) {
                registeredScroll = true;
                registeredScrollScreen = currScreen;
                registeredScrollSyncId = currScreenHandler.containerId;
            }
            BankOverlay2.setBankSyncId(currScreenHandler.containerId);

            //most (almost all) of the functionality is in HandledScreenMixin
        });
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent event) {
        currentOverlayType = BankOverlayType.NONE;
        expectedOverlayType = BankOverlayType.NONE;
        currentData = null;
        Pages = null;
        activeInv = -1;
        currentCharacterID = null;
        activeInvSlots.clear();
        annotationCache.clear();
        heldItem = ItemStack.EMPTY;
        resetScrollRegistration();
        lastHeldWeaponCheckMs = 0;
        lastPersistedHeldWeaponKey = "";
        BankOverlay2.invalidateBagTotalCache();
        BankOverlay2.resetInteractionBlockers();
        BankOverlaySlotBridge.restoreAll();
    }
}
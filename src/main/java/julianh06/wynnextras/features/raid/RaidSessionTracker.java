package julianh06.wynnextras.features.raid;

import julianh06.wynnextras.wynncraft.state.RaidState;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.event.RaidEndedEvent;
import julianh06.wynnextras.event.api.WEEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import julianh06.wynnextras.compat.client.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import julianh06.wynnextras.compat.client.InputCompat;

import java.util.ArrayList;
import java.util.List;

public class RaidSessionTracker {

    private static final List<Session> sessions = new ArrayList<>();
    private static boolean wasInRaid = false;
    private static boolean lastRaidCompleted = false;
    private static long hudTickCounter = 0;
    private static boolean wasClicking = false;
    private static boolean dragging = false;
    private static double dragOffX = 0, dragOffY = 0;

    private static final long PAUSE_THRESHOLD_MS = 15 * 60 * 1000; // 15 minutes
    private static final int LINE_HEIGHT = 11;

    private static class Session {
        long startTime = 0;          // 0 until armed (first raid entry)
        boolean armed = false;
        int raidCount = 0;
        int failCount = 0;
        long lastRaidTime;
        long pausedTime = 0;
        boolean manuallyPaused = false;
        long manualPauseStart = 0;
        long totalRunTimeMs = 0;          // pure challenge time (no intermissions)
        long totalIntermissionMs = 0;     // raid-internal intermission between rooms
        int timedRunCount = 0;
        String cachedStatsLine = "";

        Session() {}

        void arm() {
            if (armed) return;
            armed = true;
            startTime = System.currentTimeMillis();
            lastRaidTime = startTime;
        }

        long getElapsedMs() {
            if (!armed) return 0;
            long elapsed = System.currentTimeMillis() - startTime - pausedTime;
            if (manuallyPaused) {
                elapsed -= (System.currentTimeMillis() - manualPauseStart);
            }
            return Math.max(0, elapsed);
        }

        boolean isAutoPaused() {
            if (lastRaidTime == 0) return false;
            return System.currentTimeMillis() - lastRaidTime > PAUSE_THRESHOLD_MS;
        }

        void unpause() {
            if (isAutoPaused() && !manuallyPaused) {
                long pauseStart = lastRaidTime + PAUSE_THRESHOLD_MS;
                pausedTime += System.currentTimeMillis() - pauseStart;
            }
        }

        void togglePause() {
            if (manuallyPaused) {
                pausedTime += System.currentTimeMillis() - manualPauseStart;
                manuallyPaused = false;
                manualPauseStart = 0;
            } else {
                manuallyPaused = true;
                manualPauseStart = System.currentTimeMillis();
            }
        }

        String buildStatsLine(int index) {
            julianh06.wynnextras.config.WynnExtrasConfig c = julianh06.wynnextras.config.WynnExtrasConfig.INSTANCE;
            StringBuilder sb = new StringBuilder();
            if (sessions.size() > 1) sb.append("#").append(index + 1).append(" ");

            if (!armed) {
                sb.append("Waiting for raid...");
                if (manuallyPaused) sb.append(" (Paused)");
                return sb.toString();
            }

            long elapsed = getElapsedMs();
            double hours = elapsed / 3_600_000.0;
            double runsPerHour = hours > 0.001 ? raidCount / hours : 0;
            long totalMin = elapsed / 60_000;
            long h = totalMin / 60;
            long m = totalMin % 60;
            String time = h > 0 ? h + "h " + m + "m" : m + "m";

            if (c.raidSessionShowRuns) {
                sb.append("Runs: ").append(raidCount);
                if (c.raidSessionShowFails && failCount > 0) {
                    sb.append(" (").append(failCount).append(" F)");
                }
            }
            if (c.raidSessionShowRate) {
                if (sb.length() > 0 && !sb.toString().endsWith(" ")) sb.append(" | ");
                sb.append(String.format("%.1f/hr", runsPerHour));
            }
            if (c.raidSessionShowTime) {
                if (sb.length() > 0 && !sb.toString().endsWith(" ")) sb.append(" | ");
                sb.append(time);
            }
            if (c.raidSessionShowAvgTime && timedRunCount > 0) {
                long avgMs = totalRunTimeMs / timedRunCount;
                long avgMin = avgMs / 60_000;
                long avgSec = (avgMs % 60_000) / 1000;
                if (sb.length() > 0 && !sb.toString().endsWith(" ")) sb.append(" | ");
                sb.append(String.format("avg: %d:%02d", avgMin, avgSec));
                if (totalIntermissionMs > 0) {
                    long intMs = totalIntermissionMs / timedRunCount;
                    long intMin = intMs / 60_000;
                    long intSec = (intMs % 60_000) / 1000;
                    sb.append(String.format(" (interm.: %d:%02d)", intMin, intSec));
                }
            }
            if (manuallyPaused) sb.append(" (Paused)");
            return sb.toString();
        }
    }

    private static Session primarySession() {
        return sessions.isEmpty() ? null : sessions.get(0);
    }

    public static void reset() {
        sessions.clear();
        if (MinecraftUtils.player() != null) {
            MinecraftUtils.sendMessageToClient(Component.literal("§e[Session] §fAll sessions cleared"));
        }
    }

    public static void startNewSession() {
        Session s = new Session();
        if (RaidState.isInRaid()) s.arm();
        sessions.add(s);
        if (MinecraftUtils.player() != null) {
            String suffix = sessions.size() > 1 ? " (#" + sessions.size() + ")" : "";
            String state = s.armed ? "started" : "armed — starts on first raid entry";
            MinecraftUtils.sendMessageToClient(Component.literal("§e[Session] §fNew session " + state + suffix));
        }
    }

    private static void removeSession(int index) {
        if (index >= 0 && index < sessions.size()) {
            sessions.remove(index);
            if (MinecraftUtils.player() != null) {
                MinecraftUtils.sendMessageToClient(Component.literal("§e[Session] §fRemoved session"));
            }
        }
    }

    @SubscribeEvent
    public void onRaidEnded(RaidEndedEvent event) {
        if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
        if (!(event instanceof RaidEndedEvent.Completed)) return; // fails don't count toward avg
        if (event.getRaid() == null) return;
        long runMs = event.getRaid().timeInRaid();
        if (runMs <= 0) return;

        // Split into pure challenge time vs intermissions between rooms.
        long challengeSum = 0;
        try {
            var challenges = event.getRaid().challenges();
            if (challenges != null) {
                for (RaidRoomData room : challenges.values()) {
                    if (room == null) continue;
                    long t = room.totalTime();
                    if (t > 0) challengeSum += t;
                }
            }
        } catch (Exception ignored) {}
        long pureRunMs = challengeSum > 0 ? challengeSum : runMs;
        long intermissionMs = challengeSum > 0 ? Math.max(0, runMs - challengeSum) : 0;

        for (Session s : sessions) {
            if (!s.armed || s.manuallyPaused) continue;
            s.totalRunTimeMs += pureRunMs;
            s.totalIntermissionMs += intermissionMs;
            s.timedRunCount++;
        }
    }

    public static String getStatsString() {
        Session s = primarySession();
        if (s == null) return null;
        if (!s.armed) return "Session armed — waiting for first raid";
        long elapsed = s.getElapsedMs();
        double hours = elapsed / 3_600_000.0;
        double runsPerHour = hours > 0.001 ? s.raidCount / hours : 0;
        String base = String.format("Raids: %d | %.1f/hr | Completed: %d | Failed: %d",
                s.raidCount + s.failCount, runsPerHour, s.raidCount, s.failCount);
        if (s.timedRunCount > 0) {
            long avgMs = s.totalRunTimeMs / s.timedRunCount;
            long avgMin = avgMs / 60_000;
            long avgSec = (avgMs % 60_000) / 1000;
            base += String.format(" | Avg: %d:%02d", avgMin, avgSec);
            if (s.totalIntermissionMs > 0) {
                long intMs = s.totalIntermissionMs / s.timedRunCount;
                long intMin = intMs / 60_000;
                long intSec = (intMs % 60_000) / 1000;
                base += String.format(" (interm.: %d:%02d)", intMin, intSec);
            }
        }
        return base;
    }

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
            String raw = ChatFormatting.stripFormatting(message.getString());
            if (raw == null) return;
            if (raw.contains("Raid Completed!") && !raw.contains(":")) {
                lastRaidCompleted = true;
                for (Session s : sessions) {
                    if (!s.armed) continue;
                    if (!s.manuallyPaused) s.unpause();
                    s.raidCount++;
                    s.lastRaidTime = System.currentTimeMillis();
                }
            }
            if (raw.contains("Raid Failed!") && !raw.contains(":")) {
                lastRaidCompleted = false;
                for (Session s : sessions) {
                    if (!s.armed) continue;
                    if (!s.manuallyPaused) s.unpause();
                    s.failCount++;
                    s.lastRaidTime = System.currentTimeMillis();
                }
            }
        });

        // Completion-time tracking via the raid-end event — only fires for actually
        // completed raids, so fails will never contribute to the average.
        WEEventBus.registerEventListener(new RaidSessionTracker());

        // Detect raid start + auto-start first session + refresh cached stats
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
            if (client.player == null) return;
            hudTickCounter++;
            if (hudTickCounter % 20 == 0) {
                for (int i = 0; i < sessions.size(); i++) {
                    sessions.get(i).cachedStatsLine = sessions.get(i).buildStatsLine(i);
                }
            }
            boolean inRaid = RaidState.isInRaid();
            if (inRaid && !wasInRaid) {
                if (sessions.isEmpty()) {
                    startNewSession();
                }
                for (Session s : sessions) {
                    if (!s.armed) s.arm();
                    else if (!s.manuallyPaused) s.unpause();
                }
            }
            wasInRaid = inRaid;
        });

        // Detect clicks/drags on HUD while inventory is open
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
            if (client.player == null || !(client.gui.screen() instanceof InventoryScreen)) {
                wasClicking = false;
                dragging = false;
                return;
            }
            long window = client.getWindow().handle();
            boolean clicking = InputCompat.mouseButtonState(window, InputCompat.MOUSE_BUTTON_LEFT) == InputCompat.PRESS;
            double[] mx = new double[1], my = new double[1];
            InputCompat.cursorPosition(window, mx, my);
            double scale = client.getWindow().getGuiScale();
            double mouseX = mx[0] / scale, mouseY = my[0] / scale;

            if (clicking && !wasClicking) {
                if (isOnHud(mouseX, mouseY) && !isOnButtons(mouseX, mouseY)) {
                    dragging = true;
                    dragOffX = mouseX - WynnExtrasConfig.INSTANCE.raidSessionHudX;
                    dragOffY = mouseY - WynnExtrasConfig.INSTANCE.raidSessionHudY;
                } else {
                    handleClick(mouseX, mouseY);
                }
            } else if (clicking && dragging) {
                WynnExtrasConfig.INSTANCE.raidSessionHudX = (int)(mouseX - dragOffX);
                WynnExtrasConfig.INSTANCE.raidSessionHudY = (int)(mouseY - dragOffY);
            } else if (!clicking && dragging) {
                dragging = false;
                WynnExtrasConfig.save();
            }
            wasClicking = clicking;
        });

        // HUD render — only when NO screen is open
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
            if (WynnExtrasConfig.INSTANCE.raidSessionOnlyInInventory) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gui.hud.isHidden()) return;
            if (mc.gui.screen() != null) return;
            if (WynnExtrasConfig.INSTANCE.raidSessionOnlyInRaid && !wasInRaid
                    && !RaidState.isInRaid()) return;
            renderHud(ctx, mc, false);
        });

        // Render on top of own inventory only
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof InventoryScreen)) return;
            ScreenEvents.afterExtract(screen).register((s, ctx, mouseX, mouseY, tickDelta) -> {
                if (!WynnExtrasConfig.INSTANCE.raidSessionEnabled) return;
                if (client.player == null) return;
                if (WynnExtrasConfig.INSTANCE.raidSessionOnlyInRaid && !RaidState.isInRaid()) return;
                renderHud(ctx, client, true);
            });
        });
    }

    private static int totalRows() {
        return sessions.isEmpty() ? 1 : sessions.size() + 1;
    }

    private static void renderHud(GuiGraphicsExtractor ctx, Minecraft mc, boolean showButtons) {
        float ts = WynnExtrasConfig.INSTANCE.raidSessionHudScale;
        int baseX = WynnExtrasConfig.INSTANCE.raidSessionHudX;
        int baseY = WynnExtrasConfig.INSTANCE.raidSessionHudY;

        ctx.pose().pushMatrix();
        ctx.pose().translate(baseX, baseY);
        ctx.pose().scale(ts, ts);

        if (sessions.isEmpty()) {
            if (showButtons) {
                ctx.text(mc.font, "Session: --", 0, 0, 0xFF888888, true);
                int addX = mc.font.width("Session: --  ");
                ctx.text(mc.font, "[ADD]", addX, 0, 0xFF66FF66, true);
            }
            ctx.pose().popMatrix();
            return;
        }

        for (int i = 0; i < sessions.size(); i++) {
            Session s = sessions.get(i);
            String line = s.cachedStatsLine.isEmpty() ? s.buildStatsLine(i) : s.cachedStatsLine;
            int lineColor = s.manuallyPaused ? 0xFFFFFF66 : 0xFFAAFFAA;
            int y = i * LINE_HEIGHT;

            ctx.text(mc.font, line, 0, y, lineColor, true);
            if (showButtons) {
                int btnX = mc.font.width(line + "  ");
                ctx.text(mc.font, "[X]", btnX, y, 0xFFFF6666, true);
                String pauseLabel = s.manuallyPaused ? "[>]" : "[||]";
                int pauseX = btnX + mc.font.width("[X] ");
                ctx.text(mc.font, pauseLabel, pauseX, y, 0xFFFFFF66, true);
            }
        }

        if (showButtons) {
            int addY = sessions.size() * LINE_HEIGHT;
            ctx.text(mc.font, "[ADD]", 0, addY, 0xFF66FF66, true);
        }

        ctx.pose().popMatrix();
    }

    private static boolean isOnHud(double mouseX, double mouseY) {
        float ts = WynnExtrasConfig.INSTANCE.raidSessionHudScale;
        int baseX = WynnExtrasConfig.INSTANCE.raidSessionHudX;
        int baseY = WynnExtrasConfig.INSTANCE.raidSessionHudY;
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) return false;

        int totalH = (int)(totalRows() * LINE_HEIGHT * ts);
        if (mouseY < baseY || mouseY > baseY + totalH) return false;

        int maxWidth = 0;
        if (sessions.isEmpty()) {
            maxWidth = mc.font.width("Session: --  [ADD]");
        } else {
            for (int i = 0; i < sessions.size(); i++) {
                Session s = sessions.get(i);
                String line = s.buildStatsLine(i);
                String pauseLabel = s.manuallyPaused ? "[>]" : "[||]";
                int w = mc.font.width(line + "  [X] " + pauseLabel);
                if (w > maxWidth) maxWidth = w;
            }
            int addW = mc.font.width("[ADD]");
            if (addW > maxWidth) maxWidth = addW;
        }
        return mouseX >= baseX && mouseX <= baseX + (int)(maxWidth * ts);
    }

    private static boolean isOnButtons(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) return false;
        float ts = WynnExtrasConfig.INSTANCE.raidSessionHudScale;
        int baseX = WynnExtrasConfig.INSTANCE.raidSessionHudX;
        int baseY = WynnExtrasConfig.INSTANCE.raidSessionHudY;

        if (sessions.isEmpty()) {
            int h = (int)(LINE_HEIGHT * ts);
            if (mouseY < baseY || mouseY > baseY + h) return false;
            int addX = baseX + (int)(mc.font.width("Session: --  ") * ts);
            int addEnd = addX + (int)(mc.font.width("[ADD]") * ts);
            return mouseX >= addX && mouseX <= addEnd;
        }

        for (int i = 0; i < sessions.size(); i++) {
            int rowY = baseY + (int)(i * LINE_HEIGHT * ts);
            int rowH = (int)(LINE_HEIGHT * ts);
            if (mouseY >= rowY && mouseY < rowY + rowH) {
                Session s = sessions.get(i);
                String line = s.buildStatsLine(i);
                String pauseLabel = s.manuallyPaused ? "[>]" : "[||]";
                int btnStart = baseX + (int)(mc.font.width(line + "  ") * ts);
                int btnEnd = btnStart + (int)(mc.font.width("[X] " + pauseLabel) * ts);
                return mouseX >= btnStart && mouseX <= btnEnd;
            }
        }

        int addRowY = baseY + (int)(sessions.size() * LINE_HEIGHT * ts);
        int addRowH = (int)(LINE_HEIGHT * ts);
        if (mouseY >= addRowY && mouseY < addRowY + addRowH) {
            int addEnd = baseX + (int)(mc.font.width("[ADD]") * ts);
            return mouseX >= baseX && mouseX <= addEnd;
        }

        return false;
    }

    private static void handleClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font == null) return;

        float ts = WynnExtrasConfig.INSTANCE.raidSessionHudScale;
        int baseX = WynnExtrasConfig.INSTANCE.raidSessionHudX;
        int baseY = WynnExtrasConfig.INSTANCE.raidSessionHudY;

        if (sessions.isEmpty()) {
            int h = (int)(LINE_HEIGHT * ts);
            if (mouseY < baseY || mouseY > baseY + h) return;
            int addX = baseX + (int)(mc.font.width("Session: --  ") * ts);
            int addW = (int)(mc.font.width("[ADD]") * ts);
            if (mouseX >= addX && mouseX <= addX + addW) {
                startNewSession();
            }
            return;
        }

        for (int i = 0; i < sessions.size(); i++) {
            int rowY = baseY + (int)(i * LINE_HEIGHT * ts);
            int rowH = (int)(LINE_HEIGHT * ts);
            if (mouseY < rowY || mouseY >= rowY + rowH) continue;

            Session s = sessions.get(i);
            String line = s.buildStatsLine(i);
            String pauseLabel = s.manuallyPaused ? "[>]" : "[||]";

            int xBtnX = baseX + (int)(mc.font.width(line + "  ") * ts);
            int xBtnW = (int)(mc.font.width("[X]") * ts);
            if (mouseX >= xBtnX && mouseX <= xBtnX + xBtnW) {
                removeSession(i);
                return;
            }

            int pauseX = xBtnX + (int)(mc.font.width("[X] ") * ts);
            int pauseW = (int)(mc.font.width(pauseLabel) * ts);
            if (mouseX >= pauseX && mouseX <= pauseX + pauseW) {
                s.togglePause();
                String state = s.manuallyPaused ? "Paused" : "Resumed";
                if (MinecraftUtils.player() != null) {
                    String label = sessions.size() > 1 ? " (#" + (i + 1) + ")" : "";
                    MinecraftUtils.sendMessageToClient(Component.literal("§e[Session] §f" + state + label));
                }
                return;
            }
            return;
        }

        int addRowY = baseY + (int)(sessions.size() * LINE_HEIGHT * ts);
        int addRowH = (int)(LINE_HEIGHT * ts);
        if (mouseY >= addRowY && mouseY < addRowY + addRowH) {
            int addW = (int)(mc.font.width("[ADD]") * ts);
            if (mouseX >= baseX && mouseX <= baseX + addW) {
                startNewSession();
            }
        }
    }
}

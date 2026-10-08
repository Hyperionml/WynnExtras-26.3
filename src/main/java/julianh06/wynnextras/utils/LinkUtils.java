package julianh06.wynnextras.utils;

import julianh06.wynnextras.core.WynnExtras;
import net.minecraft.util.Util;

public class LinkUtils {
    public static void openLink(String url) {
        try {
            com.mojang.blaze3d.Blaze3D.openUri(java.net.URI.create(url));
        } catch (Exception e) {
            WynnExtras.LOGGER.error("[WynnExtras] Error while opening link: " + e.getMessage());
        }
    }
}

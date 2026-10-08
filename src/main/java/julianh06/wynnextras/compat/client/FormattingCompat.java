package julianh06.wynnextras.compat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;

public final class FormattingCompat {
    private FormattingCompat() {}

    public static Integer color(ChatFormatting formatting) {
        TextColor color = formatting == null ? null : TextColor.fromLegacyFormat(formatting);
        return color == null ? null : color.getValue();
    }

    public static boolean isColor(ChatFormatting formatting) {
        return color(formatting) != null;
    }

    public static boolean isFormat(ChatFormatting formatting) {
        return formatting != null && formatting != ChatFormatting.RESET && !isColor(formatting);
    }
}

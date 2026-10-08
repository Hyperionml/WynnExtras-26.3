package julianh06.wynnextras.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;

// TODO: fail proof sendMessage for when localPlayer is null
public class ChatUtils {
    public static void sendMessage(Component message) {
        julianh06.wynnextras.compat.client.ClientMessages.display(message, false);
    }

    public static void sendMessage(String message) {
        sendMessage(message, ChatFormatting.RESET);
    }

    public static void sendMessage(String message, ChatFormatting... formats) {
        sendGenericMessage(message, false, formats);
    }

    public static void sendActionbarMessage(String message) {
        sendActionbarMessage(message, ChatFormatting.RESET);
    }

    public static void sendActionbarMessage(String message, ChatFormatting... formats) {
        sendGenericMessage(message, true, formats);
    }

    public static void displayTitle(String title, int duration) {
        displayTitle(title, "", duration, 10, 10, ChatFormatting.RESET);
    }

    public static void displayTitle(String title, String subtitle, int duration) {
        displayTitle(title, subtitle, duration, 10, 10, ChatFormatting.RESET);
    }

    public static void displayTitle(String title, String subtitle, int duration, int fadeInTicks, int fadeOutTicks, ChatFormatting... formats) {
        MutableComponent titleComponent = Component.literal(title);
        MutableComponent subtitleComponent = Component.literal(subtitle);

        titleComponent = titleComponent.withStyle(formats);
        subtitleComponent = subtitleComponent.withStyle(formats);

        MinecraftUtils.localNetworkHandler().setTitlesAnimation(new ClientboundSetTitlesAnimationPacket(fadeInTicks, duration, fadeOutTicks));

        if (!subtitle.isEmpty()) {
            MinecraftUtils.localNetworkHandler().setSubtitleText(new ClientboundSetSubtitleTextPacket(subtitleComponent));
        }
        if (!title.isEmpty()) {
            MinecraftUtils.localNetworkHandler().setTitleText(new ClientboundSetTitleTextPacket(titleComponent));
        }
    }

    public static void displayTitle(String title, String subtitle, int duration, ChatFormatting... formats) {
        displayTitle(title, subtitle, duration, 10, 10, formats);
    }

    private static void sendGenericMessage(String message, Boolean actionbar, ChatFormatting... formats) {
        MutableComponent component = Component.literal(message);

        component = component.withStyle(formats);

        julianh06.wynnextras.compat.client.ClientMessages.display(component, actionbar);
    }

    public static MutableComponent concatComponents(Iterable<? extends Component> components) {
        return concatComponents("", components);
    }

    public static MutableComponent concatComponents(String separator, Iterable<? extends Component> components) {
        MutableComponent result = Component.empty();
        for (Component component : components) {
            if (!result.getString().isEmpty()) {
                result.append(separator);
            }
            result.append(component);
        }

        return result;
    }
}
package julianh06.wynnextras.utils;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public final class TooltipUtils {
    private TooltipUtils() {}

    public static List<ClientTooltipComponent> getClientTooltipComponent(List<Component> lines) {
        if (lines == null || lines.isEmpty()) return List.of();
        return lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }

    public static int getTooltipHeight(List<ClientTooltipComponent> components) {
        if (components == null || components.isEmpty()) return 0;
        Font renderer = MinecraftUtils.mc().font;
        int height = 0;
        for (ClientTooltipComponent component : components) height += component.getHeight(renderer);
        return height;
    }

}

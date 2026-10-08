package julianh06.wynnextras.utils.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class StyledText {
    public static final StyledText EMPTY = fromString("");

    private final Component component;

    private StyledText(Component component) {
        this.component = component == null ? Component.empty() : component;
    }

    public static StyledText fromComponent(Component component) {
        return new StyledText(component);
    }

    public static StyledText fromString(String value) {
        return new StyledText(Component.literal(value == null ? "" : value));
    }

    public Component getComponent() {
        return component;
    }

    public String getString() {
        return component.getString();
    }

    public StyledText withoutFormatting() {
        return fromString(ChatFormatting.stripFormatting(getString()));
    }

    @Override
    public String toString() {
        return getString();
    }
}

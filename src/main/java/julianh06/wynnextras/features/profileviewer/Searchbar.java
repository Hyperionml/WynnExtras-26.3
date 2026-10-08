package julianh06.wynnextras.features.profileviewer;

import julianh06.wynnextras.utils.colors.CustomColor;
import net.minecraft.sounds.SoundEvents;
import julianh06.wynnextras.utils.MinecraftUtils;
import julianh06.wynnextras.utils.UI.TextInputWidget;
import julianh06.wynnextras.compat.client.InputCompat;

public class Searchbar extends TextInputWidget {
    public Searchbar(int x, int y, int height, int width) {
        super(x, y, width, height, 9, 6, 3f);
        setBackgroundColor(null);
        setTextColor(CustomColor.fromHexString("FFFFFF"));
        setPlaceholderColor(CustomColor.fromHexString("AAAAAA"));
        setCursorColor(CustomColor.fromHexString("FFFFFF"));
        setSelectionColor(CustomColor.fromInt(0xAA3366CC));
    }

    public void click() {
        MinecraftUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
        setFocused(true);
    }

    public void setActive(boolean active) {
        setFocused(active);
    }

    public boolean isActive() {
        return isFocused();
    }

    public void setSearchText(String value) {
        setPlaceholder(value);
    }

    public void setInputAndKeepStartVisible(String value) {
        setInput(value);
        cursorPos = 0;
        selectionAnchor = 0;
        horizontalTextOffset = 0;
    }

    public void setX(int value) {
        setPosition(value, getY());
    }

    public void setY(int value) {
        setPosition(getX(), value);
    }

    public void setWidth(int value) {
        setSize(value, getHeight());
    }

    public void setHeight(int value) {
        setSize(getWidth(), value);
    }

    public boolean isClickInBounds(int mouseX, int mouseY) {
        return mouseX >= getX()
                && mouseY >= getY()
                && mouseX < getX() + getWidth()
                && mouseY < getY() + getHeight();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == InputCompat.MOUSE_BUTTON_RIGHT && isClickInBounds((int) mx, (int) my)) {
            clearInput();
            MinecraftUtils.playSoundUI(SoundEvents.UI_BUTTON_CLICK.value());
            setFocused(true);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }
}

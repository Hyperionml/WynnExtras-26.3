package julianh06.wynnextras.features.raid;

import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import julianh06.wynnextras.event.KeyInputEvent;
import julianh06.wynnextras.features.inventory.BankOverlay;
import julianh06.wynnextras.utils.overlays.EasyTextInput;
import julianh06.wynnextras.compat.client.InputCompat;

import java.util.concurrent.atomic.AtomicLong;

public class RaidListFilter extends EasyTextInput {
    public RaidListFilter(int x, int y, int height, int width) {
        super(x, y, height, width);
    }

    public void setSearchText(String value) {
        super.searchText = value;
    }

    @Override
    public void onInput(KeyInputEvent event) {
        if(!isActive) return;

        AtomicLong now = new AtomicLong();
        int action = event.getAction();
        int key = event.getKey();
        int scancode = event.getScanCode();
        //char character = event.getCharacter();

        now.set(System.currentTimeMillis());

        if (action == InputCompat.RELEASE) {
            cooldowns.remove(key);
        } else {
            // Backspace
            if (key == InputCompat.KEY_BACKSPACE && cursorPos > 0) {
                input = removeAt(cursorPos, input);
                cursorPos--;
            }
            // Delete
            else if (key == InputCompat.KEY_DELETE && cursorPos < input.length()) {
                input = removeAt(cursorPos + 1, input);
            }
            // left arrow
            else if (key == InputCompat.KEY_LEFT && cursorPos > 0) {
                cursorPos--;
            }
            // right arrow
            else if (key == InputCompat.KEY_RIGHT && cursorPos < input.length()) {
                cursorPos++;
            }
        }
    }

    @Override
    public void drawWithTexture(GuiGraphicsExtractor context, Identifier texture) {
        long now = System.currentTimeMillis();
        RenderUtils.drawTexturedRect(context, texture, CustomColor.NONE, x, y, width, height, (int) width, (int) height);
        if(input.isEmpty() && !isActive) {
            context.text(Minecraft.getInstance().font, searchText, x + 4, y + 3, CustomColor.fromHexString("FFFFFF").asInt());
        } else {
            if(cursorPos > input.length()) {
                cursorPos = input.length();
            }
            context.text(Minecraft.getInstance().font, input, x + 4, y + 3, CustomColor.fromHexString("FFFFFF").asInt());
            if(now - lastBlink > 500) {
                blinkToggle = !blinkToggle;
                lastBlink = now;
            }
            if(blinkToggle && isActive) RenderUtils.drawLine(context, CustomColor.fromHexString("FFFFFF"), x + 4 + Minecraft.getInstance().font.width(input.substring(0, cursorPos)), y + 2, x + 4 + Minecraft.getInstance().font.width(input.substring(0, cursorPos)), y + 11, 1);
        }
    }
}

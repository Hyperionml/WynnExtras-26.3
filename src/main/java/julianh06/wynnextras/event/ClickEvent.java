package julianh06.wynnextras.event;

import julianh06.wynnextras.event.api.WEEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import julianh06.wynnextras.compat.client.InputCompat;

public class ClickEvent extends WEEvent {
    private static boolean wasLeftPressed = false;

    public double mouseX;
    public double mouseY;

    public ClickEvent(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isLeftPressed = InputCompat.mouseButtonState(client.getWindow().handle(), InputCompat.MOUSE_BUTTON_LEFT) == InputCompat.PRESS;

            if (isLeftPressed && !wasLeftPressed) {
                new ClickEvent(client.mouseHandler.xpos(), client.mouseHandler.ypos()).post();
            }

            wasLeftPressed = isLeftPressed;
        });
    }
}

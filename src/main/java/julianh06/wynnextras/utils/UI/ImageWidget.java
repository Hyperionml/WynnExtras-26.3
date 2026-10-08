package julianh06.wynnextras.utils.UI;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class ImageWidget extends Widget{
    Identifier image;

    public ImageWidget(Identifier image, int x, int y, int width, int height, UIUtils ui) {
        super(x, y, width, height);
        // set default action
        this.ui = ui;
        this.image = image;
    }

    public ImageWidget(Identifier image, UIUtils ui) {
        this(image, 0, 0, 0, 0, ui);
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float tickDelta) {
        ui.drawImage(image, x, y, width, height);
    }
}

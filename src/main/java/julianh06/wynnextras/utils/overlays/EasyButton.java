package julianh06.wynnextras.utils.overlays;

import julianh06.wynnextras.utils.colors.CustomColor;
import julianh06.wynnextras.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class EasyButton extends EasyElement{
    @Nullable public String buttonText;
    CustomColor color = CustomColor.fromHexString("FFFFFF");

    public EasyButton(int x, int y, int height, int width) {
        super(x, y, height, width);
    }

    public EasyButton(int x, int y, int height, int width, @Nullable String text) {
        this(x, y, height, width);
        buttonText = text;
    }

    @Override
    public void click() {}

    @Override
    public void draw(GuiGraphicsExtractor context) {RenderUtils.drawRect(context, CustomColor.fromHexString("FFFFFF"), x, y, width, height);}

    public void drawWithTexture(GuiGraphicsExtractor context, Identifier texture) {
        if(texture != null) {
            RenderUtils.drawTexturedRect(context, texture, CustomColor.NONE, x, y, width, height, (int) width, (int) height);
        }
        if(buttonText == null) {
            return;
        }
        context.text(Minecraft.getInstance().font, buttonText, x + 2, y + 3, CustomColor.fromHexString("FFFFFF").asInt());
    }
}

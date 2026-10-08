package julianh06.wynnextras.features.inventory.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.nio.file.Path;

public class BookshelfData extends BankData {
    public static final BookshelfData INSTANCE = new BookshelfData();

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("wynnextras/" + Minecraft.getInstance().player.getUUID().toString() + "/bookshelf.json");
    }
}

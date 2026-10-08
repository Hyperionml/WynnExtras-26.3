package julianh06.wynnextras.features.inventory.data;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import java.nio.file.Path;

public class AccountBankData extends BankData {
    public static final AccountBankData INSTANCE = new AccountBankData();

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("wynnextras/" + Minecraft.getInstance().player.getUUID().toString() + "/account_bank.json");
    }
}

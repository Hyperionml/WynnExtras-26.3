package julianh06.wynnextras.features.render;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public class PlayerRenderFilter {
    private static final Set<UUID> hiddenPlayers = new HashSet<>();

    public static void hide(Player player) {
        hiddenPlayers.add(player.getUUID());
    }

    public static void show(Player player) {
        hiddenPlayers.remove(player.getUUID());
    }

    public static boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUUID());
    }
}

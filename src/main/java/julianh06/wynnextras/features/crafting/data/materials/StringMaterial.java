package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum StringMaterial implements IMaterial {
    WHEAT("Wheat", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_wheat.png")),
    BARLEY("Barley", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_barley.png")),
    OAT("Oat", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_oat.png")),
    MALT("Malt", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_barley.png")),
    HOPS("Hops", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_hops.png")),
    RYE("Rye", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_rye.png")),
    MILLET("Millet", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_millet.png")),
    DECAY("Decay", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_decay.png")),
    RICE("Rice", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_rice.png")),
    SORGHUM("Sorghum", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_sorghum.png")),
    HEMP("Hemp", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_hemp.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_dernic.png")),
    JUTE("Jute", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_jute.png")),
    HEATHER("Heather", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/farming/string_heather.png"));

    private final String name;
    private final Identifier serverTexture;

    StringMaterial(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " String";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

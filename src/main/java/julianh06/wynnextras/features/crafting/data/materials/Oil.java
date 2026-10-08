package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Oil implements IMaterial {
    GUDGEON("Gudgeon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_gudgeon.png")),
    TROUT("Trout", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_trout.png")),
    SALMON("Salmon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_salmon.png")),
    CARP("Carp", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_carp.png")),
    ICEFISH("Icefish", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_ice.png")),
    PIRANHA("Piranha", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_piranha.png")),
    KOI("Koi", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_koi.png")),
    GYLIA("Gylia", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_gylia.png")),
    BASS("Bass", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_bass.png")),
    MOLTEN("Molten", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_molten.png")),
    STARFISH("Starfish", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_star.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_dernic.png")),
    STURGEON("Sturgeon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_sturgeon.png")),
    MAHSEER("Mahseer", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/oil_mahseer.png"));

    private final String name;
    private final Identifier serverTexture;

    Oil(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Oil";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

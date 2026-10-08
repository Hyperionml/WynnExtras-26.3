package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Meat implements IMaterial {
    GUDGEON("Gudgeon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_gudgeon.png")),
    TROUT("Trout", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_trout.png")),
    SALMON("Salmon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_salmon.png")),
    CARP("Carp", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_carp.png")),
    ICEFISH("Icefish", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_ice.png")),
    PIRANHA("Piranha", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_piranha.png")),
    KOI("Koi", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_koi.png")),
    GYLIA("Gylia", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_gylia.png")),
    BASS("Bass", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_bass.png")),
    MOLTEN("Molten", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_molten.png")),
    STARFISH("Starfish", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_star.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_dernic.png")),
    STURGEON("Sturgeon", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_sturgeon.png")),
    MAHSEER("Mahseer", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/fishing/meat_mahseer.png"));

    private final String name;
    private final Identifier serverTexture;

    Meat(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Meat";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

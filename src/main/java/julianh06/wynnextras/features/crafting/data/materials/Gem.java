package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Gem implements IMaterial {
    COPPER("Copper", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_copper.png")),
    GRANITE("Granite", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_granite.png")),
    GOLD("Gold", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_gold.png")),
    SANDSTONE("Sandstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_sandstone.png")),
    IRON("Iron", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_iron.png")),
    SILVER("Silver", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_silver.png")),
    COBALT("Cobalt", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_cobalt.png")),
    KANDERSTONE("Kanderstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_kander.png")),
    DIAMOND("Diamond", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_diamond.png")),
    MOLTEN("Molten", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_molten.png")),
    VOIDSTONE("Voidstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_void.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_dernic.png")),
    TITANIUM("Titanium", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_titanium.png")),
    CINNABAR("Cinnabar", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/gem_cinnabar.png"));

    private final String name;
    private final Identifier serverTexture;

    Gem(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Gem";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

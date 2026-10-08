package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Ingot implements IMaterial {
    COPPER("Copper", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_copper.png")),
    GRANITE("Granite", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_granite.png")),
    GOLD("Gold", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_gold.png")),
    SANDSTONE("Sandstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_sandstone.png")),
    IRON("Iron", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_iron.png")),
    SILVER("Silver", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_silver.png")),
    COBALT("Cobalt", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_cobalt.png")),
    KANDERSTONE("Kanderstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_kanderstone.png")),
    DIAMOND("Diamond", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_diamond.png")),
    MOLTEN("Molten", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_molten.png")),
    VOIDSTONE("Voidstone", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_void.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_dernic.png")),
    TITANIUM("Titanium", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_titanium.png")),
    CINNABAR("Cinnabar", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/mining/ingot_cinnabar.png"));

    private final String name;
    private final Identifier serverTexture;

    Ingot(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Ingot";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

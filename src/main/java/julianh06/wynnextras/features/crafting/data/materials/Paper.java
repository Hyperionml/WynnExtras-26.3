package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Paper implements IMaterial {
    OAK("Oak", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_oak.png")),
    BIRCH("Birch", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_birch.png")),
    WILLOW("Willow", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_willow.png")),
    ACACIA("Acacia", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_acacia.png")),
    SPRUCE("Spruce", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_spruce.png")),
    JUNGLE("Jungle", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_jungle.png")),
    DARK("Dark", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_dark.png")),
    LIGHT("Light", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_light.png")),
    PINE("Pine", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_pine.png")),
    AVO("Avo", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_avo.png")),
    SKY("Sky", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_sky.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_dernic.png")),
    MAPLE("Maple", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_maple.png")),
    REDWOOD("Redwood", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/paper_redwood.png"));

    private final String name;
    private final Identifier serverTexture;

    Paper(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Paper";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

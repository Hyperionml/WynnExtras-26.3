package julianh06.wynnextras.features.crafting.data.materials;

import julianh06.wynnextras.features.crafting.data.IMaterial;
import julianh06.wynnextras.features.crafting.data.MaterialTextureResolver;
import net.minecraft.resources.Identifier;

public enum Plank implements IMaterial {
    OAK("Oak", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_oak.png")),
    BIRCH("Birch", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_birch.png")),
    WILLOW("Willow", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_willow.png")),
    ACACIA("Acacia", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_acacia.png")),
    SPRUCE("Spruce", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_spruce.png")),
    JUNGLE("Jungle", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_jungle.png")),
    DARK("Dark", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_dark.png")),
    LIGHT("Light", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_light.png")),
    PINE("Pine", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_pine.png")),
    AVO("Avo", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_avo.png")),
    SKY("Sky", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_sky.png")),
    DERNIC("Dernic", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_dernic.png")),
    MAPLE("Maple", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_maple.png")),
    REDWOOD("Redwood", Identifier.fromNamespaceAndPath("minecraft", "textures/wynn/economy/woodcutting/plank_redwood.png"));

    private final String name;
    private final Identifier serverTexture;

    Plank(String name, Identifier serverTexture) {
        this.name = name;
        this.serverTexture = serverTexture;
    }

    @Override
    public String getName() {
        return name + " Plank";
    }

    @Override
    public Identifier getTexture() {
        return MaterialTextureResolver.resolve(serverTexture);
    }
}

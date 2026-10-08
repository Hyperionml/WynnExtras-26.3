package julianh06.wynnextras.mixin;

import julianh06.wynnextras.features.spellhider.*;
import julianh06.wynnextras.core.WynnExtras;
import julianh06.wynnextras.features.inventory.MountColorBackground;
import julianh06.wynnextras.mixin.Accessor.ItemRenderStateAccessor;
import julianh06.wynnextras.utils.ItemUtils;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(ItemModelResolver.class)
public class ItemModelManagerMixin {
    @ModifyVariable(method = "appendItemLayers", at = @At("HEAD"), argsOnly = true)
    private ItemStack addMountColorBackground(ItemStack stack) {
        return MountColorBackground.apply(stack);
    }

    @Inject(method = "updateForNonLiving", at = @At("TAIL"))
    public void updateModelsForNonLiving(ItemStackRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Entity entity, CallbackInfo ci) {
        if (!(entity instanceof Display.ItemDisplay)) return;
        if (stack.getItem() != Items.OAK_BOAT) return;

        Float modelData = ItemUtils.getFirsCustomModelDataFloat(stack);

        boolean loggerIdle = ModelDataLogger.isIdle();
        if (loggerIdle && modelData != null && SpellHider.isModelRegistered(modelData.intValue())) return;

        Set<Identifier> fileNames = getFileNames(renderState);
        if (fileNames == null) {
            return;
        }
        for (Identifier fileName : fileNames) {
            if (modelData != null) {
                SpellHider.addModel(fileName.getPath(), modelData);
            }

            SpellNamespace spellMapping = SpellHiderMappings.INSTANCE.getSpellMapping(fileName);
            if (spellMapping == null || spellMapping.isEmpty()) {
                SpellData fromPath = SpellHider.getFromPath(fileName.getPath());
                if (fromPath == null) {
                    return;
                }
                ModelDataLogger.addTextToRender(fromPath.getHash(), entity.position());
                ModelDataLogger.handleUnknownModel(modelData, fileNames);
            } else {
                SpellHider.addName(fileName.getPath(), spellMapping.getFQName());
                ModelDataLogger.addTextToRender(spellMapping.getFQName(), entity.position());
            }
        }

        if (loggerIdle && modelData != null && SpellHider.getFromModel(modelData.intValue()) != null) {
            SpellHider.markModelRegistered(modelData.intValue());
        }
    }

    @Unique
    private static Set<Identifier> getFileNames(ItemStackRenderState renderState) {
        ItemRenderStateAccessor renderStateAccess = (ItemRenderStateAccessor) renderState;
        int layerCount = renderStateAccess.getLayerCount();
        ItemStackRenderState.LayerRenderState[] layers = renderStateAccess.getLayers();
        if (layers == null || layerCount == 0) {
            return null;
        }

        Set<Identifier> names = new HashSet<>();
        for (int i = 0; i < layerCount; i++)
            ((julianh06.wynnextras.mixin.Accessor.ItemLayerRenderStateAccessor) layers[i]).getQuads().all().forEach(q -> names.add(q.materialInfo().sprite().contents().name()));
        return names;
    }
}
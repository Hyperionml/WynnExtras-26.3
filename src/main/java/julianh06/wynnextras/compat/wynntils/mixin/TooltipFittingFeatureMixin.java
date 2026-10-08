package julianh06.wynnextras.compat.wynntils.mixin;

import julianh06.wynnextras.utils.TooltipUtils;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import julianh06.wynnextras.config.WynnExtrasConfig;
import julianh06.wynnextras.features.inventory.WeightDisplay;
import julianh06.wynnextras.utils.LunarCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "com.wynntils.features.tooltips.TooltipFittingFeature", remap = false)
public class TooltipFittingFeatureMixin {
    @Redirect(
            method = "onTooltipPre",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/wynntils/utils/mc/TooltipUtils;getClientTooltipComponent(Ljava/util/List;)Ljava/util/List;"
            ),
            require = 0
    )
    private List<ClientTooltipComponent> redirectGetClientTooltipComponent(List<Component> components) {
        if (LunarCompat.isLunarClient()) return TooltipUtils.getClientTooltipComponent(components);

        var currentHoveredStack = WeightDisplay.getCurrentHoveredStack();
        if (!WynnExtrasConfig.INSTANCE.showWeight || currentHoveredStack == null)
            return TooltipUtils.getClientTooltipComponent(components);

        if (!WeightDisplay.isTrackedMythic(currentHoveredStack))
            return TooltipUtils.getClientTooltipComponent(components);

        String cleanName = WeightDisplay.extractCleanName(currentHoveredStack);
        WeightDisplay.ItemData scaleData = WeightDisplay.weightCacheByHash.get(currentHoveredStack.getComponents().hashCode());
        WeightDisplay.ItemData itemData = WeightDisplay.getSelectedItemData(cleanName);
        if (scaleData == null || scaleData.data().isEmpty() || itemData == null) return TooltipUtils.getClientTooltipComponent(components);

        int idx = Math.min(itemData.index(), scaleData.data().size() - 1);

        List<Component> expanded = new ArrayList<>(components);
        for (int i = 0; i < scaleData.data().size(); i++) expanded.add(Component.empty());
        for (int i = 0; i < WeightDisplay.getScaleSourceHeaderCount(cleanName); i++) expanded.add(Component.empty());
        if (scaleData.data().size() > 1) expanded.add(Component.empty());
        if (WeightDisplay.shouldShowScaleSourceControls(cleanName)) {
            expanded.add(Component.empty());
            expanded.add(Component.empty());
        }
        // per-stat weight lines
        if (WynnExtrasConfig.INSTANCE.showScales) {
            WeightDisplay.WeightData profile = itemData.data().get(idx);
            for (Component line : components) {
                String[] parts = WeightDisplay.extractStatFromLine(line.getString());
                if (parts == null) continue;
                String apiName = WeightDisplay.resolveIdentKey(parts[0], parts[1])[0];
                if (profile.identifications().getOrDefault(apiName, 0f) != 0f) expanded.add(Component.empty());
            }
        }
        return TooltipUtils.getClientTooltipComponent(expanded);

        //this is still not perfect but better than the tooltip being cut off
    }
}

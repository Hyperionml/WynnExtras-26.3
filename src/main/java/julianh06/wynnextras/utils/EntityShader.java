package julianh06.wynnextras.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;

public final class EntityShader {
    public static Integer activeShader = null;

    private EntityShader() {}

    public static int[] mixedTintLayers(int[] tintLayers, List<BakedQuad> quads, int shader) {
        int maxTintIndex = 0;
        for (BakedQuad q : quads) {
            if (q.materialInfo().isTinted()) maxTintIndex = Math.max(maxTintIndex, q.materialInfo().tintIndex());
        }
        int len = Math.max(Math.max(tintLayers == null ? 0 : tintLayers.length, maxTintIndex + 1), 1);
        int[] out = tintLayers == null ? new int[len] : Arrays.copyOf(tintLayers, len);
        for (int i = 0; i < out.length; i++) {
            int color = tintLayers != null && i < tintLayers.length ? tintLayers[i] : -1;
            out[i] = ARGB.multiply(color, shader);
        }
        return out;
    }

    public static List<BakedQuad> quadsWithDefaultTintIndex(List<BakedQuad> quads) {
        List<BakedQuad> out = new ArrayList<>(quads.size());
        for (BakedQuad q : quads) {
            int tintIndex = q.materialInfo().isTinted() ? q.materialInfo().tintIndex() : 0;
            out.add(new BakedQuad(
                q.position0(), q.position1(), q.position2(), q.position3(),
                q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(),
                q.direction(), new BakedQuad.MaterialInfo(
                    q.materialInfo().sprite(), q.materialInfo().layer(), q.materialInfo().itemRenderType(),
                    q.materialInfo().itemGlintRenderType(), q.materialInfo().itemGlintSpecialRenderType(),
                    tintIndex, q.materialInfo().shadeDirectionOverride(), q.materialInfo().lightEmission())
            ));
        }
        return out;
    }
}

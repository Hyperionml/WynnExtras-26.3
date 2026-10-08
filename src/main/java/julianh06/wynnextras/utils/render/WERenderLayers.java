package julianh06.wynnextras.utils.render;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class WERenderLayers {
    private static final ConcurrentHashMap<Integer, RenderType> linesCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, RenderType> linesThroughWallsCache = new ConcurrentHashMap<>();


    public static RenderPipeline linePipeline = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("wynnextras_lines")
            .withVertexShader("shaders/vertex/lines.vsh")
            .withFragmentShader("shaders/fragment/lines.fsh")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.LINES)
            .build();


    private static RenderType createLineRenderLayer(int lineWidth, boolean throughWalls) {
        return RenderType.create(
                "wynnextras_lines_" + lineWidth + (throughWalls ? "_xray" : ""),
                RenderSetup.builder(linePipeline)
                        .sortOnUpload()

                        .setLayeringTransform(throughWalls ? LayeringTransform.NO_LAYERING : LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup()
        );
    }

    public static RenderType getLines(int lineWidth, boolean throughWalls) {
        ConcurrentHashMap<Integer, RenderType> cache = throughWalls ? linesThroughWallsCache : linesCache;
        return cache.computeIfAbsent(lineWidth, lw -> createLineRenderLayer(lw, throughWalls));
    }

    public static RenderType getFilled(boolean throughWalls) {
        return throughWalls ? FILLED_XRAY : FILLED;
    }

    private static final RenderType FILLED = RenderType.create(
            "wynnextras_filled",
            RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX)
                    .sortOnUpload()

                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup()
    );

    private static final RenderType FILLED_XRAY = RenderType.create(
            "wynnextras_filled_xray",
            RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX)
                    .sortOnUpload()

                    .setLayeringTransform(LayeringTransform.NO_LAYERING)
                    .createRenderSetup()
    );

}

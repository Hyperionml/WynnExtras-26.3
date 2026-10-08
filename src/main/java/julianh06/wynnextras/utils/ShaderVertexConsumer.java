package julianh06.wynnextras.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;

public final class ShaderVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float rMul;
    private final float gMul;
    private final float bMul;
    private final float aMul;

    private ShaderVertexConsumer(VertexConsumer delegate, int shaderColor) {
        this.delegate = delegate;
        this.rMul = ARGB.red(shaderColor) / 255.0f;
        this.gMul = ARGB.green(shaderColor) / 255.0f;
        this.bMul = ARGB.blue(shaderColor) / 255.0f;
        this.aMul = ARGB.alpha(shaderColor) / 255.0f;
    }

    public static VertexConsumer wrap(VertexConsumer delegate, int shaderColor) {
        return new ShaderVertexConsumer(delegate, shaderColor);
    }

    private int tint(int channel, float mul) {
        return Math.min(255, Math.round(channel * mul));
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        delegate.setColor(tint(red, rMul), tint(green, gMul), tint(blue, bMul), tint(alpha, aMul));
        return this;
    }

    @Override
    public VertexConsumer setColor(int argb) {
        int r = tint(ARGB.red(argb), rMul);
        int g = tint(ARGB.green(argb), gMul);
        int b = tint(ARGB.blue(argb), bMul);
        int a = tint(ARGB.alpha(argb), aMul);
        delegate.setColor(ARGB.color(a, r, g, b));
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) { delegate.setUv3(u,v); return this; }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}

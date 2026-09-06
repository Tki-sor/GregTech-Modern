package brachy.modularui.drawable.schema;

import net.minecraft.core.SectionPos;
import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.ParametersAreNonnullByDefault;

/** Translates section-local liquid vertices into absolute block coordinates. */
@ParametersAreNonnullByDefault
public class LiquidVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final SectionPos sectionPos;

    public LiquidVertexConsumer(VertexConsumer delegate, SectionPos sectionPos) {
        this.delegate = delegate;
        this.sectionPos = sectionPos;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return delegate.addVertex(x + sectionPos.x() * SectionPos.SECTION_SIZE,
                y + sectionPos.y() * SectionPos.SECTION_SIZE,
                z + sectionPos.z() * SectionPos.SECTION_SIZE);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setColor(int color) {
        return delegate.setColor(color);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return delegate.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return delegate.setNormal(x, y, z);
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        return delegate.setLineWidth(width);
    }
}

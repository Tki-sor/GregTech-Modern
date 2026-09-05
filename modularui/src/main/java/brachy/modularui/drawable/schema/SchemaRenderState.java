package brachy.modularui.drawable.schema;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/** GUI extraction state for a schema's 3D Picture-in-Picture render. */
@OnlyIn(Dist.CLIENT)
public final class SchemaRenderState implements PictureInPictureRenderState {
    private final BaseSchemaRenderer renderer;
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final float scale;
    private final Matrix3x2f pose;
    private final @Nullable ScreenRectangle scissorArea;
    private final @Nullable ScreenRectangle bounds;
    private final float partialTick;

    public SchemaRenderState(BaseSchemaRenderer renderer, int x0, int y0, int x1, int y1, float scale,
                             Matrix3x2f pose, @Nullable ScreenRectangle scissorArea, float partialTick) {
        this.renderer = renderer;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.scale = scale;
        this.pose = new Matrix3x2f(pose);
        this.scissorArea = scissorArea;
        ScreenRectangle transformedBounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(this.pose);
        this.bounds = scissorArea != null ? scissorArea.intersection(transformedBounds) : transformedBounds;
        this.partialTick = partialTick;
    }

    public BaseSchemaRenderer renderer() {
        return renderer;
    }

    public float partialTick() {
        return partialTick;
    }

    @Override
    public int x0() {
        return x0;
    }

    @Override
    public int y0() {
        return y0;
    }

    @Override
    public int x1() {
        return x1;
    }

    @Override
    public int y1() {
        return y1;
    }

    @Override
    public float scale() {
        return scale;
    }

    @Override
    public Matrix3x2f pose() {
        return pose;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return bounds;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SchemaRenderState other)) return false;
        return renderer == other.renderer && x0 == other.x0 && y0 == other.y0 && x1 == other.x1 && y1 == other.y1
                && Float.compare(scale, other.scale) == 0;
    }

    @Override
    public int hashCode() {
        int result = System.identityHashCode(renderer);
        result = 31 * result + x0;
        result = 31 * result + y0;
        result = 31 * result + x1;
        result = 31 * result + y1;
        result = 31 * result + Float.hashCode(scale);
        return result;
    }
}

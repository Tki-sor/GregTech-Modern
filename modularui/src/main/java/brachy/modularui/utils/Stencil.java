package brachy.modularui.utils;

import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.widget.sizer.Area;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A transformed clipping stack for GUI drawables.
 *
 * <p>The target GUI renderer records elements before drawing them, so the old OpenGL stencil pass cannot be flushed from
 * this utility. Rectangular clipping is delegated to the extractor's scissor stack; custom shapes retain their bounds
 * and use the same rectangular fallback.</p>
 */
public class Stencil {

    private static final ObjectArrayList<Area> stencils = new ObjectArrayList<>();
    private static final ObjectArrayList<Runnable> stencilShapes = new ObjectArrayList<>();

    private final GuiContext context;

    @ApiStatus.Internal
    public Stencil(GuiContext context) {
        this.context = context;
    }

    public static void reset() {
        stencils.clear();
        stencilShapes.clear();
    }

    public void push(@NotNull Rectangle area) {
        push(area.x, area.y, area.width, area.height);
    }

    public void pushAtZero(@NotNull Rectangle area) {
        push(0, 0, area.width, area.height);
    }

    public void push(float x, float y, float w, float h) {
        push(() -> {}, (int) Math.floor(x), (int) Math.floor(y), (int) Math.ceil(w), (int) Math.ceil(h));
    }

    public void push(Runnable stencilShape, boolean hideStencilShape) {
        push(stencilShape, 0, 0, 0, 0, hideStencilShape);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h) {
        push(stencilShape, x, y, w, h, true);
    }

    public void push(Runnable stencilShape, int x, int y, int w, int h, boolean hideStencilShape) {
        Area scissor = new Area(x, y, w, h);
        scissor.transformAndRectanglerize(this.context);
        if (!stencils.isEmpty()) {
            stencils.top().clamp(scissor);
        }
        stencils.add(scissor);
        stencilShapes.add(stencilShape);
        applyScissor(scissor);
    }

    private void applyScissor(Area area) {
        if (this.context.getGraphics() != null) {
            this.context.getGraphics().enableScissor(area.x, area.y, area.x + area.width, area.y + area.height);
        }
    }

    public void pop() {
        if (stencils.isEmpty()) {
            throw new IllegalStateException("Tried to pop an empty stencil stack!");
        }
        stencils.pop();
        stencilShapes.pop();
        if (this.context.getGraphics() != null) {
            this.context.getGraphics().disableScissor();
            if (!stencils.isEmpty()) {
                applyScissor(stencils.top());
            }
        }
    }

    public static boolean isInsideScissorArea(Area area, IViewportStack stack) {
        if (stencils.isEmpty()) return true;
        Area.SHARED.set(0, 0, area.width, area.height);
        Area.SHARED.transformAndRectanglerize(stack);
        return stencils.top().intersects(Area.SHARED);
    }
}

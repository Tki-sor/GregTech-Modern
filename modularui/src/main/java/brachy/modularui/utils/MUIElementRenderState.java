package brachy.modularui.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** A small adapter for custom MUI geometry recorded by GuiGraphicsExtractor. */
public final class MUIElementRenderState implements GuiElementRenderState {

    private final RenderPipeline pipeline;
    private final ScreenRectangle bounds;
    private final @Nullable ScreenRectangle scissorArea;
    private final Consumer<VertexConsumer> vertexBuilder;

    public MUIElementRenderState(RenderType renderType, ScreenRectangle bounds,
                                 @Nullable ScreenRectangle scissorArea, Consumer<VertexConsumer> vertexBuilder) {
        this.pipeline = renderType.pipeline();
        this.bounds = bounds;
        this.scissorArea = scissorArea;
        this.vertexBuilder = vertexBuilder;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        this.vertexBuilder.accept(vertexConsumer);
    }

    @Override
    public RenderPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return this.scissorArea;
    }

    @Override
    public ScreenRectangle bounds() {
        return this.bounds;
    }
}

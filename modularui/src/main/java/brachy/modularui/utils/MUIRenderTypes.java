package brachy.modularui.utils;

import brachy.modularui.ModularUI;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class MUIRenderTypes {

    private static final RenderPipeline GUI_TEXTURED_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(ModularUI.id("pipeline/gui_textured"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();
    private static final RenderPipeline GUI_TRIANGLE_STRIP_PIPELINE = createGuiPipeline(
            "gui_triangle_strip", VertexFormat.Mode.TRIANGLE_STRIP);
    private static final RenderPipeline GUI_TRIANGLE_FAN_PIPELINE = createGuiPipeline(
            "gui_triangle_fan", VertexFormat.Mode.TRIANGLE_FAN);
    private static final RenderPipeline GUI_POSITION_QUADS_PIPELINE = createGuiPipeline(
            "gui_position_quads", VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
    private static final RenderPipeline GUI_POSITION_COLOR_QUADS_PIPELINE = createGuiPipeline(
            "gui_position_color_quads", VertexFormat.Mode.QUADS);
    private static final RenderPipeline GUI_POSITION_TRIANGLE_FAN_PIPELINE = createGuiPipeline(
            "gui_position_triangle_fan", VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
    private static final RenderPipeline GUI_POSITION_TRIANGLE_STRIP_PIPELINE = createGuiPipeline(
            "gui_position_triangle_strip", VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);

    private static final Function<Identifier, RenderType> GUI_TEXTURE = texture -> RenderType.create(
            "gui_texture",
            RenderSetup.builder(GUI_TEXTURED_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                    .createRenderSetup());
    private static final RenderType GUI_TRIANGLE_STRIP = createRenderType(
            "gui_triangle_strip", GUI_TRIANGLE_STRIP_PIPELINE);
    private static final RenderType GUI_TRIANGLE_FAN = createRenderType(
            "gui_triangle_fan", GUI_TRIANGLE_FAN_PIPELINE);
    private static final RenderType GUI_OVERLAY_TRIANGLE_FAN = createRenderType(
            "gui_overlay_triangle_fan", GUI_TRIANGLE_FAN_PIPELINE);
    private static final RenderType GUI_POSITION_QUADS = createRenderType(
            "gui_position_quads", GUI_POSITION_QUADS_PIPELINE);
    private static final RenderType GUI_POSITION_COLOR_QUADS = createRenderType(
            "gui_position_color_quads", GUI_POSITION_COLOR_QUADS_PIPELINE);
    private static final RenderType GUI_POSITION_TRIANGLE_FAN = createRenderType(
            "gui_position_triangle_fan", GUI_POSITION_TRIANGLE_FAN_PIPELINE);
    private static final RenderType GUI_POSITION_TRIANGLE_STRIP = createRenderType(
            "gui_position_triangle_strip", GUI_POSITION_TRIANGLE_STRIP_PIPELINE);

    private MUIRenderTypes() {}

    private static RenderPipeline createGuiPipeline(String name, VertexFormat.Mode mode) {
        return createGuiPipeline(name, mode, DefaultVertexFormat.POSITION_COLOR);
    }

    private static RenderPipeline createGuiPipeline(String name, VertexFormat.Mode mode, VertexFormat format) {
        return RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                .withLocation(ModularUI.id("pipeline/" + name))
                .withVertexFormat(format, mode)
                .build();
    }

    private static RenderType createRenderType(String name, RenderPipeline pipeline) {
        return RenderType.create(name, RenderSetup.builder(pipeline)
                .bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
                .createRenderSetup());
    }

    public static RenderType guiTexture(Identifier texture) {
        return GUI_TEXTURE.apply(texture);
    }

    public static RenderType guiTriangleStrip() {
        return GUI_TRIANGLE_STRIP;
    }

    public static RenderType guiTriangleFan() {
        return GUI_TRIANGLE_FAN;
    }

    public static RenderType guiOverlayTriangleFan() {
        return GUI_OVERLAY_TRIANGLE_FAN;
    }

    public static RenderType guiPositionQuads() {
        return GUI_POSITION_QUADS;
    }

    public static RenderType guiPositionColorQuads() {
        return GUI_POSITION_COLOR_QUADS;
    }

    public static RenderType guiPositionTriangleFan() {
        return GUI_POSITION_TRIANGLE_FAN;
    }

    public static RenderType guiPositionTriangleStrip() {
        return GUI_POSITION_TRIANGLE_STRIP;
    }
}

package brachy.modularui.drawable.schema;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

/** Renders schema scene submissions into the GUI PIP color/depth target. */
@OnlyIn(Dist.CLIENT)
public final class SchemaPictureInPictureRenderer extends PictureInPictureRenderer<SchemaRenderState> {
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("schema");

    public SchemaPictureInPictureRenderer(net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<SchemaRenderState> getRenderStateClass() {
        return SchemaRenderState.class;
    }

    @Override
    protected void renderToTexture(SchemaRenderState state, PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        BaseSchemaRenderer renderer = state.renderer();
        int guiScale = minecraft.getWindow().getGuiScale();
        float width = Math.max(1, (state.x1() - state.x0()) * guiScale);
        float height = Math.max(1, (state.y1() - state.y0()) * guiScale);

        Matrix4f projectionMatrix = renderer.createProjectionMatrix(width, height);
        RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(projectionMatrix),
                renderer.isIsometric() ? ProjectionType.ORTHOGRAPHIC : ProjectionType.PERSPECTIVE);
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);

        poseStack.setIdentity();
        poseStack.mulPose(renderer.createViewMatrix(new Matrix4f()));

        FeatureRenderDispatcher dispatcher = minecraft.gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage storage = dispatcher.getSubmitNodeStorage();
        renderer.renderWorld(poseStack, storage, state.partialTick());
        dispatcher.renderAllFeatures();
    }

    @Override
    public void close() {
        super.close();
        projectionBuffer.close();
    }

    @Override
    protected String getTextureLabel() {
        return "schema";
    }
}

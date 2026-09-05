package brachy.modularui.drawable.schema;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.Icon;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.MatrixUtils;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.SchemaWidget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A schema drawable with a real 26.1 render-state based 3D backend. */
@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {

    @Getter private final ISchema schema;
    @Getter private final Camera camera = new Camera();
    @Getter private final RenderLevel renderLevel;
    private final Viewport viewport = new Viewport();
    private final List<Vector3f> pos = new ArrayList<>();
    @Getter private BlockHitResult lastRayTrace;
    @Getter private RenderFilter renderFilter = RenderFilter.ALL;
    @Getter private final Matrix4f projection = new Matrix4f();
    @Getter private final Vector3f openGLMousePos = new Vector3f();
    @Getter @Setter private boolean captureDebugInfo;
    private boolean compiling;
    private boolean completed;
    private boolean canceled = true;

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = Objects.requireNonNull(schema);
        this.renderLevel = new RenderLevel(schema, (blockPos, blockState) -> this.renderFilter.shouldRender(blockPos, blockState));
    }

    public void notifyRecompile() {
        this.completed = false;
        this.canceled = true;
    }

    protected void cancelCompilation() {
        this.compiling = false;
        this.canceled = true;
    }

    public boolean isCompiling() {
        return this.compiling;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    protected void recompile() {
        this.compiling = false;
        this.completed = true;
        this.canceled = false;
    }

    public void dispose() {
        cancelCompilation();
    }

    public SchemaWidget asWidget() {
        return new SchemaWidget(this);
    }

    public Icon asIcon() {
        return new Icon(this);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiGraphicsExtractor graphics = context.getGraphics();
        if (graphics == null || width <= 0 || height <= 0) return;

        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();
        onSetupCamera();
        Area screenArea = context.getScreenArea();
        int transformedX = context.transformX(x, y) + screenArea.x();
        int transformedY = context.transformY(x, y) + screenArea.y();
        this.viewport.calculateOpenGLViewportFromRectangle(transformedX, transformedY, width, height);
        setupCamera(width, height);

        graphics.fill(x, y, x + width, y + height, getClearColor());
        graphics.submitPictureInPictureRenderState(new SchemaRenderState(
                this, x, y, x + width, y + height, 1.0f,
                graphics.pose(), graphics.peekScissorStack(), context.getRenderPartialTicks()));

        if (doRayTrace() || captureDebugInfo) {
            BlockHitResult result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX, mouseY, width, height);
            }
            if (result == null || result.getType() != HitResult.Type.BLOCK) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(createWorldRenderPose(), result);
            }
            this.lastRayTrace = result;
        }

        if (captureDebugInfo) {
            drawProjectedBlockPos(graphics, width, height);
        }

        this.completed = true;
        this.canceled = false;
        onRendered();
    }

    public void drawProjectedBlockPos(GuiGraphicsExtractor graphics, int width, int height) {
        for (Vector3f point : this.pos) {
            float x = this.viewport.unscaleXFromViewport(point.x, width);
            float y = this.viewport.unscaleYFromViewport(point.y, height);
            GuiDraw.drawRect(graphics, x - 1, y - 1, 2, 2, Color.withAlpha(Color.BLUE.main, 1f));
        }
    }

    public void drawBlockOutlines(MultiBufferSource.BufferSource bufferSource) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.lines());
        PoseStack poseStack = createWorldRenderPose();
        for (var entry : this.schema) {
            BlockPos blockPos = entry.getKey();
            BlockState blockState = entry.getValue();
            if (!this.renderFilter.shouldRender(blockPos, blockState) || blockState.isAir()) continue;
            ShapeRenderer.renderShape(poseStack, buffer, blockState.getShape(this.renderLevel, blockPos),
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(), Color.RED.main, 1.0f);
        }
        bufferSource.endBatch(RenderTypes.lines());
    }

    public PoseStack createWorldRenderPose() {
        PoseStack pose = new PoseStack();
        pose.translate(-camera.pos().x, -camera.pos().y, -camera.pos().z);
        return pose;
    }

    /** Compatibility entry point for callers that already own a buffer source. */
    public void renderWorld(MultiBufferSource.BufferSource bufferSource, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        SubmitNodeStorage storage = minecraft.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage();
        renderWorld(createWorldRenderPose(), storage, partialTick);
        minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
    }

    /** Submit the schema scene to the target 26.1 feature collector. */
    void renderWorld(PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);

        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(
                minecraft.options.ambientOcclusion().get(), true, minecraft.getBlockColors());
        FluidRenderer fluidRenderer = new FluidRenderer(minecraft.getModelManager().getFluidStateModelSet());

        for (ChunkSectionLayer layer : ChunkSectionLayer.values()) {
            RenderType renderType = movingRenderType(layer);
            collector.submitCustomGeometry(poseStack, renderType, (renderPose, buffer) -> {
                VertexConsumer transformed = new PoseVertexConsumer(buffer, renderPose);
                for (var entry : this.schema) {
                    BlockPos blockPos = entry.getKey().immutable();
                    BlockState blockState = this.renderLevel.getBlockState(blockPos);
                    if (blockState.isAir()) continue;

                    if (blockState.getRenderShape() == RenderShape.MODEL) {
                        BlockStateModel model = minecraft.getModelManager().getBlockStateModelSet().get(blockState);
                        blockRenderer.tesselateBlock(
                                (x, y, z, quad, instance) -> {
                                    if (quad.materialInfo().layer() == layer) {
                                        transformed.putBlockBakedQuad(x, y, z, quad, instance);
                                    }
                                },
                                blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                                this.renderLevel, blockPos, blockState, model, blockState.getSeed(blockPos));
                    }
                }
            });

            collector.submitCustomGeometry(poseStack, renderType, (renderPose, buffer) -> {
                for (var entry : this.schema) {
                    BlockPos fluidPos = entry.getKey().immutable();
                    BlockState blockState = this.renderLevel.getBlockState(fluidPos);
                    FluidState fluidState = this.renderLevel.getFluidState(fluidPos);
                    if (blockState.isAir() || fluidState.isEmpty()) continue;
                    if (minecraft.getModelManager().getFluidStateModelSet().get(fluidState).layer() != layer) continue;

                    SectionOrigin origin = SectionOrigin.of(fluidPos);
                    VertexConsumer transformed = new PoseVertexConsumer(buffer, renderPose, origin.x, origin.y, origin.z);
                    fluidRenderer.tesselate(this.renderLevel, fluidPos, ignored -> transformed, blockState, fluidState);
                }
            });
        }

        if (isBEREnabled()) {
            submitBlockEntities(poseStack, collector, partialTick);
        }

        submitHighlight(poseStack, collector);

        if (captureDebugInfo) {
            collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (renderPose, buffer) -> {
                PoseStack outlinePose = new PoseStack();
                outlinePose.last().set(renderPose);
                for (var entry : this.schema) {
                    BlockPos blockPos = entry.getKey().immutable();
                    BlockState blockState = this.renderLevel.getBlockState(blockPos);
                    if (blockState.isAir()) continue;
                    ShapeRenderer.renderShape(outlinePose, buffer,
                            blockState.getShape(this.renderLevel, blockPos),
                            blockPos.getX(), blockPos.getY(), blockPos.getZ(), Color.RED.main, 1.0f);
                }
            });
        }
    }

    private void submitBlockEntities(PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockEntityRenderDispatcher dispatcher = minecraft.getBlockEntityRenderDispatcher();
        CameraRenderState cameraState = new CameraRenderState();
        cameraState.pos = new Vec3(camera.pos().x, camera.pos().y, camera.pos().z);
        cameraState.initialized = true;
        cameraState.viewRotationMatrix = createViewMatrix(new Matrix4f());
        dispatcher.prepare(cameraState.pos);

        for (var entry : this.schema) {
            BlockPos blockPos = entry.getKey().immutable();
            var blockEntity = this.renderLevel.getBlockEntity(blockPos);
            if (blockEntity == null) continue;
            BlockEntityRenderState renderState = dispatcher.tryExtractRenderState(blockEntity, partialTick, null);
            if (renderState == null) continue;

            poseStack.pushPose();
            poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            dispatcher.submit(renderState, poseStack, collector, cameraState);
            poseStack.popPose();
        }
    }

    private static RenderType movingRenderType(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        };
    }

    protected void setupCamera(int width, int height) {
        Matrix4f view = createViewMatrix(new Matrix4f());
        createProjectionMatrix(width, height).mul(view, this.projection);
    }

    protected Matrix4f createProjectionMatrix(float width, float height) {
        float aspect = width / height;
        if (isIsometric()) {
            float halfHeight = Math.max(1.0f, camera.dist() * (float) Math.tan(30.0f * Mth.DEG_TO_RAD));
            float halfWidth = halfHeight * aspect;
            return new Matrix4f().setOrtho(-halfWidth, halfWidth, halfHeight, -halfHeight,
                    -10000.0f, 10000.0f, RenderSystem.getDevice().isZZeroToOne());
        }
        return new Matrix4f().setPerspective(60.0f * Mth.DEG_TO_RAD, aspect, 0.05f, 10000.0f,
                RenderSystem.getDevice().isZZeroToOne());
    }

    Matrix4f createViewMatrix(Matrix4f destination) {
        MatrixUtils.lookAt(destination, camera.pos(), camera.lookAt());
        return destination;
    }

    protected void resetCamera() {
    }

    protected BlockHitResult rayTrace(int mouseX, int mouseY, int width, int height) {
        if (this.captureDebugInfo) {
            int i = 0;
            for (var entry : this.schema) {
                BlockPos blockPos = entry.getKey();
                BlockState blockState = entry.getValue();
                if (blockState.isAir() || !this.renderFilter.shouldRender(blockPos, blockState)) continue;
                Vector3f vector = this.pos.size() > i ? this.pos.get(i) : new Vector3f();
                this.projection.project(blockPos.getX() + 0.5f, blockPos.getY() + 0.5f, blockPos.getZ() + 0.5f,
                        this.viewport.getViewport(), vector);
                if (this.pos.size() > i) this.pos.set(i, vector);
                else this.pos.add(vector);
                i++;
            }
            while (i < this.pos.size()) this.pos.remove(i);
        }

        screenToOpenGLPos(mouseX, mouseY, width, height, 0.0f, this.openGLMousePos);
        Vector3f start = this.projection.unproject(this.openGLMousePos, this.viewport.getViewport(), new Vector3f());
        screenToOpenGLPos(mouseX, mouseY, width, height, 1.0f, this.openGLMousePos);
        Vector3f end = this.projection.unproject(this.openGLMousePos, this.viewport.getViewport(), new Vector3f());
        return this.renderLevel.clip(new ClipContext(
                new Vec3(start), new Vec3(end), ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));
    }

    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, Vector3f dest) {
        return screenToOpenGLPos(x, y, width, height, 0.0f, dest);
    }

    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, float depth, Vector3f dest) {
        this.viewport.rescaleToViewport(x, y, width, height, dest);
        dest.z = depth;
        return dest;
    }

    public Vector3f screenToWorldPos(int x, int y, int screenWidth, int screenHeight) {
        screenToOpenGLPos(x, y, screenWidth, screenHeight, 0.0f, this.openGLMousePos);
        return this.projection.unproject(this.openGLMousePos, this.viewport.getViewport(), new Vector3f());
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {
    }

    @ApiStatus.OverrideOnly
    protected void onRendered() {
    }

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(PoseStack poseStack, @NotNull BlockHitResult result) {
    }

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {
    }

    /** Submit the current hit highlight into the same PIP render target as the schema. */
    protected void submitHighlight(PoseStack poseStack, SubmitNodeCollector collector) {
    }

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(Color.WHITE.main, 0.5f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isBEREnabled() {
        return true;
    }

    public void updateRenderFilter(RenderFilter renderFilter) {
        this.renderFilter = renderFilter != null ? renderFilter : RenderFilter.ALL;
        notifyRecompile();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseSchemaRenderer that)) return false;
        return this.schema.equals(that.schema) && this.camera.equals(that.camera)
                && this.viewport.equals(that.viewport) && this.renderFilter.equals(that.renderFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.schema, this.camera, this.viewport, this.renderFilter);
    }

    private record SectionOrigin(int x, int y, int z) {
        static SectionOrigin of(BlockPos pos) {
            return new SectionOrigin(pos.getX() & -16, pos.getY() & -16, pos.getZ() & -16);
        }
    }

    private static final class PoseVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final PoseStack.Pose pose;
        private final float offsetX;
        private final float offsetY;
        private final float offsetZ;

        private PoseVertexConsumer(VertexConsumer delegate, PoseStack.Pose pose) {
            this(delegate, pose, 0.0f, 0.0f, 0.0f);
        }

        private PoseVertexConsumer(VertexConsumer delegate, PoseStack.Pose pose, float offsetX, float offsetY, float offsetZ) {
            this.delegate = delegate;
            this.pose = pose;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(pose, x + offsetX, y + offsetY, z + offsetZ);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            delegate.setColor(color);
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
        public VertexConsumer setNormal(float x, float y, float z) {
            Vector3f normal = pose.transformNormal(x, y, z, new Vector3f());
            delegate.setNormal(normal.x, normal.y, normal.z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }
}

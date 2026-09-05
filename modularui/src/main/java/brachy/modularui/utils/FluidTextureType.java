package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.block.FluidModel;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.BiFunction;

public enum FluidTextureType {

    STILL,
    FLOWING,
    OVERLAY;

    private static final Identifier WATER_STILL = Identifier.withDefaultNamespace("block/water_still");

    public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions) {
        return map(fluidTypeExtensions, FluidStack.EMPTY);
    }

    public TextureAtlasSprite map(IClientFluidTypeExtensions fluidTypeExtensions, FluidStack fluidStack) {
        if (!fluidStack.isEmpty()) {
            FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                    .get(fluidStack.getFluid().defaultFluidState());
            return switch (this) {
                case STILL -> model.stillMaterial().sprite();
                case FLOWING -> model.flowingMaterial().sprite();
                case OVERLAY -> model.overlayMaterial() != null ? model.overlayMaterial().sprite() : model.stillMaterial().sprite();
            };
        }
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(WATER_STILL);
    }
}

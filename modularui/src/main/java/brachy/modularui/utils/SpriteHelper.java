package brachy.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeStorage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.data.AtlasIds;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

public class SpriteHelper {

    public static TextureAtlasSprite getSpriteOfBlockState(BlockState blockState, Direction facing) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        return getBestTexture(model, blockState, facing);
    }

    public static List<BakedQuad> getQuadsOfBlockState(BlockState blockState, Direction facing) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);
        return parts.stream().flatMap(part -> part.getQuads(facing).stream()).toList();
    }

    public static TextureAtlasSprite getBestTexture(BlockStateModel model, @Nullable BlockState blockState,
                                                     @Nullable Direction facing) {
        List<BakedQuad> quads = new ArrayList<>();
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);
        parts.forEach(part -> quads.addAll(part.getQuads(facing)));
        if (quads.isEmpty()) {
            return Minecraft.getInstance().getAtlasManager()
                    .getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
        } else {
            return quads.getFirst().materialInfo().sprite();
        }
    }

    public static TextureAtlasSprite getSpriteOfItem(ItemStack item) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForNonLiving(
                renderState, item, ItemDisplayContext.GUI, null);
        var material = renderState.pickParticleMaterial(RandomSource.create());
        return material != null ? material.sprite() : Minecraft.getInstance().getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
    }

    public static List<BakedQuad> getQuadsOfItem(ItemStack item) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForNonLiving(
                renderState, item, ItemDisplayContext.GUI, null);
        SubmitNodeStorage submits = new SubmitNodeStorage();
        renderState.submit(new PoseStack(), submits, 15728880, 0, 0);
        return submits.order(0).getItemSubmits().stream()
                .flatMap(submit -> submit.quads().stream())
                .toList();
    }
}

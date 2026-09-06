package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.VoidFluidHandler;

import com.mojang.serialization.MapCodec;

import java.util.Arrays;
import java.util.stream.Stream;

public final class FluidContainerIngredient implements ICustomIngredient {

    public static final MapCodec<FluidContainerIngredient> CODEC = FluidIngredient.CODEC.fieldOf("fluid")
            .xmap(FluidContainerIngredient::new, value -> value.fluid);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC = FluidIngredient.STREAM_CODEC
            .map(FluidContainerIngredient::new, value -> value.fluid);

    private final FluidIngredient fluid;

    public FluidContainerIngredient(FluidIngredient fluid) {
        this.fluid = fluid;
    }

    public FluidContainerIngredient(FluidStack fluidStack) {
        this(FluidIngredient.of(fluidStack));
    }

    public FluidContainerIngredient(TagKey<Fluid> tag, int amount) {
        this(FluidIngredient.of(tag, amount, null));
    }

    public FluidIngredient getFluid() {
        return fluid;
    }

    public ItemStack[] getItems() {
        return Arrays.stream(fluid.getStacks()).map(FluidUtil::getFilledBucket).filter(stack -> !stack.isEmpty())
                .toArray(ItemStack[]::new);
    }

    public ItemStack getExtractedStack(ItemStack input) {
        FluidActionResult result = FluidUtil.tryEmptyContainer(input, VoidFluidHandler.INSTANCE, fluid.getAmount(), null, true);
        return result.isSuccess() ? result.getResult() : input;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return FluidUtil.getFluidContained(stack)
                .map(value -> fluid.test(value) && value.getAmount() >= fluid.getAmount()).orElse(false)
                && FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, fluid.getAmount(), null, false).isSuccess();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Arrays.stream(getItems()).filter(stack -> !stack.isEmpty()).map(stack -> stack.getItem().builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.FLUID_CONTAINER.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof FluidContainerIngredient other && fluid.equals(other.fluid);
    }

    @Override
    public int hashCode() {
        return fluid.hashCode();
    }
}

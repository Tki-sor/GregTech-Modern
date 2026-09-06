package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;
import com.gregtechceu.gtceu.utils.IngredientUtils;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.stream.Stream;

/** An item ingredient whose required count is sampled when a recipe run starts. */
public final class IntProviderIngredient implements ICustomIngredient, IRangedIngredient<SizedIngredient> {

    public static final MapCodec<IntProviderIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(IntProviderIngredient::getInner),
            IntProvider.CODEC.fieldOf("count_provider").forGetter(IntProviderIngredient::getCountProvider),
            Codec.INT.optionalFieldOf("sampled_count", -1).forGetter(IntProviderIngredient::getSampledCount))
            .apply(instance, IntProviderIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IntProviderIngredient> STREAM_CODEC = StreamCodec.composite(
            IngredientUtils.STREAM_CODEC, IntProviderIngredient::getInner,
            ByteBufCodecs.fromCodecWithRegistries(IntProvider.CODEC), IntProviderIngredient::getCountProvider,
            ByteBufCodecs.VAR_INT, IntProviderIngredient::getSampledCount,
            IntProviderIngredient::new);

    private final Ingredient inner;
    private final IntProvider countProvider;
    private int sampledCount;

    public IntProviderIngredient(Ingredient inner, IntProvider countProvider) {
        this(inner, countProvider, -1);
    }

    public IntProviderIngredient(Ingredient inner, IntProvider countProvider, int sampledCount) {
        Preconditions.checkArgument(countProvider.getMinValue() >= 0,
                "IntProviderIngredient must have a min value of at least 0.");
        this.inner = inner;
        this.countProvider = countProvider;
        this.sampledCount = sampledCount;
    }

    public static Ingredient of(Ingredient inner, IntProvider countProvider) {
        return new IntProviderIngredient(inner, countProvider).toVanilla();
    }

    public static Ingredient of(ItemStack stack, IntProvider countProvider) {
        return of(SizedIngredient.getInner(SizedIngredient.create(stack)), countProvider);
    }

    public static IntProviderIngredient get(Ingredient ingredient) {
        return ingredient.isCustom() && ingredient.getCustomIngredient() instanceof IntProviderIngredient provider
                ? provider : null;
    }

    public IntProviderIngredient copy() {
        return new IntProviderIngredient(inner, countProvider, sampledCount);
    }

    public Ingredient getInner() {
        return inner;
    }

    public IntProvider getCountProvider() {
        return countProvider;
    }

    public int getSampledCount() {
        return sampledCount;
    }

    public void setSampledCount(int sampledCount) {
        this.sampledCount = sampledCount;
    }

    @Override
    public boolean test(ItemStack stack) {
        return inner.test(stack);
    }

    public ItemStack[] getItems() {
        GTCEu.LOGGER.warn("Cannot get items of a ranged ingredient!");
        return new ItemStack[0];
    }

    public ItemStack getMaxSizeStack() {
        ItemStack[] items = IngredientUtils.getItems(inner);
        return items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(countProvider.getMaxValue());
    }

    @Override
    public int rollSampledCount(RandomSource random) {
        if (!isRolled()) sampledCount = countProvider.sample(random);
        return sampledCount;
    }

    @Override
    public SizedIngredient collapse() {
        IRangedIngredient.super.collapse();
        return new SizedIngredient(inner, rollSampledCount());
    }

    public void reset() {
        sampledCount = -1;
    }

    @Override
    public Stream<Holder<net.minecraft.world.item.Item>> items() {
        return inner.items();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.INT_PROVIDER.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IntProviderIngredient other && inner.equals(other.inner)
                && countProvider.equals(other.countProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner, countProvider);
    }
}

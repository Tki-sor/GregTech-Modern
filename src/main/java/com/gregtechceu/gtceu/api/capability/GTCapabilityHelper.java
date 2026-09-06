package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.item.component.ISpoilableItem;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import com.gregtechceu.gtceu.api.transfer.GTMTransferAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("DataFlowIssue")
public class GTCapabilityHelper {

    @Nullable
    public static IElectricItem getElectricItem(ItemStack itemStack) {
        return itemStack.getCapability(GTCapability.CAPABILITY_ELECTRIC_ITEM);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergyItem(ItemStack itemStack) {
        EnergyHandler handler = itemStack.getCapability(Capabilities.Energy.ITEM);
        return handler == null ? null : GTMTransferAdapters.energyStorage(handler);
    }

    @Nullable
    public static EnergyHandler getEnergyHandler(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.Energy.ITEM);
    }

    @Nullable
    public static IItemHandler getItemHandler(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);
        return handler == null ? null : GTMTransferAdapters.itemHandler(handler);
    }

    @Nullable
    public static ResourceHandler<ItemResource> getItemResourceHandler(Level level, BlockPos pos,
                                                                        @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    @Nullable
    public static IFluidHandler getFluidHandler(Level level, BlockPos pos, @Nullable Direction side) {
        ResourceHandler<FluidResource> handler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return handler == null ? null : GTMTransferAdapters.fluidHandler(handler);
    }

    @Nullable
    public static ResourceHandler<FluidResource> getFluidResourceHandler(Level level, BlockPos pos,
                                                                          @Nullable Direction side) {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    @Nullable
    public static IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, pos, side);
    }

    @Nullable
    public static IEnergyInfoProvider getEnergyInfoProvider(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, pos, side);
    }

    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_COVERABLE, pos, side);
    }

    @Nullable
    public static IWorkable getWorkable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_WORKABLE, pos, side);
    }

    @Nullable
    public static IControllable getControllable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_CONTROLLABLE, pos, side);
    }

    @Nullable
    public static IEnergyStorage getForgeEnergy(Level level, BlockPos pos, @Nullable Direction side) {
        EnergyHandler handler = level.getCapability(Capabilities.Energy.BLOCK, pos, side);
        return handler == null ? null : GTMTransferAdapters.energyStorage(handler);
    }

    @Nullable
    public static EnergyHandler getEnergyHandler(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Energy.BLOCK, pos, side);
    }

    @Nullable
    public static ILaserContainer getLaser(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_LASER, pos, side);
    }

    @Nullable
    public static IOpticalComputationProvider getOpticalComputationProvider(Level level, BlockPos pos,
                                                                            @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, pos, side);
    }

    @Nullable
    public static IDataAccessHatch getDataAccess(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, pos, side);
    }

    @Nullable
    public static IHazardParticleContainer getHazardContainer(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, pos, side);
    }

    @Nullable
    public static IMonitorComponent getMonitorComponent(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(GTCapability.CAPABILITY_MONITOR_COMPONENT, pos, side);
    }

    @Nullable
    public static MedicalConditionTracker getMedicalConditionTracker(@NotNull Entity entity) {
        return GTCapability.CAPABILITY_MEDICAL_CONDITION_TRACKER.getCapability(entity, null);
    }

    @Nullable
    public static ISpoilableItem getSpoilable(ItemStack stack) {
        return stack.getCapability(GTCapability.CAPABILITY_SPOILABLE_ITEM);
    }
}

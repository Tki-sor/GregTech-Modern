package com.gregtechceu.gtceu.api.transfer;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Representative transaction semantics for the GTM transfer layer: simulate vs execute,
 * commit vs rollback, and empty/full boundaries. Sided access is covered by capability
 * query wiring (Direction context) and is exercised through GameTests in task 16.
 */
class GTMTransferAdaptersTest {

    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void itemInsertExecutesOnCommit() {
        CustomItemStackHandler handler = new CustomItemStackHandler(2);
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(0, ItemResource.of(Items.DIRT), 10, transaction);
            assertEquals(10, inserted);
            transaction.commit();
        }
        assertEquals(10, handler.getStackInSlot(0).getCount());
    }

    @Test
    void itemInsertRollsBackWithoutCommit() {
        CustomItemStackHandler handler = new CustomItemStackHandler(2);
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(0, ItemResource.of(Items.DIRT), 10, transaction);
            assertEquals(10, inserted);
        }
        assertTrue(handler.getStackInSlot(0).isEmpty(), "aborted insert must roll back");
    }

    @Test
    void itemInsertRespectsFullBoundary() {
        CustomItemStackHandler handler = new CustomItemStackHandler(2);
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(64, handler.insert(0, ItemResource.of(Items.DIRT), 128, transaction));
            assertEquals(0, handler.insert(0, ItemResource.of(Items.DIRT), 10, transaction));
            transaction.commit();
        }
        assertEquals(64, handler.getStackInSlot(0).getCount());
    }

    @Test
    void itemExtractRollsBackWithoutCommit() {
        CustomItemStackHandler handler = new CustomItemStackHandler(2);
        handler.setStackInSlot(0, new ItemStack(Items.DIRT, 32));
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(32, handler.extract(0, ItemResource.of(Items.DIRT), 32, transaction));
        }
        assertEquals(32, handler.getStackInSlot(0).getCount());
    }

    @Test
    void fluidInsertExecutesOnCommitAndRollsBackOnAbort() {
        CustomFluidTank tank = new CustomFluidTank(1000);
        FluidResource water = FluidResource.of(Fluids.WATER);
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(600, tank.insert(0, water, 600, transaction));
            transaction.commit();
        }
        assertEquals(600, tank.getFluidAmount());

        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(400, tank.insert(0, water, 400, transaction));
        }
        assertEquals(600, tank.getFluidAmount(), "aborted insert must roll back");
    }

    @Test
    void fluidExtractSimulatesBoundaries() {
        CustomFluidTank tank = new CustomFluidTank(1000);
        FluidResource water = FluidResource.of(Fluids.WATER);
        try (Transaction transaction = Transaction.open(null)) {
            tank.insert(0, water, 500, transaction);
            transaction.commit();
        }
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(0, tank.extract(0, FluidResource.of(Fluids.LAVA), 500, transaction));
            assertEquals(500, tank.extract(0, water, 2000, transaction));
        }
        assertEquals(500, tank.getFluidAmount());
    }

    @Test
    void energyContainerCommitsAndRollsBack() {
        long[] stored = {0};
        IEnergyContainer container = new IEnergyContainer() {

            @Override
            public long getEnergyStored() {
                return stored[0];
            }

            @Override
            public long getEnergyCapacity() {
                return 1000;
            }

            @Override
            public long changeEnergy(long energyDifference) {
                long changed;
                if (energyDifference >= 0) {
                    changed = Math.min(energyDifference, 1000 - stored[0]);
                } else {
                    changed = Math.max(energyDifference, -stored[0]);
                }
                stored[0] += changed;
                return changed;
            }

            @Override
            public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
                return 0;
            }

            @Override
            public boolean inputsEnergy(Direction side) {
                return false;
            }

            @Override
            public long getInputAmperage() {
                return 0;
            }

            @Override
            public long getInputVoltage() {
                return 0;
            }
        };
        EnergyHandler energy = GTMTransferAdapters.energy(container);
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(400, energy.insert(400, transaction));
            transaction.commit();
        }
        assertEquals(400, stored[0]);

        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(600, energy.insert(600, transaction));
            assertEquals(1000, energy.extract(500, transaction));
        }
        assertEquals(400, stored[0], "aborted insert/extract must roll back");
    }

    @Test
    void energyStorageAdapterBridgesToEnergyHandler() {
        int[] stored = {0};
        IEnergyStorage storage = new IEnergyStorage() {

            @Override
            public int receiveEnergy(int toReceive, boolean simulate) {
                if (simulate) return Math.min(toReceive, 1000 - stored[0]);
                int accepted = Math.min(toReceive, 1000 - stored[0]);
                stored[0] += accepted;
                return accepted;
            }

            @Override
            public int extractEnergy(int toExtract, boolean simulate) {
                if (simulate) return Math.min(toExtract, stored[0]);
                int extracted = Math.min(toExtract, stored[0]);
                stored[0] -= extracted;
                return extracted;
            }

            @Override
            public int getEnergyStored() {
                return stored[0];
            }

            @Override
            public int getMaxEnergyStored() {
                return 1000;
            }

            @Override
            public boolean canReceive() {
                return true;
            }

            @Override
            public boolean canExtract() {
                return true;
            }
        };
        EnergyHandler energy = GTMTransferAdapters.energy(storage);
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(300, energy.insert(300, transaction));
            transaction.commit();
        }
        assertEquals(300, storage.getEnergyStored());
        try (Transaction transaction = Transaction.open(null)) {
            assertEquals(300, energy.extract(300, transaction));
        }
        assertEquals(0, storage.getEnergyStored());
    }
}

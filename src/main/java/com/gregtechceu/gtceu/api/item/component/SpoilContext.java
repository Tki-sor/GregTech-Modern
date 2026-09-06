package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.GTMTransferAdapters;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the environment in which an item spoils, for example,
 * the level and the block's position, or the entity. It also may include a way to get an
 * {@link IItemHandler} (an instance of {@link ItemHandlerSource}) in which the item spoiled, and the slot number of the
 * item.
 * This info is used to, for example, spawn an entity when an item spoils.
 */
@With
public record SpoilContext(@Nullable Level level,
                           @Nullable BlockPos pos,
                           @Nullable Entity entity,
                           @Nullable ItemHandlerSource itemHandlerSource,
                           @Nullable CompoundTag itemHandlerData,
                           int slot) {

    // Hand-written with methods: Lombok's @With is not applied reliably on records under the
    // Java 25 toolchain, and the sync/spoil paths depend on these always being present.

    public SpoilContext withLevel(@Nullable Level level) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    public SpoilContext withPos(@Nullable BlockPos pos) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    public SpoilContext withEntity(@Nullable Entity entity) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    public SpoilContext withItemHandlerSource(@Nullable ItemHandlerSource itemHandlerSource) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    public SpoilContext withItemHandlerData(@Nullable CompoundTag itemHandlerData) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    public SpoilContext withSlot(int slot) {
        return new SpoilContext(level, pos, entity, itemHandlerSource, itemHandlerData, slot);
    }

    /**
     * @return the {@link Level} used to determine time to calculate spoilage progress (using
     *         {@link Level#getGameTime()}).
     *         This is usually the Overworld. If it is {@code null}, all spoilage updates are ignored.
     */
    public static @Nullable Level getDefaultLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.overworld();
    }

    public SpoilContext() {
        this((Level) null);
    }

    public SpoilContext(@Nullable Level level) {
        this(level, null);
    }

    public SpoilContext(@Nullable Level level, @Nullable BlockPos pos) {
        this(level, pos, null, null, null, -1);
    }

    public SpoilContext(@NotNull Entity entity) {
        this(entity.level(), entity.blockPosition(), entity, null, null, -1);
    }

    public SpoilContext(@NotNull Player player, int slot) {
        this(player.level(), player.blockPosition(), player, ItemHandlerSource.PLAYER_INVENTORY, null, slot);
    }

    public SpoilContext(@NotNull MetaMachine machine) {
        this(machine.getLevel(), machine.getBlockPos(), null, null, null, -1);
    }

    public boolean isEmpty() {
        return level == null;
    }

    public @Nullable IItemHandler itemHandler() {
        if (itemHandlerSource == null) return null;
        return itemHandlerSource.getHandler(this);
    }

    public SpoilContext withItemHandlerData(String key, Tag value) {
        CompoundTag tag = itemHandlerData == null ? new CompoundTag() : itemHandlerData.copy();
        tag.put(key, value);
        return this.withItemHandlerData(tag);
    }

    public SpoilContext withItemHandlerSide(Direction side) {
        if (side == null) return this.withItemHandlerSource(ItemHandlerSource.BLOCK_CAPABILITY);
        return this.withItemHandlerSource(ItemHandlerSource.BLOCK_CAPABILITY)
                .withItemHandlerData("side", StringTag.valueOf(side.getSerializedName()));
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (level != null) tag.putString("level", level.dimension().identifier().toString());
        if (pos != null) tag.putLong("pos", pos.asLong());
        if (entity != null) tag.putInt("entity", entity.getId());
        if (slot != -1) tag.putInt("slot", slot);
        if (itemHandlerSource != null) tag.putString("handlerSource", itemHandlerSource.getId().toString());
        if (itemHandlerData != null) tag.put("handlerData", itemHandlerData);
        return tag;
    }

    public static SpoilContext deserializeNBT(CompoundTag tag) {
        SpoilContext ctx = new SpoilContext();
        if (tag.contains("level")) {
            ctx = ctx.withLevel(ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(
                    Registries.DIMENSION,
                    Identifier.parse(tag.getString("level").orElseThrow()))));
        }
        if (tag.contains("pos")) {
            ctx = ctx.withPos(BlockPos.of(tag.getLong("pos").orElseThrow()));
        }
        if (tag.contains("entity") && ctx.level != null) {
            ctx = ctx.withEntity(ctx.level.getEntity(tag.getInt("entity").orElseThrow()));
        }
        if (tag.contains("slot")) {
            ctx = ctx.withSlot(tag.getInt("slot").orElseThrow());
        }
        if (tag.contains("handlerSource")) {
            ctx = ctx.withItemHandlerSource(
                    ItemHandlerSource.getById(Identifier.parse(tag.getString("handlerSource").orElseThrow())));
        }
        if (tag.contains("handlerData")) {
            ctx = ctx.withItemHandlerData(tag.getCompound("handlerData").orElseGet(CompoundTag::new));
        }
        return ctx;
    }

    /**
     * This class represents a way to get an {@link IItemHandler} from a {@link SpoilContext}, optionally
     * using {@link SpoilContext#itemHandlerData}. This is used instead of a normal supplier, due to the fact that
     * it is serializable. Note that new instances of this class should not be created dynamically.
     * <br>
     * This class is basically equivalent to a serializable {@code Function<SpoilContext, IItemHandler>}.
     */
    public static abstract class ItemHandlerSource {

        private static final Map<Identifier, ItemHandlerSource> HANDLER_SOURCES = new HashMap<>();

        /**
         * Represents getting an item handler as a capability of a block, with an optional "side" key in
         * {@link SpoilContext#itemHandlerData}
         */
        public static final ItemHandlerSource BLOCK_CAPABILITY = new ItemHandlerSource(GTCEu.id("block_cap")) {

            @Override
            protected @Nullable IItemHandler getHandler(SpoilContext ctx) {
                if (ctx.level() == null || ctx.pos() == null || ctx.itemHandlerData() == null) return null;
                CompoundTag tag = ctx.itemHandlerData();
                Direction side = Direction.byName(tag.getString("side").orElse(""));
                var handler = ctx.level().getCapability(Capabilities.Item.BLOCK, ctx.pos(), side);
                return handler == null ? null : GTMTransferAdapters.itemHandler(handler);
            }
        };

        /**
         * Represents getting an item handler as a player's inventory (used if {@link SpoilContext#entity} is a
         * {@link Player})
         */
        public static final ItemHandlerSource PLAYER_INVENTORY = new ItemHandlerSource(GTCEu.id("player_inventory")) {

            @Override
            protected @Nullable IItemHandler getHandler(SpoilContext ctx) {
                if (ctx.entity instanceof Player player) {
                    return new CustomItemStackHandler(player.getInventory().getNonEquipmentItems());
                } else return null;
            }
        };

        private static ItemHandlerSource getById(Identifier id) {
            return HANDLER_SOURCES.get(id);
        }

        private final Identifier id;

        public ItemHandlerSource(Identifier id) {
            this.id = id;
            HANDLER_SOURCES.put(id, this);
        }

        private Identifier getId() {
            return id;
        }

        @Override
        public String toString() {
            return id.toString();
        }

        abstract protected @Nullable IItemHandler getHandler(SpoilContext ctx);
    }
}

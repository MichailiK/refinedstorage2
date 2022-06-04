package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FluidStorageBlock extends StorageBlock {
    private final FluidStorageType.Variant variant;

    public FluidStorageBlock(FluidStorageType.Variant variant) {
        super(BlockConstants.STONE_PROPERTIES);
        this.variant = variant;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidStorageBlockBlockEntity(pos, state, variant);
    }
}
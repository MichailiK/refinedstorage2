package com.refinedmods.refinedstorage2.platform.forge.datagen.loot;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class BlockDropProvider extends BlockLootSubProvider {
    public BlockDropProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        Blocks.INSTANCE.getCable().forEach((color, block) -> this.dropSelf(block.get()));
        Blocks.INSTANCE.getGrid().forEach((color, block) -> this.dropSelf(block.get()));
        Blocks.INSTANCE.getController().forEach((color, block) -> this.dropSelf(block.get()));
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> this.dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        final Stream<Block> cables = Blocks.INSTANCE.getCable().values().stream().map(b -> b);
        final Stream<Block> grids = Blocks.INSTANCE.getGrid().values().stream().map(b -> b);
        final Stream<Block> controllers = Blocks.INSTANCE.getController().values().stream().map(b -> b);
        final Stream<Block> creativeControllers = Blocks.INSTANCE.getCreativeController().values().stream().map(b -> b);
        return Stream.concat(Stream.concat(cables, grids), Stream.concat(controllers, creativeControllers)).toList();
    }
}

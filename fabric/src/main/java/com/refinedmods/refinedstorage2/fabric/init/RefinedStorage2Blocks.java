package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import com.refinedmods.refinedstorage2.fabric.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.fabric.block.QuartzEnrichedIronBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2Blocks {
    private static final FabricBlockSettings STONE_SETTINGS = FabricBlockSettings
        .of(Material.STONE)
        .hardness(1.9F)
        .resistance(1.9F)
        .sounds(BlockSoundGroup.STONE);

    private CableBlock cable;
    private QuartzEnrichedIronBlock quartzEnrichedIron;
    private DiskDriveBlock diskDrive;

    public void register() {
        cable = Registry.register(Registry.BLOCK, new Identifier(RefinedStorage2Mod.ID, "cable"), new CableBlock());
        quartzEnrichedIron = Registry.register(Registry.BLOCK, new Identifier(RefinedStorage2Mod.ID, "quartz_enriched_iron_block"), new QuartzEnrichedIronBlock(STONE_SETTINGS));
        diskDrive = Registry.register(Registry.BLOCK, new Identifier(RefinedStorage2Mod.ID, "disk_drive"), new DiskDriveBlock(STONE_SETTINGS));
    }

    public CableBlock getCable() {
        return cable;
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIron() {
        return quartzEnrichedIron;
    }

    public DiskDriveBlock getDiskDrive() {
        return diskDrive;
    }
}
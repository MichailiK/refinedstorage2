package com.refinedmods.refinedstorage2.platform.forge.datagen;

import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ConstructorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DestructorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExternalStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.WirelessTransmitterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.grid.CraftingGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.grid.GridBlock;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ColorMap;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
    public ItemModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerCables();
        registerExporters();
        registerImporters();
        registerExternalStorages();
        registerControllers();
        registerCreativeControllers();
        registerGrids();
        registerCraftingGrids();
        registerDetectors();
        registerConstructors();
        registerDestructors();
        registerWirelessTransmitters();
    }

    private void registerCables() {
        final ResourceLocation base = createIdentifier("item/cable/base");
        final ColorMap<CableBlock> blocks = Blocks.INSTANCE.getCable();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName()))
        );
    }

    private void registerExporters() {
        final ResourceLocation base = createIdentifier("item/exporter/base");
        final ColorMap<ExporterBlock> blocks = Blocks.INSTANCE.getExporter();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerImporters() {
        final ResourceLocation base = createIdentifier("item/importer/base");
        final ColorMap<ImporterBlock> blocks = Blocks.INSTANCE.getImporter();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerExternalStorages() {
        final ResourceLocation base = createIdentifier("item/external_storage/base");
        final ColorMap<ExternalStorageBlock> blocks = Blocks.INSTANCE.getExternalStorage();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerControllers() {
        final ResourceLocation base = new ResourceLocation("item/generated");
        final ResourceLocation off = createIdentifier("block/controller/off");
        final ResourceLocation nearlyOff = createIdentifier("block/controller/nearly_off");
        final ResourceLocation nearlyOn = createIdentifier("block/controller/nearly_on");
        final ResourceLocation stored = createIdentifier("stored_in_controller");
        final ColorMap<ControllerBlock> blocks = Blocks.INSTANCE.getController();
        blocks.forEach((color, id, block) ->
            withExistingParent(id.getPath(), base)
                .override()
                .predicate(stored, 0)
                .model(modelFile(off))
                .end()
                .override()
                .predicate(stored, 0.01f)
                .model(modelFile(nearlyOff))
                .end()
                .override()
                .predicate(stored, 0.3f)
                .model(modelFile(nearlyOn))
                .end()
                .override()
                .predicate(stored, 0.4f)
                .model(modelFile(createIdentifier("block/controller/" + color.getName())))
                .end()
        );
    }

    private void registerCreativeControllers() {
        final BlockColorMap<ControllerBlock> blocks = Blocks.INSTANCE.getCreativeController();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/controller/" + color.getName())
        ));
    }

    private void registerGrids() {
        final BlockColorMap<GridBlock> blocks = Blocks.INSTANCE.getGrid();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/grid/" + color.getName())
        ));
    }

    private void registerCraftingGrids() {
        final BlockColorMap<CraftingGridBlock> blocks = Blocks.INSTANCE.getCraftingGrid();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/crafting_grid/" + color.getName())
        ));
    }

    private void registerDetectors() {
        final BlockColorMap<DetectorBlock> blocks = Blocks.INSTANCE.getDetector();
        blocks.forEach((color, id, block) -> withExistingParent(
            id.getPath(),
            createIdentifier("block/detector/" + color.getName())
        ));
    }

    private void registerConstructors() {
        final ResourceLocation base = createIdentifier("item/constructor/base");
        final ColorMap<ConstructorBlock> blocks = Blocks.INSTANCE.getConstructor();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerDestructors() {
        final ResourceLocation base = createIdentifier("item/destructor/base");
        final ColorMap<DestructorBlock> blocks = Blocks.INSTANCE.getDestructor();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cable",
            createIdentifier("block/cable/" + color.getName())
        ));
    }

    private void registerWirelessTransmitters() {
        final ResourceLocation base = createIdentifier("block/wireless_transmitter/inactive");
        final ColorMap<WirelessTransmitterBlock> blocks = Blocks.INSTANCE.getWirelessTransmitter();
        blocks.forEach((color, id, block) -> singleTexture(
            id.getPath(),
            base,
            "cutout",
            createIdentifier("block/wireless_transmitter/cutouts/" + color.getName())
        ));
    }

    private ModelFile modelFile(final ResourceLocation location) {
        return new ModelFile.ExistingModelFile(location, existingFileHelper);
    }
}

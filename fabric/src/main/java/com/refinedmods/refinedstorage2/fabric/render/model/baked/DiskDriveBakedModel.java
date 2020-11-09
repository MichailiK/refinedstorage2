package com.refinedmods.refinedstorage2.fabric.render.model.baked;

import com.refinedmods.refinedstorage2.fabric.block.DiskDriveBlock;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Random;
import java.util.function.Supplier;

public class DiskDriveBakedModel extends ForwardingBakedModel {
    private final BakedModel diskModel;

    public DiskDriveBakedModel(BakedModel baseModel, BakedModel diskModel) {
        this.wrapped = baseModel;
        this.diskModel = diskModel;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        QuadRotator quadRotator = new QuadRotator(state.get(DiskDriveBlock.DIRECTION));

        context.pushTransform(quadRotator);

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        QuadTranslator[] quadTranslators = new QuadTranslator[8];

        int i = 0;
        for (int x = 0; x < 2; ++x) {
            for (int y = 0; y < 4; ++y) {
                quadTranslators[i++] = new QuadTranslator(x == 0 ? -(2F / 16F) : -(9F / 16F), -((y * 3F) / 16F) - (2F / 16F), 0);
            }
        }

        for (int j = 0; j < 8; ++j) {
            context.pushTransform(quadTranslators[j]);
            context.fallbackConsumer().accept(diskModel);
            context.popTransform();
        }

        context.popTransform();
    }
}

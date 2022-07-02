package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.AbstractStorageBlockContainerMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractStorageBlockScreen extends AbstractStorageScreen<AbstractStorageBlockContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/storage.png");

    protected AbstractStorageBlockScreen(final AbstractStorageBlockContainerMenu menu,
                                         final Inventory inventory,
                                         final Component title) {
        super(menu, inventory, title, 80);
    }

    @Override
    protected boolean isResourceFilterButtonActive() {
        return false;
    }

    @Override
    protected void renderBg(final PoseStack poseStack, final float delta, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        super.renderBg(poseStack, delta, mouseX, mouseY);
    }
}
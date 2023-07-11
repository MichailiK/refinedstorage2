package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGridResource extends AbstractGridResource<ItemResource> {
    private final int id;
    private final ItemStack itemStack;

    public ItemGridResource(final ResourceAmount<ItemResource> resourceAmount,
                            final ItemStack itemStack,
                            final String name,
                            final String modId,
                            final String modName,
                            final Set<String> tags,
                            final String tooltip) {
        super(resourceAmount, name, Map.of(
            GridResourceAttributeKeys.MOD_ID, Set.of(modId),
            GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            GridResourceAttributeKeys.TAGS, tags,
            GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Item.getId(resourceAmount.getResource().item());
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack copyItemStack() {
        return itemStack.copyWithCount(1);
    }

    @Override
    public int getRegistryId() {
        return id;
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(
            StorageChannelTypes.ITEM,
            resourceAmount.getResource(),
            extractMode,
            cursor
        );
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        scrollingStrategy.onScroll(
            StorageChannelTypes.ITEM,
            resourceAmount.getResource(),
            scrollMode,
            -1
        );
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        final Font font = Minecraft.getInstance().font;
        graphics.renderItem(itemStack, x, y);
        graphics.renderItemDecorations(font, itemStack, x, y, null);
    }

    @Override
    public String getDisplayedAmount() {
        return AmountFormatting.formatWithUnits(getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return AmountFormatting.format(getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        final Minecraft minecraft = Minecraft.getInstance();
        return Screen.getTooltipFromItem(minecraft, itemStack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage() {
        return itemStack.getTooltipImage();
    }
}

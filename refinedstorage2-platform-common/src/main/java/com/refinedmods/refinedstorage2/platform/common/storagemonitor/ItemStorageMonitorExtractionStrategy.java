package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemStorageMonitorExtractionStrategy implements StorageMonitorExtractionStrategy {
    @Override
    @SuppressWarnings("deprecation")
    public boolean extract(final ResourceKey resource,
                           final boolean fullStack,
                           final Player player,
                           final Actor actor,
                           final Network network) {
        if (!(resource instanceof ItemResource itemResource)) {
            return false;
        }
        final long extracted = network.getComponent(StorageNetworkComponent.class).extract(
            itemResource,
            fullStack ? itemResource.item().getMaxStackSize() : 1,
            Action.EXECUTE,
            actor
        );
        if (extracted > 0) {
            final ItemStack stack = itemResource.toItemStack(extracted);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            return true;
        }
        return false;
    }
}

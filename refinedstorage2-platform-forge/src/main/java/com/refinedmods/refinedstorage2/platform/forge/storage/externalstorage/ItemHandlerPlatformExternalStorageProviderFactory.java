package com.refinedmods.refinedstorage2.platform.forge.storage.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.external.ExternalStorageProvider;
import com.refinedmods.refinedstorage2.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.forge.storage.CapabilityCacheImpl;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class ItemHandlerPlatformExternalStorageProviderFactory implements PlatformExternalStorageProviderFactory {
    @Override
    public Optional<ExternalStorageProvider> create(final ServerLevel level,
                                                    final BlockPos pos,
                                                    final Direction direction,
                                                    final StorageChannelType storageChannelType) {
        if (storageChannelType != StorageChannelTypes.ITEM) {
            return Optional.empty();
        }
        return Optional.of(new ItemHandlerExternalStorageProvider(new CapabilityCacheImpl(level, pos, direction)));
    }
}

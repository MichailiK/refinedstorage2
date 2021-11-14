package com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorageRepository;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageType;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class FluidStorageType implements StorageType<FluidResource> {
    public static final FluidStorageType INSTANCE = new FluidStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";

    private FluidStorageType() {
    }

    @Override
    public Storage<FluidResource> fromTag(NbtCompound tag, PlatformStorageRepository storageManager) {
        Storage<FluidResource> storage = new PlatformCappedStorage<>(
                tag.getLong(TAG_CAPACITY),
                FluidStorageType.INSTANCE,
                storageManager::markAsChanged
        );

        NbtList stacks = tag.getList(TAG_STACKS, NbtType.COMPOUND);
        for (NbtElement stackTag : stacks) {
            FluidResource.fromTagWithAmount((NbtCompound) stackTag).ifPresent(resourceAmount -> storage.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.EXECUTE));
        }
        return storage;
    }

    @Override
    public NbtCompound toTag(Storage<FluidResource> storage) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_CAPACITY, ((CappedStorage) storage).getCapacity());
        NbtList stacks = new NbtList();
        for (ResourceAmount<FluidResource> resourceAmount : storage.getAll()) {
            stacks.add(FluidResource.toTagWithAmount(resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }
}
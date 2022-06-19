package com.refinedmods.refinedstorage2.platform.apiimpl.storage;

import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorageRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientStorageRepository implements PlatformStorageRepository {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<UUID, StorageInfo> info = new HashMap<>();
    private final RateLimiter rateLimiter = RateLimiter.create(2);
    private final Consumer<UUID> storageInfoRequestAcceptor;

    public ClientStorageRepository(Consumer<UUID> storageInfoRequestAcceptor) {
        Preconditions.checkNotNull(storageInfoRequestAcceptor);
        this.storageInfoRequestAcceptor = storageInfoRequestAcceptor;
    }

    @Override
    public <T> Optional<Storage<T>> get(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void set(UUID id, Storage<T> storage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<Storage<T>> disassemble(UUID id) {
        throw new UnsupportedOperationException();
    }

    public void setInfo(UUID id, long stored, long capacity) {
        info.put(id, new StorageInfo(stored, capacity));
    }

    @Override
    public StorageInfo getInfo(UUID id) {
        trySendRequestPacket(id);
        return info.getOrDefault(id, StorageInfo.UNKNOWN);
    }

    private void trySendRequestPacket(UUID id) {
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Sending request info packet for {}", id);
        storageInfoRequestAcceptor.accept(id);
    }

    @Override
    public void markAsChanged() {
        throw new UnsupportedOperationException();
    }
}
package com.refinedmods.refinedstorage2.api.grid.service;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.Source;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Optional;
import java.util.function.Function;

public class GridServiceImpl<T> implements GridService<T> {
    private final StorageChannel<T> storageChannel;
    private final Source source;
    private final Function<T, Long> maxCountProvider;

    public GridServiceImpl(StorageChannel<T> storageChannel, Source source, Function<T, Long> maxCountProvider) {
        this.storageChannel = storageChannel;
        this.source = source;
        this.maxCountProvider = maxCountProvider;
    }

    @Override
    public Optional<ResourceAmount<T>> insert(ResourceAmount<T> resourceAmount, GridInsertMode insertMode) {
        return switch (insertMode) {
            case ENTIRE_RESOURCE -> insertEntireResource(resourceAmount);
            case SINGLE_RESOURCE -> insertSingleResource(resourceAmount);
        };
    }

    @Override
    public void extract(T resource, GridExtractMode extractMode, InsertableStorage<T> destination) {
        switch (extractMode) {
            case ENTIRE_RESOURCE -> extractEntireResource(resource, destination);
            case HALF_RESOURCE -> {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void extractEntireResource(T resource, InsertableStorage<T> destination) {
        long extractedFromSource = storageChannel.extract(resource, maxCountProvider.apply(resource), Action.SIMULATE);
        if (extractedFromSource > 0) {
            long remainderFromDestination = destination.insert(resource, extractedFromSource, Action.SIMULATE);
            boolean insertedSomething = remainderFromDestination != extractedFromSource;
            if (insertedSomething) {
                long amountInsertedIntoDestination = extractedFromSource - remainderFromDestination;
                extractedFromSource = storageChannel.extract(resource, amountInsertedIntoDestination, source);
                destination.insert(resource, extractedFromSource, Action.EXECUTE);
            }
        }
    }

    private Optional<ResourceAmount<T>> insertSingleResource(ResourceAmount<T> resourceAmount) {
        long remainder = storageChannel.insert(resourceAmount.getResource(), 1, Action.SIMULATE);
        boolean insertedSomething = remainder == 0;
        if (insertedSomething) {
            storageChannel.insert(resourceAmount.getResource(), 1, source);
            if (resourceAmount.getAmount() - 1 == 0) {
                return Optional.empty();
            }
            return Optional.of(new ResourceAmount<>(resourceAmount.getResource(), resourceAmount.getAmount() - 1));
        } else {
            return Optional.of(resourceAmount);
        }
    }

    private Optional<ResourceAmount<T>> insertEntireResource(ResourceAmount<T> resourceAmount) {
        long remainder = storageChannel.insert(resourceAmount.getResource(), resourceAmount.getAmount(), Action.SIMULATE);
        boolean insertedSomething = remainder != resourceAmount.getAmount();
        if (insertedSomething) {
            remainder = storageChannel.insert(resourceAmount.getResource(), resourceAmount.getAmount(), source);
            if (remainder > 0) {
                return Optional.of(new ResourceAmount<>(resourceAmount.getResource(), remainder));
            }
            return Optional.empty();
        } else {
            return Optional.of(resourceAmount);
        }
    }
}
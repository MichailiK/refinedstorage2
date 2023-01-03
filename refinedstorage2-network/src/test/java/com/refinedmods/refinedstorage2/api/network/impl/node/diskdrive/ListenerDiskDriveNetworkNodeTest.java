package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@NetworkTest
@SetupNetwork
class ListenerDiskDriveNetworkNodeTest {
    @AddNetworkNode
    DiskDriveNetworkNode sut;

    DiskDriveListener listener;
    StorageDiskProviderImpl provider;

    @BeforeEach
    void setUp() {
        listener = mock(DiskDriveListener.class);
        sut.setListener(listener);
        provider = new StorageDiskProviderImpl();
        sut.setDiskProvider(provider);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallDiskStateChangeListenerWhenExtracting(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.extract("A", 1, action, EmptyActor.INSTANCE);
        networkStorage.extract("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onDiskChanged();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldCallDiskStateChangeListenerWhenInserting(
        final Action action,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 74, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.insert("A", 1, action, EmptyActor.INSTANCE);
        networkStorage.insert("A", 1, action, EmptyActor.INSTANCE);

        // Assert
        final VerificationMode expectedTimes = action == Action.EXECUTE ? times(1) : never();
        verify(listener, expectedTimes).onDiskChanged();
    }

    @Test
    void shouldNotCallDiskStateChangeListenerWhenUnnecessaryOnExtracting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 76, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.extract("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onDiskChanged();
    }

    @Test
    void shouldNotCallDiskStateChangeListenerWhenUnnecessaryOnInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        networkStorage.insert("A", 74, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        verify(listener, never()).onDiskChanged();
    }

    private void initializeDiskDriveAndActivate() {
        sut.setDiskProvider(provider);
        sut.setActive(true);
    }
}
package com.refinedmods.refinedstorage2.api.network.impl.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.EmptyActor;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.network.test.AddNetworkNode;
import com.refinedmods.refinedstorage2.network.test.InjectNetwork;
import com.refinedmods.refinedstorage2.network.test.InjectNetworkStorageChannel;
import com.refinedmods.refinedstorage2.network.test.NetworkTest;
import com.refinedmods.refinedstorage2.network.test.SetupNetwork;
import com.refinedmods.refinedstorage2.network.test.util.FakeActor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.refinedmods.refinedstorage2.network.test.nodefactory.AbstractNetworkNodeFactory.PROPERTY_ENERGY_USAGE;
import static com.refinedmods.refinedstorage2.network.test.nodefactory.DiskDriveNetworkNodeFactory.PROPERTY_ENERGY_USAGE_PER_DISK;
import static org.assertj.core.api.Assertions.assertThat;

@NetworkTest
@SetupNetwork
class DiskDriveNetworkNodeTest {
    private static final long BASE_USAGE = 10;
    private static final long USAGE_PER_DISK = 3;

    @AddNetworkNode(properties = {
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE, longValue = BASE_USAGE),
        @AddNetworkNode.Property(key = PROPERTY_ENERGY_USAGE_PER_DISK, longValue = USAGE_PER_DISK)
    })
    DiskDriveNetworkNode sut;

    StorageDiskProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new StorageDiskProviderImpl();
    }

    @Test
    void shouldInitializeWithDiskProvider(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);

        // Act
        sut.setDiskProvider(provider);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void testInitialState(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Act
        final DiskDriveState states = sut.createState();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(sut.getFilterMode()).isEqualTo(FilterMode.BLOCK);
        assertThat(networkStorage.getAll()).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
        assertThat(states.getStates())
            .hasSize(9)
            .allMatch(state -> state == StorageDiskState.NONE);
        assertThat(sut.getAmountOfDiskSlots()).isEqualTo(9);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testDiskState(final boolean active) {
        // Arrange
        final Storage<String> normalStorage = new LimitedStorageImpl<>(100);
        normalStorage.insert("A", 74, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> nearCapacityStorage = new LimitedStorageImpl<>(100);
        nearCapacityStorage.insert("A", 75, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> fullStorage = new LimitedStorageImpl<>(100);
        fullStorage.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        final Storage<String> unlimitedStorage = new InMemoryStorageImpl<>();

        provider.setInSlot(2, unlimitedStorage);
        provider.setInSlot(3, normalStorage);
        provider.setInSlot(5, nearCapacityStorage);
        provider.setInSlot(7, fullStorage);

        // Act
        sut.setDiskProvider(provider);
        sut.setActive(active);

        final DiskDriveState state = sut.createState();

        // Assert
        assertThat(state.getState(0)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(1)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(2)).isEqualTo(active ? StorageDiskState.NORMAL : StorageDiskState.INACTIVE);
        assertThat(state.getState(3)).isEqualTo(active ? StorageDiskState.NORMAL : StorageDiskState.INACTIVE);
        assertThat(state.getState(4)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(5)).isEqualTo(
            active ? StorageDiskState.NEAR_CAPACITY : StorageDiskState.INACTIVE);
        assertThat(state.getState(6)).isEqualTo(StorageDiskState.NONE);
        assertThat(state.getState(7)).isEqualTo(active ? StorageDiskState.FULL : StorageDiskState.INACTIVE);
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + (USAGE_PER_DISK * 4));
    }

    @Test
    void shouldSetDiskInSlot(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        initializeDiskDriveAndActivate();

        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(7, storage);

        // Act
        sut.onDiskChanged(7);

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 5)
        );
    }

    @Test
    void shouldChangeDiskInSlot(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> originalStorage = new LimitedStorageImpl<>(10);
        originalStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(7, originalStorage);
        initializeDiskDriveAndActivate();

        final Storage<String> replacedStorage = new LimitedStorageImpl<>(10);
        replacedStorage.insert("B", 2, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(7, replacedStorage);

        // Act
        final Collection<ResourceAmount<String>> preDiskChanging = new HashSet<>(networkStorage.getAll());
        sut.onDiskChanged(7);
        final Collection<ResourceAmount<String>> postDiskChanging = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE + USAGE_PER_DISK);
        assertThat(preDiskChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 5)
        );
        assertThat(postDiskChanging).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 2)
        );
        assertThat(networkStorage.getStored()).isEqualTo(2L);
    }

    @Test
    void shouldRemoveDiskInSlot(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(10);
        storage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(7, storage);
        initializeDiskDriveAndActivate();

        provider.removeInSlot(7);

        // Act
        final Collection<ResourceAmount<String>> preDiskRemoval = new HashSet<>(networkStorage.getAll());
        sut.onDiskChanged(7);
        final Collection<ResourceAmount<String>> postDiskRemoval = networkStorage.getAll();

        // Assert
        assertThat(sut.getEnergyUsage()).isEqualTo(BASE_USAGE);
        assertThat(preDiskRemoval).isNotEmpty();
        assertThat(postDiskRemoval).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldNotChangeDiskInInvalidSlot() {
        // Act
        sut.onDiskChanged(-1);
        sut.onDiskChanged(9);

        // Assert
        final DiskDriveState states = sut.createState();

        assertThat(states.getStates())
            .hasSize(9)
            .allMatch(state -> state == StorageDiskState.NONE);
    }

    @Test
    void shouldNotUpdateNetworkStorageWhenChangingDiskWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.setActive(false);
        sut.onDiskChanged(1);
        final Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
        assertThat(networkStorage.getStored()).isZero();
    }

    @Test
    void shouldHaveResourcesFromDiskPresentInNetwork(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);

        initializeDiskDriveAndActivate();

        // Act
        final Collection<ResourceAmount<String>> resources = networkStorage.getAll();
        final long stored = networkStorage.getStored();

        // Assert
        assertThat(resources).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
        assertThat(stored).isEqualTo(100);
    }

    @Test
    void shouldInsert(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage1);

        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        provider.setInSlot(2, storage2);

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        provider.setInSlot(3, storage3);

        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 150, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("A", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("B", 300, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(150);
        assertThat(inserted2).isEqualTo(10);
        assertThat(inserted3).isEqualTo(140);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 60),
            new ResourceAmount<>("B", 40)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 100)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 140),
            new ResourceAmount<>("A", 160)
        );
        assertThat(networkStorage.getStored()).isEqualTo(inserted1 + inserted2 + inserted3);
    }

    @Test
    void shouldExtract(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storage1.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage1.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage1);

        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storage2.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage2.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(2, storage2);

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storage3.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(3, storage3);

        initializeDiskDriveAndActivate();

        // Act
        final long extracted = networkStorage.extract("A", 85, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isEqualTo(85);

        assertThat(storage1.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("B", 50)
        );
        assertThat(storage2.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 50),
            new ResourceAmount<>("A", 15)
        );
        assertThat(storage3.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("C", 10)
        );

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("B", 100),
            new ResourceAmount<>("A", 15),
            new ResourceAmount<>("C", 10)
        );
        assertThat(networkStorage.getStored()).isEqualTo(125);
    }

    @Test
    void shouldRespectAllowlistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectAllowlistWithNormalizerWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of("A"));
        sut.setNormalizer(resource -> {
            if (resource instanceof String str) {
                return str.substring(0, 1);
            }
            return resource;
        });

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("A1", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("A2", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted4 = networkStorage.insert("B", 1, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted5 = networkStorage.insert("B1", 1, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(1);
        assertThat(inserted2).isEqualTo(1);
        assertThat(inserted3).isEqualTo(1);
        assertThat(inserted4).isZero();
        assertThat(inserted5).isZero();
    }

    @Test
    void shouldRespectEmptyAllowlistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.ALLOW);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isZero();
    }

    @Test
    void shouldRespectBlocklistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of("A", "B"));

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isZero();
        assertThat(inserted2).isZero();
        assertThat(inserted3).isEqualTo(10);
    }

    @Test
    void shouldRespectEmptyBlocklistWhenInserting(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setFilterMode(FilterMode.BLOCK);
        sut.setFilterTemplates(Set.of());

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted1 = networkStorage.insert("A", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted2 = networkStorage.insert("B", 12, Action.EXECUTE, EmptyActor.INSTANCE);
        final long inserted3 = networkStorage.insert("C", 10, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted1).isEqualTo(12);
        assertThat(inserted2).isEqualTo(12);
        assertThat(inserted3).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldRespectAccessModeWhenInserting(final AccessMode accessMode,
                                              @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, INSERT -> assertThat(inserted).isEqualTo(5);
            case EXTRACT -> assertThat(inserted).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldRespectAccessModeWhenExtracting(
        final AccessMode accessMode,
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        sut.setAccessMode(accessMode);

        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        switch (accessMode) {
            case INSERT_EXTRACT, EXTRACT -> assertThat(extracted).isEqualTo(5);
            case INSERT -> assertThat(extracted).isZero();
        }
    }

    @Test
    void shouldNotAllowInsertsWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        sut.setActive(false);

        // Act
        final long inserted = networkStorage.insert("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(inserted).isZero();
    }

    @Test
    void shouldNotAllowExtractsWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 20, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        sut.setActive(false);

        // Act
        final long extracted = networkStorage.extract("A", 5, Action.EXECUTE, EmptyActor.INSTANCE);

        // Assert
        assertThat(extracted).isZero();
    }

    @Test
    void shouldHideFromNetworkWhenInactive(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage
    ) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final Collection<ResourceAmount<String>> preInactiveness = new HashSet<>(networkStorage.getAll());
        sut.setActive(false);
        final Collection<ResourceAmount<String>> postInactiveness = networkStorage.getAll();

        // Assert
        assertThat(preInactiveness).isNotEmpty();
        assertThat(postInactiveness).isEmpty();
    }

    @Test
    void shouldShowOnNetworkWhenActive(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new LimitedStorageImpl<>(100);
        storage.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        storage.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage);
        sut.setDiskProvider(provider);

        // Act
        sut.setActive(true);

        // Assert
        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );
    }

    @Test
    void shouldNoLongerShowOnNetworkWhenRemoved(
        @InjectNetworkStorageChannel final StorageChannel<String> networkStorage,
        @InjectNetwork final Network network
    ) {
        // Arrange
        final Storage<String> storage1 = new LimitedStorageImpl<>(100);
        storage1.insert("A", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(1, storage1);
        initializeDiskDriveAndActivate();

        // Act & assert
        final Storage<String> storage2 = new LimitedStorageImpl<>(100);
        storage2.insert("B", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(2, storage2);
        sut.onDiskChanged(2);

        assertThat(networkStorage.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(
            new ResourceAmount<>("A", 50),
            new ResourceAmount<>("B", 50)
        );

        network.removeContainer(() -> sut);
        assertThat(networkStorage.getAll()).isEmpty();

        final Storage<String> storage3 = new LimitedStorageImpl<>(100);
        storage3.insert("C", 50, Action.EXECUTE, EmptyActor.INSTANCE);
        provider.setInSlot(3, storage3);
        sut.onDiskChanged(3);

        assertThat(networkStorage.getAll()).isEmpty();
    }

    @Test
    void shouldTrackChanges(@InjectNetworkStorageChannel final StorageChannel<String> networkStorage) {
        // Arrange
        final Storage<String> storage = new TrackedStorageImpl<>(new LimitedStorageImpl<>(100), () -> 0L);
        provider.setInSlot(1, storage);
        initializeDiskDriveAndActivate();

        // Act
        final long inserted = networkStorage.insert("A", 10, Action.EXECUTE, FakeActor.INSTANCE);

        // Assert
        assertThat(inserted).isEqualTo(10);
        assertThat(networkStorage.findTrackedResourceByActorType("A", FakeActor.class)).isNotEmpty();
    }

    private void initializeDiskDriveAndActivate() {
        sut.setDiskProvider(provider);
        sut.setActive(true);
    }
}
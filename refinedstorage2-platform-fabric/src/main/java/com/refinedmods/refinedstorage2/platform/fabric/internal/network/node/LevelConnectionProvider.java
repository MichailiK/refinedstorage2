package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node;

import com.refinedmods.refinedstorage2.api.network.node.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.node.Connections;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.blockentity.NetworkNodeContainerBlockEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class LevelConnectionProvider implements ConnectionProvider {
    private final Level level;

    public LevelConnectionProvider(Level level) {
        this.level = level;
    }

    @Override
    public Connections findConnections(NetworkNodeContainer pivot, Set<NetworkNodeContainer> existingConnections) {
        ScanState scanState = new ScanState(convertToScanEntries(existingConnections));

        // TODO: Convert to queue
        depthScan(scanState, ((NetworkNodeContainerBlockEntity) pivot).getBlockPos());

        return new Connections(
                convertToContainers(scanState.foundEntries),
                convertToContainers(scanState.newEntries),
                convertToContainers(scanState.removedEntries)
        );
    }

    private Set<NetworkNodeContainer> convertToContainers(Set<ScanEntry> foundEntries) {
        return foundEntries.stream().map(ScanEntry::getContainer).collect(Collectors.toSet());
    }

    private Set<ScanEntry> convertToScanEntries(Set<NetworkNodeContainer> existingConnections) {
        return existingConnections
                .stream()
                .map(container -> new ScanEntry(
                        container,
                        ((BlockEntity) container).getLevel(),
                        ((BlockEntity) container).getBlockPos()
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public List<NetworkNodeContainer> sort(Set<NetworkNodeContainer> containers) {
        return containers
                .stream()
                .sorted(Comparator.comparing(container -> ((BlockEntity) container).getBlockPos()))
                .toList();
    }

    private void depthScan(ScanState scanState, BlockPos position) {
        if (getBlockEntity(level, position) instanceof NetworkNodeContainerBlockEntity containerBlockEntity) {
            addEntry(scanState, new ScanEntry(containerBlockEntity, level, position));
        }
    }

    private void addEntry(ScanState scanState, ScanEntry entry) {
        if (scanState.foundEntries.contains(entry)) {
            return;
        }

        scanState.foundEntries.add(entry);

        if (!scanState.currentEntries.contains(entry)) {
            scanState.newEntries.add(entry);
        }

        scanState.removedEntries.remove(entry);

        Set<NetworkNodeContainer> connections = findConnectionsAt(level, ((NetworkNodeContainerBlockEntity) entry.getContainer()).getBlockPos());
        for (NetworkNodeContainer connection : connections) {
            depthScan(scanState, ((NetworkNodeContainerBlockEntity) connection).getBlockPos());
        }
    }

    private Set<NetworkNodeContainer> findConnectionsAt(Level level, BlockPos pos) {
        Set<NetworkNodeContainer> containers = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.relative(direction);
            if (getBlockEntity(level, offsetPos) instanceof NetworkNodeContainerBlockEntity containerBlockEntity) {
                containers.add(containerBlockEntity);
            }
        }
        return containers;
    }

    private BlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        // Avoid using EntityCreationType.IMMEDIATE.
        // By default, the block is removed first and then the block entity (see BaseBlock#onRemove).
        // But, when using mods like Carrier or Carpet that allow for moving block entities,
        // they remove the block entity first and then the block.
        // When removing a block with Carrier for example,
        // this causes a problematic situation that the block entity IS gone,
        // but that the #getBlockEntity() call here with type IMMEDIATE would recreate the block entity because
        // the block is still there.
        // If the block entity is returned here again even if it is removed, the preconditions in NetworkBuilder will fail
        // as the "removed" block entity/connection would still be present.
        return level.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }

    private static class ScanState {
        private final Set<ScanEntry> currentEntries;
        private final Set<ScanEntry> foundEntries = new HashSet<>();
        private final Set<ScanEntry> newEntries = new HashSet<>();
        private final Set<ScanEntry> removedEntries;

        public ScanState(Set<ScanEntry> currentEntries) {
            this.currentEntries = currentEntries;
            this.removedEntries = new HashSet<>(currentEntries);
        }
    }

    private static class ScanEntry {
        private final NetworkNodeContainer container;
        private final ResourceKey<Level> dimension;
        private final BlockPos position;

        public ScanEntry(NetworkNodeContainer container, Level dimension, BlockPos position) {
            this.container = container;
            this.dimension = dimension.dimension();
            this.position = position;
        }

        public NetworkNodeContainer getContainer() {
            return container;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScanEntry that = (ScanEntry) o;
            return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, position);
        }
    }
}
package com.refinedmods.refinedstorage2.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemTargetBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.wirelesstransmitter.WirelessTransmitter;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

class NetworkBoundItemSessionImpl implements NetworkBoundItemSession {
    private final Player player;
    private final Vec3 playerPosition;
    private final SlotReference slotReference;
    @Nullable
    private final NetworkReference networkReference;

    NetworkBoundItemSessionImpl(
        final Player player,
        final SlotReference slotReference,
        @Nullable final NetworkReference networkReference
    ) {
        this.player = player;
        // We copy the player position as it can change after opening the network bound item (opening while walking)
        // and could cause the network not being accessible anymore (due to being out of range of a transmitter).
        // If the network is no longer accessible, certain assumptions will break (e.g. grid watcher can no longer
        // be removed after it was added).
        this.playerPosition = new Vec3(player.position().x, player.position().y, player.position().z);
        this.slotReference = slotReference;
        this.networkReference = networkReference;
    }

    @Override
    public Optional<Network> resolveNetwork() {
        if (networkReference == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(player.getServer())
            .map(server -> server.getLevel(networkReference.dimensionKey()))
            .filter(level -> level.isLoaded(networkReference.pos()))
            .map(level -> level.getBlockEntity(networkReference.pos()))
            .filter(NetworkBoundItemTargetBlockEntity.class::isInstance)
            .map(NetworkBoundItemTargetBlockEntity.class::cast)
            .map(NetworkBoundItemTargetBlockEntity::getNetworkForBoundItem)
            .filter(this::isInRange);
    }

    private boolean isInRange(final Network network) {
        return network.getComponent(GraphNetworkComponent.class)
            .getContainers(WirelessTransmitter.class)
            .stream()
            .anyMatch(wirelessTransmitter -> wirelessTransmitter.isInRange(
                player.level().dimension(),
                playerPosition
            ));
    }

    @Override
    public boolean isActive() {
        return slotReference.resolve(player)
            .flatMap(Platform.INSTANCE::getEnergyStorage)
            .map(energyStorage -> energyStorage.getStored() > 0)
            .orElse(true);
    }

    @Override
    public void drainEnergy(final long amount) {
        slotReference.resolve(player).flatMap(Platform.INSTANCE::getEnergyStorage).ifPresent(
            energyStorage -> energyStorage.extract(amount, Action.EXECUTE)
        );
    }

    record NetworkReference(ResourceKey<Level> dimensionKey, BlockPos pos) {
    }
}

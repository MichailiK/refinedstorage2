package com.refinedmods.refinedstorage2.core.network;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface NetworkManager {
    Network onNodeAdded(BlockPos pos);

    Set<Network> onNodeRemoved(BlockPos pos);

    Optional<Network> getNetwork(UUID id);

    Collection<Network> getNetworks();
}

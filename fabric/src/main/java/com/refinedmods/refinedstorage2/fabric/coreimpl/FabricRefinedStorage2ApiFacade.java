package com.refinedmods.refinedstorage2.fabric.coreimpl;

import com.refinedmods.refinedstorage2.core.RefinedStorage2ApiFacade;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.NetworkManagerImpl;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.FabricNetworkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FabricRefinedStorage2ApiFacade implements RefinedStorage2ApiFacade {
    @Override
    public NetworkManager getNetworkManager(ServerWorld world) {
        return world
            .getServer()
            .getWorld(World.OVERWORLD)
            .getPersistentStateManager()
            .getOrCreate(() -> new FabricNetworkManager(FabricNetworkManager.NAME, new NetworkManagerImpl()), FabricNetworkManager.NAME);
    }
}

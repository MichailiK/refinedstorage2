package com.refinedmods.refinedstorage2.platform.api.support.network.bounditem;

import com.refinedmods.refinedstorage2.api.network.Network;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public interface NetworkBoundItemTargetBlockEntity {
    @Nullable
    Network getNetworkForBoundItem();
}
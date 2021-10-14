package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.FluidResource;

import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.util.registry.Registry;

public class FluidGridResource extends GridResource<FluidResource> {
    private final int id;
    private final FluidVariant fluidVariant;

    public FluidGridResource(ResourceAmount<FluidResource> resourceAmount, String name, String modId, String modName, Set<String> tags) {
        super(resourceAmount, name, modId, modName, tags);
        this.id = Registry.FLUID.getRawId(getResourceAmount().getResource().getFluid());
        this.fluidVariant = resourceAmount.getResource().toFluidVariant();
    }

    public FluidVariant getFluidVariant() {
        return fluidVariant;
    }

    @Override
    public int getId() {
        return id;
    }
}

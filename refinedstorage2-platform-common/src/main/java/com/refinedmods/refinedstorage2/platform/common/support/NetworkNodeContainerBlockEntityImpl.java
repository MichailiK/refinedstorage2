package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.support.AbstractNetworkNodeContainerBlockEntity;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkNodeContainerBlockEntityImpl<T extends AbstractNetworkNode>
    extends AbstractNetworkNodeContainerBlockEntity<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkNodeContainerBlockEntityImpl.class);

    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);
    private boolean lastActive;

    public NetworkNodeContainerBlockEntityImpl(final BlockEntityType<?> type,
                                               final BlockPos pos,
                                               final BlockState state,
                                               final T networkNode) {
        super(type, pos, state, networkNode);
    }

    protected boolean isActive() {
        final long energyUsage = getNode().getEnergyUsage();
        final boolean hasLevel = level != null && level.isLoaded(worldPosition);
        return hasLevel
            && getNode().getNetwork() != null
            && getNode().getNetwork().getComponent(EnergyNetworkComponent.class).getStored() >= energyUsage;
    }

    public void updateActiveness(final BlockState state,
                                 @Nullable final BooleanProperty activenessProperty) {
        final boolean newActive = isActive();
        if (newActive != lastActive && activenessChangeRateLimiter.tryAcquire()) {
            LOGGER.debug("Activeness change for node at {}: {} -> {}", getBlockPos(), lastActive, newActive);
            this.lastActive = newActive;
            activenessChanged(state, newActive, activenessProperty);
        }
    }

    protected void activenessChanged(final BlockState state,
                                     final boolean newActive,
                                     @Nullable final BooleanProperty activenessProperty) {
        getNode().setActive(newActive);

        final boolean needToUpdateBlockState = activenessProperty != null
            && state.getValue(activenessProperty) != newActive;

        if (needToUpdateBlockState) {
            LOGGER.debug("Sending block update for block at {} due to state change to {}", getBlockPos(), newActive);
            updateActivenessState(state, activenessProperty, newActive);
        }
    }

    private void updateActivenessState(final BlockState state,
                                       final BooleanProperty activenessProperty,
                                       final boolean active) {
        if (level != null) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(activenessProperty, active));
        }
    }

    public void doWork() {
        getNode().doWork();
    }

    @Override
    public boolean canPerformOutgoingConnection(final Direction direction) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return true;
        }
        return myDirection != direction;
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction direction, final BlockState other) {
        if (!colorsAllowConnecting(other)) {
            return false;
        }
        final Direction myDirection = getDirection();
        if (myDirection != null) {
            return myDirection != direction.getOpposite();
        }
        return true;
    }

    protected final boolean colorsAllowConnecting(final BlockState other) {
        if (!(other.getBlock() instanceof ColorableBlock<?> otherColorableBlock)) {
            return true;
        }
        final ColorableBlock<?> colorableBlock = getColor();
        if (colorableBlock == null) {
            return true;
        }
        return otherColorableBlock.getColor() == colorableBlock.getColor()
            || colorableBlock.canAlwaysConnect()
            || otherColorableBlock.canAlwaysConnect();
    }

    @Nullable
    private ColorableBlock<?> getColor() {
        if (!(getBlockState().getBlock() instanceof ColorableBlock<?> colorableBlock)) {
            return null;
        }
        return colorableBlock;
    }

    @Nullable
    protected final Direction getDirection() {
        final BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof AbstractDirectionalBlock<?> directionalBlock)) {
            return null;
        }
        return directionalBlock.extractDirection(blockState);
    }
}
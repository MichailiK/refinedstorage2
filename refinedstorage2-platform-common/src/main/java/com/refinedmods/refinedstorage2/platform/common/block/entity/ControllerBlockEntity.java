package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerEnergyType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.network.energy.PlatformEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockEntity extends InternalNetworkNodeContainerBlockEntity<ControllerNetworkNode> implements ExtendedMenuProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_STORED = "stored";
    private static final int ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final ControllerBlock.ControllerType type;
    private final EnergyStorage energyStorage;
    private long lastTypeChanged;

    public ControllerBlockEntity(ControllerBlock.ControllerType type, BlockPos pos, BlockState state) {
        super(getBlockEntityType(type), pos, state, new ControllerNetworkNode());
        this.type = type;
        this.energyStorage = createEnergyStorage(type);
        this.getNode().setEnergyStorage(energyStorage);
    }

    public static void serverTick(Level level, BlockState state, ControllerBlockEntity blockEntity) {
        InternalNetworkNodeContainerBlockEntity.serverTick(level, state, blockEntity);
        blockEntity.updateEnergyTypeInLevel(state);
    }

    private EnergyStorage createEnergyStorage(ControllerBlock.ControllerType type) {
        return switch (type) {
            case NORMAL -> createNormalEnergyStorage();
            case CREATIVE -> new InfiniteEnergyStorage();
        };
    }

    private EnergyStorage createNormalEnergyStorage() {
        return new PlatformEnergyStorage(
                new EnergyStorageImpl(PlatformAbstractions.INSTANCE.getConfig().getController().getCapacity()),
                this::setChanged
        );
    }

    private static BlockEntityType<ControllerBlockEntity> getBlockEntityType(ControllerBlock.ControllerType type) {
        return type == ControllerBlock.ControllerType.CREATIVE
                ? BlockEntities.INSTANCE.getCreativeController()
                : BlockEntities.INSTANCE.getController();
    }

    public void updateEnergyTypeInLevel(BlockState state) {
        ControllerEnergyType energyType = ControllerEnergyType.ofState(getNode().getState());
        ControllerEnergyType inLevelEnergyType = state.getValue(ControllerBlock.ENERGY_TYPE);

        if (energyType != inLevelEnergyType && (lastTypeChanged == 0 || System.currentTimeMillis() - lastTypeChanged > ENERGY_TYPE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Energy type state change for block at {}: {} -> {}", getBlockPos(), inLevelEnergyType, energyType);

            this.lastTypeChanged = System.currentTimeMillis();

            updateEnergyTypeInLevel(state, energyType);
        }
    }

    private void updateEnergyTypeInLevel(BlockState state, ControllerEnergyType type) {
        level.setBlockAndUpdate(getBlockPos(), state.setValue(ControllerBlock.ENERGY_TYPE, type));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_STORED, getNode().getActualStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_STORED) && energyStorage instanceof PlatformEnergyStorage platformEnergyStorage) {
            platformEnergyStorage.receiveSilently(tag.getLong(TAG_STORED));
        }
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", type == ControllerBlock.ControllerType.CREATIVE ? "creative_controller" : "controller");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new ControllerContainerMenu(syncId, inv, this, player);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeLong(getActualStored());
        buf.writeLong(getActualCapacity());
    }

    public long getActualStored() {
        return getNode().getActualStored();
    }

    public long getActualCapacity() {
        return getNode().getActualCapacity();
    }
}
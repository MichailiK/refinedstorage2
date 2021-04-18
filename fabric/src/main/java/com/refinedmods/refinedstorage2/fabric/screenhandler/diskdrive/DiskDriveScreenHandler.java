package com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.SlotFixedItemInv;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDiskInfo;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveInventory;
import com.refinedmods.refinedstorage2.fabric.screenhandler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screenhandler.FilterModeAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.PriorityAccessor;
import com.refinedmods.refinedstorage2.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.fabric.screenhandler.slot.FilterSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DiskDriveScreenHandler extends BaseScreenHandler implements PriorityAccessor, FilterModeAccessor {
    private static final int DISK_SLOT_X = 61;
    private static final int DISK_SLOT_Y = 54;

    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private final StorageDiskInfoAccessor storageDiskInfoAccessor;
    private final List<Slot> diskSlots = new ArrayList<>();
    private final TwoWaySyncProperty<Integer> priorityProperty;
    private final TwoWaySyncProperty<FilterMode> filterModeProperty;

    public DiskDriveScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        addProperty(priorityProperty = TwoWaySyncProperty.forClient(
                0,
                priority -> priority,
                priority -> priority,
                0,
                (priority) -> {
                }
        ));

        addProperty(filterModeProperty = TwoWaySyncProperty.forClient(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                FilterMode.BLOCK,
                (filterMode) -> {
                }
        ));

        this.storageDiskInfoAccessor = new StorageDiskInfoAccessorImpl(playerInventory.player.getEntityWorld());

        addSlots(playerInventory.player, new DiskDriveInventory(), new FullFixedItemInv(9));
    }

    public DiskDriveScreenHandler(int syncId, PlayerEntity player, FixedItemInv diskInventory, FixedItemInv filterInventory, DiskDriveBlockEntity diskDrive, StorageDiskInfoAccessor storageDiskInfoAccessor) {
        super(RefinedStorage2Mod.SCREEN_HANDLERS.getDiskDrive(), syncId);

        addProperty(priorityProperty = TwoWaySyncProperty.forServer(
                0,
                priority -> priority,
                priority -> priority,
                diskDrive::getPriority,
                diskDrive::setPriority
        ));

        addProperty(filterModeProperty = TwoWaySyncProperty.forServer(
                1,
                FilterModeSettings::getFilterMode,
                FilterModeSettings::getFilterMode,
                diskDrive::getFilterMode,
                diskDrive::setFilterMode
        ));

        this.storageDiskInfoAccessor = storageDiskInfoAccessor;

        addSlots(player, diskInventory, filterInventory);
    }

    private void addSlots(PlayerEntity player, FixedItemInv diskInventory, FixedItemInv filterInventory) {
        for (int i = 0; i < DiskDriveNetworkNode.DISK_COUNT; ++i) {
            diskSlots.add(addSlot(createDiskSlot(player, diskInventory, i)));
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(createFilterSlot(player, filterInventory, i));
        }
        addPlayerInventory(player.inventory, 8, 141);
    }

    private SlotFixedItemInv createFilterSlot(PlayerEntity player, FixedItemInv filterInventory, int i) {
        int x = FILTER_SLOT_X + (18 * i);
        return new FilterSlot(this, filterInventory, !player.world.isClient(), i, x, FILTER_SLOT_Y);
    }

    private SlotFixedItemInv createDiskSlot(PlayerEntity player, FixedItemInv diskInventory, int i) {
        int x = DISK_SLOT_X + ((i % 2) * 18);
        int y = DISK_SLOT_Y + Math.floorDiv(i, 2) * 18;
        return new SlotFixedItemInv(this, diskInventory, !player.world.isClient(), i, x, y);
    }

    public boolean hasInfiniteDisk() {
        return getStorageDiskInfo().anyMatch(info -> info.getCapacity() == -1);
    }

    public double getProgress() {
        if (hasInfiniteDisk()) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    public int getCapacity() {
        return getStorageDiskInfo().mapToInt(StorageDiskInfo::getCapacity).sum();
    }

    public int getStored() {
        return getStorageDiskInfo().mapToInt(StorageDiskInfo::getStored).sum();
    }

    private Stream<StorageDiskInfo> getStorageDiskInfo() {
        return diskSlots
                .stream()
                .map(Slot::getStack)
                .filter(stack -> !stack.isEmpty())
                .map(storageDiskInfoAccessor::getDiskInfo)
                .flatMap(info -> info.map(Stream::of).orElseGet(Stream::empty));
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            if (index < 8) {
                if (!insertItem(stackInSlot, 8, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stackInSlot, 0, 8, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return originalStack;
    }

    @Override
    public int getPriority() {
        return priorityProperty.getDeserialized();
    }

    @Override
    public void setPriority(int priority) {
        priorityProperty.syncToServer(priority);
    }

    @Override
    public FilterMode getFilterMode() {
        return filterModeProperty.getDeserialized();
    }

    @Override
    public void setFilterMode(FilterMode filterMode) {
        filterModeProperty.syncToServer(filterMode);
    }
}
package com.refinedmods.refinedstorage2.platform.fabric.packet.s2c;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterStatus;
import com.refinedmods.refinedstorage2.platform.common.support.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerToClientCommunicationsImpl implements ServerToClientCommunications {
    @Override
    public void sendEnergyInfo(final ServerPlayer player, final long stored, final long capacity) {
        sendToPlayer(player, PacketIds.ENERGY_INFO, buf -> {
            buf.writeLong(stored);
            buf.writeLong(capacity);
        });
    }

    @Override
    public void sendWirelessTransmitterRange(final ServerPlayer player, final int range) {
        sendToPlayer(player, PacketIds.WIRELESS_TRANSMITTER_RANGE, buf -> buf.writeInt(range));
    }

    @Override
    public void sendGridActiveness(final ServerPlayer player, final boolean active) {
        sendToPlayer(player, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
    }

    @Override
    public void sendGridUpdate(final ServerPlayer player,
                               final PlatformResourceKey resource,
                               final long change,
                               @Nullable final TrackedResource trackedResource) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(id -> sendToPlayer(
            player,
            PacketIds.GRID_UPDATE,
            buf -> {
                buf.writeResourceLocation(id);
                resource.toBuffer(buf);
                buf.writeLong(change);
                PacketUtil.writeTrackedResource(buf, trackedResource);
            }
        ));
    }

    @Override
    public void sendGridClear(final ServerPlayer player) {
        sendToPlayer(player, PacketIds.GRID_CLEAR, buf -> {
        });
    }

    @Override
    public void sendResourceSlotUpdate(final ServerPlayer player,
                                       @Nullable final ResourceAmount resourceAmount,
                                       final int slotIndex) {
        sendToPlayer(player, PacketIds.RESOURCE_SLOT_UPDATE, buf -> {
            buf.writeInt(slotIndex);
            if (resourceAmount != null
                && resourceAmount.getResource() instanceof PlatformResourceKey platformResource) {
                sendResourceSlotUpdate(platformResource, resourceAmount.getAmount(), buf);
            } else {
                buf.writeBoolean(false);
            }
        });
    }

    private void sendResourceSlotUpdate(final PlatformResourceKey resource,
                                        final long amount,
                                        final FriendlyByteBuf buf) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresentOrElse(id -> {
            buf.writeBoolean(true);
            buf.writeResourceLocation(id);
            resource.toBuffer(buf);
            buf.writeLong(amount);
        }, () -> buf.writeBoolean(false));
    }

    @Override
    public void sendStorageInfoResponse(final ServerPlayer player, final UUID id, final StorageInfo storageInfo) {
        sendToPlayer(player, PacketIds.STORAGE_INFO_RESPONSE, bufToSend -> {
            bufToSend.writeUUID(id);
            bufToSend.writeLong(storageInfo.stored());
            bufToSend.writeLong(storageInfo.capacity());
        });
    }

    @Override
    public void sendNetworkTransmitterStatus(final ServerPlayer player, final NetworkTransmitterStatus status) {
        sendToPlayer(player, PacketIds.NETWORK_TRANSMITTER_STATUS, buf -> {
            buf.writeBoolean(status.error());
            buf.writeComponent(status.message());
        });
    }

    @Override
    public void sendNoPermission(final ServerPlayer player, final Component message) {
        sendToPlayer(player, PacketIds.NO_PERMISSION, buf -> buf.writeComponent(message));
    }

    private static void sendToPlayer(final ServerPlayer playerEntity,
                                     final ResourceLocation id,
                                     final Consumer<FriendlyByteBuf> bufConsumer) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }
}

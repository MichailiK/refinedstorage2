package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.storage.StorageInfo;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;

import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class StorageInfoRequestPacket implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        UUID id = buf.readUUID();

        server.execute(() -> {
            StorageInfo info = Rs2PlatformApiFacade.INSTANCE
                    .getStorageRepository(player.getCommandSenderWorld())
                    .getInfo(id);

            PlatformAbstractions.INSTANCE.getServerToClientCommunications().sendStorageInfoResponse(player, id, info);
        });
    }
}

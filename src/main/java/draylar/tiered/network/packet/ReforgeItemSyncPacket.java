package draylar.tiered.network.packet;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ReforgeItemSyncPacket(List<Identifier> ids, List<Integer> listSize, List<Integer> itemIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReforgeItemSyncPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tiered", "reforge_item_sync_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgeItemSyncPacket> PACKET_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ReforgeItemSyncPacket::ids,
            ByteBufCodecs.INT.apply(ByteBufCodecs.list()), ReforgeItemSyncPacket::listSize, ByteBufCodecs.INT.apply(ByteBufCodecs.list()), ReforgeItemSyncPacket::itemIds, ReforgeItemSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

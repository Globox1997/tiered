package draylar.tiered.network.packet;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReforgeItemSyncPacket(List<Identifier> ids, List<Integer> listSize, List<Integer> itemIds) implements CustomPayload {

    public static final CustomPayload.Id<ReforgeItemSyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "reforge_item_sync_packet"));

    public static final PacketCodec<RegistryByteBuf, ReforgeItemSyncPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeCollection(value.ids, PacketByteBuf::writeIdentifier);
        buf.writeCollection(value.listSize, PacketByteBuf::writeInt);
        buf.writeCollection(value.itemIds, PacketByteBuf::writeInt);
    }, buf -> new ReforgeItemSyncPacket(buf.readList(PacketByteBuf::readIdentifier), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

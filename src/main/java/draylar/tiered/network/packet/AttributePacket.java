package draylar.tiered.network.packet;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AttributePacket(List<String> attributeIds, List<String> attributeJsons) implements CustomPayload {

    public static final CustomPayload.Id<AttributePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "attribute_packet"));

    public static final PacketCodec<RegistryByteBuf, AttributePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeCollection(value.attributeIds, PacketByteBuf::writeString);
        buf.writeCollection(value.attributeJsons, PacketByteBuf::writeString);
    }, buf -> new AttributePacket(buf.readList(PacketByteBuf::readString), buf.readList(PacketByteBuf::readString)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

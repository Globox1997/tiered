package draylar.tiered.network.packet;

import java.util.List;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AttributePacket(boolean reset, List<String> attributeIds, List<String> attributeJsons) implements CustomPayload {

    public static final int MAX_STRING_LENGTH = 262144;

    public static final CustomPayload.Id<AttributePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "attribute_packet"));

    public static final PacketCodec<RegistryByteBuf, AttributePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBoolean(value.reset);
        buf.writeCollection(value.attributeIds, (buffer, string) -> buffer.writeString(string, MAX_STRING_LENGTH));
        buf.writeCollection(value.attributeJsons, (buffer, string) -> buffer.writeString(string, MAX_STRING_LENGTH));
    }, buf -> new AttributePacket(buf.readBoolean(), buf.readList(buffer -> buffer.readString(MAX_STRING_LENGTH)), buf.readList(buffer -> buffer.readString(MAX_STRING_LENGTH))));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

package draylar.tiered.network.packet;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AttributePacket(boolean reset, List<String> attributeIds, List<String> attributeJsons) implements CustomPacketPayload {

    public static final int MAX_STRING_LENGTH = 262144;

    public static final CustomPacketPayload.Type<AttributePacket> PACKET_ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tiered", "attribute_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributePacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBoolean(value.reset);
        buf.writeCollection(value.attributeIds, (buffer, string) -> buffer.writeUtf(string, MAX_STRING_LENGTH));
        buf.writeCollection(value.attributeJsons, (buffer, string) -> buffer.writeUtf(string, MAX_STRING_LENGTH));
    }, buf -> new AttributePacket(buf.readBoolean(), buf.readList(buffer -> buffer.readUtf(MAX_STRING_LENGTH)), buf.readList(buffer -> buffer.readUtf(MAX_STRING_LENGTH))));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

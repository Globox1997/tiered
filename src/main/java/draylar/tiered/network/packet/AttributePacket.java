package draylar.tiered.network.packet;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AttributePacket(boolean reset, List<String> attributeIds, List<String> attributeJsons) implements CustomPacketPayload {

    public static final int MAX_STRING_LENGTH = 262144;

    public static final CustomPacketPayload.Type<AttributePacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tiered", "attribute_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributePacket> PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, AttributePacket::reset,
            ByteBufCodecs.stringUtf8(MAX_STRING_LENGTH).apply(ByteBufCodecs.list()), AttributePacket::attributeIds, ByteBufCodecs.stringUtf8(MAX_STRING_LENGTH).apply(ByteBufCodecs.list()),
            AttributePacket::attributeJsons, AttributePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

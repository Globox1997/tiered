package draylar.tiered.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HealthPacket(float health) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HealthPacket> PACKET_ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tiered", "health_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HealthPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeFloat(value.health);
    }, buf -> new HealthPacket(buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

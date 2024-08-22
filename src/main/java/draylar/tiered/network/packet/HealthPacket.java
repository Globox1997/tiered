package draylar.tiered.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HealthPacket(float health) implements CustomPayload {

    public static final CustomPayload.Id<HealthPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "health_packet"));

    public static final PacketCodec<RegistryByteBuf, HealthPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeFloat(value.health);
    }, buf -> new HealthPacket(buf.readFloat()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

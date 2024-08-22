package draylar.tiered.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReforgePacket() implements CustomPayload {

    public static final CustomPayload.Id<ReforgePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "reforge_packet"));

    public static final PacketCodec<RegistryByteBuf, ReforgePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
    }, buf -> new ReforgePacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

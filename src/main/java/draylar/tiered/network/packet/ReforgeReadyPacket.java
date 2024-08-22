package draylar.tiered.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReforgeReadyPacket(boolean disableButton) implements CustomPayload {

    public static final CustomPayload.Id<ReforgeReadyPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "reforge_ready_packet"));

    public static final PacketCodec<RegistryByteBuf, ReforgeReadyPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeBoolean(value.disableButton);
    }, buf -> new ReforgeReadyPacket(buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

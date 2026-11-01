package draylar.tiered.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ReforgeReadyPacket(boolean disableButton, boolean uniqueReforge) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReforgeReadyPacket> PACKET_ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("tiered", "reforge_ready_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgeReadyPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeBoolean(value.disableButton);
        buf.writeBoolean(value.uniqueReforge);
    }, buf -> new ReforgeReadyPacket(buf.readBoolean(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

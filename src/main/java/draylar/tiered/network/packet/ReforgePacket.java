package draylar.tiered.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReforgePacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReforgePacket> PACKET_ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tiered", "reforge_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgePacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
    }, buf -> new ReforgePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

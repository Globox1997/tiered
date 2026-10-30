package draylar.tiered.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReforgeScreenPacket(int mouseX, int mouseY, boolean reforgingScreen) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReforgeScreenPacket> PACKET_ID = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tiered", "reforge_screen_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgeScreenPacket> PACKET_CODEC = StreamCodec.ofMember((value, buf) -> {
        buf.writeInt(value.mouseX);
        buf.writeInt(value.mouseY);
        buf.writeBoolean(value.reforgingScreen);
    }, buf -> new ReforgeScreenPacket(buf.readInt(), buf.readInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

}

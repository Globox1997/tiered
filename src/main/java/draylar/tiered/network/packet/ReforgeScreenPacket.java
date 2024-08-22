package draylar.tiered.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ReforgeScreenPacket(int mouseX, int mouseY, boolean reforgingScreen) implements CustomPayload {

    public static final CustomPayload.Id<ReforgeScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("tiered", "reforge_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, ReforgeScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.mouseX);
        buf.writeInt(value.mouseY);
        buf.writeBoolean(value.reforgingScreen);
    }, buf -> new ReforgeScreenPacket(buf.readInt(), buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}

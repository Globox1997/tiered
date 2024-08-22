package draylar.tiered.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record TierComponent(String tier, float durable, int operation) {
    public static final TierComponent DEFAULT = new TierComponent("", -1, 2);

    public static final Codec<TierComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("tier").forGetter(TierComponent::tier),
            Codec.FLOAT.fieldOf("durable_factor").forGetter(TierComponent::durable), Codec.INT.fieldOf("operation").forGetter(TierComponent::operation)).apply(instance, TierComponent::new));

    public static final PacketCodec<ByteBuf, TierComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.STRING, TierComponent::tier, PacketCodecs.FLOAT, TierComponent::durable,
            PacketCodecs.INTEGER, TierComponent::operation, TierComponent::new);

}

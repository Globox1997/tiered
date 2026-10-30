package draylar.tiered.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TierComponent(String tier, float durable, int operation) {
    public static final TierComponent DEFAULT = new TierComponent("", -1, 2);

    public static final Codec<TierComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("tier").forGetter(TierComponent::tier),
            Codec.FLOAT.fieldOf("durable_factor").forGetter(TierComponent::durable), Codec.INT.fieldOf("operation").forGetter(TierComponent::operation)).apply(instance, TierComponent::new));

    public static final StreamCodec<ByteBuf, TierComponent> PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, TierComponent::tier, ByteBufCodecs.FLOAT, TierComponent::durable,
            ByteBufCodecs.INT, TierComponent::operation, TierComponent::new);

    public boolean hasDurableFactor() {
        return this.durable != DEFAULT.durable() && Float.isFinite(this.durable);
    }

}

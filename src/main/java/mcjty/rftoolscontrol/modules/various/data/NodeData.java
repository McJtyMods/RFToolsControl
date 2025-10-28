package mcjty.rftoolscontrol.modules.various.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Persistent data for the node block entity, tracking the configured channel,
 * node name, and linked processor position.
 */
public record NodeData(@Nullable String channel, @Nullable String node, @Nullable BlockPos processor) {

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("channel").forGetter(data -> Optional.ofNullable(data.channel)),
            Codec.STRING.optionalFieldOf("node").forGetter(data -> Optional.ofNullable(data.node)),
            BlockPos.CODEC.optionalFieldOf("processor").forGetter(data -> Optional.ofNullable(data.processor))
    ).apply(instance, (channel, node, processor) ->
            new NodeData(channel.orElse(null), node.orElse(null), processor.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), data -> Optional.ofNullable(data.channel),
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), data -> Optional.ofNullable(data.node),
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), data -> Optional.ofNullable(data.processor),
            (channel, node, processor) -> new NodeData(channel.orElse(null), node.orElse(null), processor.orElse(null)));

    public static NodeData createDefault() {
        return new NodeData(null, null, null);
    }

    public NodeData withChannel(@Nullable String channel) {
        return new NodeData(channel, node, processor);
    }

    public NodeData withNode(@Nullable String node) {
        return new NodeData(channel, node, processor);
    }

    public NodeData withProcessor(@Nullable BlockPos processor) {
        return new NodeData(channel, node, processor);
    }
}

package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extra data for the processor. This stores network nodes, channel, and log messages that
 * used to live in ProcessorTileEntity NBT. Kept separate to allow lightweight syncing
 * and persistence via attachments/data components.
 */
public record ProcessorExtraData(Map<String, BlockPos> networkNodes, String channel, List<String> logMessages) {

    public static final ProcessorExtraData DEFAULT = new ProcessorExtraData(Map.of(), "", List.of());

    public static final Codec<ProcessorExtraData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).fieldOf("networkNodes").forGetter(ProcessorExtraData::networkNodes),
            Codec.STRING.fieldOf("channel").forGetter(ProcessorExtraData::channel),
            Codec.STRING.listOf().fieldOf("logMessages").forGetter(ProcessorExtraData::logMessages)
    ).apply(instance, ProcessorExtraData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorExtraData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, BlockPos.STREAM_CODEC), ProcessorExtraData::networkNodes,
            ByteBufCodecs.STRING_UTF8, ProcessorExtraData::channel,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8), ProcessorExtraData::logMessages,
            ProcessorExtraData::new
    );

    public ProcessorExtraData withNetworkNodes(Map<String, BlockPos> networkNodes) { return new ProcessorExtraData(networkNodes, this.channel, this.logMessages); }
    public ProcessorExtraData withChannel(String channel) { return new ProcessorExtraData(this.networkNodes, channel, this.logMessages); }
    public ProcessorExtraData withLogMessages(List<String> logMessages) { return new ProcessorExtraData(this.networkNodes, this.channel, logMessages); }
}

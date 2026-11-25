package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProcessorGraphicsOperationsData(Map<String, GfxOp> operations) {

    public static final ProcessorGraphicsOperationsData DEFAULT = new ProcessorGraphicsOperationsData(Collections.emptyMap());

    public static final Codec<ProcessorGraphicsOperationsData> CODEC = Codec.unboundedMap(Codec.STRING, GfxOp.CODEC)
            .xmap(ProcessorGraphicsOperationsData::new, ProcessorGraphicsOperationsData::operations);

    private static final StreamCodec<RegistryFriendlyByteBuf, GfxOp> OP_STREAM_CODEC =
            StreamCodec.of((buffer, op) -> op.writeToBuf(buffer), GfxOp::readFromBuf);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorGraphicsOperationsData> STREAM_CODEC =
            ByteBufCodecs.<RegistryFriendlyByteBuf, String, GfxOp, Map<String, GfxOp>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, OP_STREAM_CODEC)
                    .map(ProcessorGraphicsOperationsData::new, ProcessorGraphicsOperationsData::operations);

    public ProcessorGraphicsOperationsData withOperations(Map<String, GfxOp> operations) { return new ProcessorGraphicsOperationsData(operations); }
}

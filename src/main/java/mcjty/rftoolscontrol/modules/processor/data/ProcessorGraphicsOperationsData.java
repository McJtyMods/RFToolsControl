package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.Map;

public record ProcessorGraphicsOperationsData(Map<String, GfxOp> operations) {

    public static final ProcessorGraphicsOperationsData DEFAULT = new ProcessorGraphicsOperationsData(Collections.emptyMap());

    public static final Codec<ProcessorGraphicsOperationsData> CODEC = Codec.unboundedMap(Codec.STRING, GfxOp.CODEC)
            .xmap(ProcessorGraphicsOperationsData::new, ProcessorGraphicsOperationsData::operations);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessorGraphicsOperationsData> STREAM_CODEC =
            StreamCodec.of(
            (buf, value) -> {
                buf.writeMap(value.operations(),
                        FriendlyByteBuf::writeUtf,
                        (b, op) -> op.writeToBuf(b));
            },
            (buf) -> {
                Map<String, GfxOp> map = buf.readMap(
                        FriendlyByteBuf::readUtf,
                        GfxOp::readFromBuf);
                return new ProcessorGraphicsOperationsData(map);
            }
    );

    public ProcessorGraphicsOperationsData withOperations(Map<String, GfxOp> operations) { return new ProcessorGraphicsOperationsData(operations); }
}

package mcjty.rftoolscontrol.modules.processor.data;

import com.mojang.serialization.Codec;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.Map;

/**
 * Placeholder data component for processor graphics operations.
 * Actual serialization will be implemented when ProcessorTileEntity migrates to data components.
 */
public record GraphicsOperationsData(Map<String, GfxOp> operations) {

    public static final GraphicsOperationsData DEFAULT = new GraphicsOperationsData(Collections.emptyMap());

    public static final Codec<GraphicsOperationsData> CODEC = Codec.unboundedMap(Codec.STRING, GfxOp.CODEC)
            .xmap(GraphicsOperationsData::new, GraphicsOperationsData::operations);

    public static final StreamCodec<RegistryFriendlyByteBuf, GraphicsOperationsData> STREAM_CODEC =
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
                return new GraphicsOperationsData(map);
            }
    );
}

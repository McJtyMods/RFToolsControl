package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public abstract class GfxOp {

    // Codec without NBT: encode as a small map {"type": int, "data": <subtype>}
    public static final Codec<GfxOp> CODEC = Codec.of(
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(GfxOp input, DynamicOps<T> ops, T prefix) {
                    var builder = ops.mapBuilder();
                    builder.add("type", ops.createInt(input.getType().ordinal()));
                    return switch (input.getType()) {
                        case OP_BOX -> GfxOpBox.CODEC.encodeStart(ops, (GfxOpBox) input).flatMap(d -> { builder.add("data", d); return builder.build(prefix); });
                        case OP_LINE -> GfxOpLine.CODEC.encodeStart(ops, (GfxOpLine) input).flatMap(d -> { builder.add("data", d); return builder.build(prefix); });
                        case OP_TEXT -> GfxOpText.CODEC.encodeStart(ops, (GfxOpText) input).flatMap(d -> { builder.add("data", d); return builder.build(prefix); });
                    };
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<GfxOp, T>> decode(DynamicOps<T> ops, T input) {
                    return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> {
                        T typeElem = map.get("type");
                        if (typeElem == null) {
                            return DataResult.error(() -> "Missing 'type' for GfxOp");
                        }
                        DataResult<Integer> typeRes = ops.getNumberValue(typeElem).map(Number::intValue);
                        return typeRes.flatMap(t -> {
                            GfxOpType[] values = GfxOpType.values();
                            if (t < 0 || t >= values.length) {
                                return DataResult.error(() -> "Invalid GfxOp type: " + t);
                            }
                            T dataElem = map.get("data");
                            if (dataElem == null) {
                                return DataResult.error(() -> "Missing 'data' for GfxOp");
                            }
                            GfxOpType type = values[t];
                            return switch (type) {
                                case OP_BOX -> GfxOpBox.CODEC.decode(ops, dataElem).map(p -> Pair.of(p.getFirst(), ops.empty()));
                                case OP_LINE -> GfxOpLine.CODEC.decode(ops, dataElem).map(p -> Pair.of(p.getFirst(), ops.empty()));
                                case OP_TEXT -> GfxOpText.CODEC.decode(ops, dataElem).map(p -> Pair.of(p.getFirst(), ops.empty()));
                            };
                        });
                    });
                }
            }
    );

    // Stream codec using a leading type byte and delegating to subclass codecs
    public static final StreamCodec<RegistryFriendlyByteBuf, GfxOp> STREAM_CODEC = StreamCodec.of(
            (buf, op) -> {
                buf.writeByte(op.getType().ordinal());
                switch (op.getType()) {
                    case OP_BOX -> GfxOpBox.STREAM_CODEC.encode(buf, (GfxOpBox) op);
                    case OP_LINE -> GfxOpLine.STREAM_CODEC.encode(buf, (GfxOpLine) op);
                    case OP_TEXT -> GfxOpText.STREAM_CODEC.encode(buf, (GfxOpText) op);
                }
            },
            buf -> {
                byte ord = ByteBufCodecs.BYTE.decode(buf);
                GfxOpType type = GfxOpType.values()[ord];
                return switch (type) {
                    case OP_BOX -> GfxOpBox.STREAM_CODEC.decode(buf);
                    case OP_LINE -> GfxOpLine.STREAM_CODEC.decode(buf);
                    case OP_TEXT -> GfxOpText.STREAM_CODEC.decode(buf);
                };
            }
    );

    public abstract void render(GuiGraphics graphics, MultiBufferSource buffer);

    public abstract GfxOpType getType();

    public String getOpName() {
        return switch (getType()) {
            case OP_BOX -> "box";
            case OP_LINE -> "line";
            case OP_TEXT -> "text";
        };
    }

    public static GfxOp readFromNBT(CompoundTag tag) {
        GfxOpType type = GfxOpType.values()[tag.getByte("type")];
        GfxOp op = createGfxOp(type);
        op.readFromNBTInternal(tag);
        return op;
    }

    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("type", (byte) getType().ordinal());
        writeToNBTInternal(tag);
        return tag;
    }

    protected abstract void readFromNBTInternal(CompoundTag tag);

    protected abstract void writeToNBTInternal(CompoundTag tag);

    public static GfxOp readFromBuf(FriendlyByteBuf buf) {
        GfxOpType type = GfxOpType.values()[buf.readByte()];
        GfxOp op = createGfxOp(type);
        op.readFromBufInternal(buf);
        return op;
    }

    private static GfxOp createGfxOp(GfxOpType type) {
        return switch (type) {
            case OP_BOX -> new GfxOpBox();
            case OP_LINE -> new GfxOpLine();
            case OP_TEXT -> new GfxOpText();
        };
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeByte(getType().ordinal());
        writeToBufInternal(buf);
    }

    protected abstract void readFromBufInternal(FriendlyByteBuf buf);

    protected abstract void writeToBufInternal(FriendlyByteBuf buf);


}

package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public abstract class GfxOp {

    public abstract void render(PoseStack matrixStack, MultiBufferSource buffer);

    public abstract GfxOpType getType();

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
        GfxOp op = null;
        switch (type) {
            case OP_BOX:
                op = new GfxOpBox();
                break;
            case OP_LINE:
                op = new GfxOpLine();
                break;
            case OP_TEXT:
                op = new GfxOpText();
                break;
        }
        return op;
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeByte(getType().ordinal());
        writeToBufInternal(buf);
    }

    protected abstract void readFromBufInternal(FriendlyByteBuf buf);

    protected abstract void writeToBufInternal(FriendlyByteBuf buf);


}

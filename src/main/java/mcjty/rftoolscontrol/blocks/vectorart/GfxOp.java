package mcjty.rftoolscontrol.blocks.vectorart;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class GfxOp {

    public abstract void render();

    public abstract GfxOpType getType();

    public static GfxOp readFromNBT(CompoundNBT tag) {
        GfxOpType type = GfxOpType.values()[tag.getByte("type")];
        GfxOp op = createGfxOp(type);
        op.readFromNBTInternal(tag);
        return op;
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putByte("type", (byte) getType().ordinal());
        writeToNBTInternal(tag);
        return tag;
    }

    protected abstract void readFromNBTInternal(CompoundNBT tag);

    protected abstract void writeToNBTInternal(CompoundNBT tag);

    public static GfxOp readFromBuf(PacketBuffer buf) {
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

    public void writeToBuf(PacketBuffer buf) {
        buf.writeByte(getType().ordinal());
        writeToBufInternal(buf);
    }

    protected abstract void readFromBufInternal(PacketBuffer buf);

    protected abstract void writeToBufInternal(PacketBuffer buf);


}

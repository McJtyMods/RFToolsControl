package mcjty.rftoolscontrol.blocks.vectorart;

import io.netty.buffer.ByteBuf;
import mcjty.lib.client.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;

public class GfxOpBox extends GfxOp {

    private int x;
    private int y;
    private int w;
    private int h;
    private int color;

    public GfxOpBox() {

    }

    public GfxOpBox(int x, int y, int w, int h, int color) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
    }

    @Override
    public void render() {
        RenderHelper.drawBeveledBox(x, y, x+w-1, y+h-1, color, color, color);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_BOX;
    }

    @Override
    protected void readFromNBTInternal(NBTTagCompound tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        w = tag.getByte("w");
        h = tag.getByte("h");
        color = tag.getInteger("color");
    }

    @Override
    protected void writeToNBTInternal(NBTTagCompound tag) {
        tag.setByte("x", (byte) x);
        tag.setByte("y", (byte) y);
        tag.setByte("w", (byte) w);
        tag.setByte("h", (byte) h);
        tag.setInteger("color", color);
    }

    @Override
    protected void readFromBufInternal(ByteBuf buf) {
        x = buf.readByte();
        y = buf.readByte();
        w = buf.readByte();
        h = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(ByteBuf buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        buf.writeByte(w);
        buf.writeByte(h);
        buf.writeInt(color);
    }
}

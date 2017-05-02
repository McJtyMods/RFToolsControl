package mcjty.rftoolscontrol.blocks.vectorart;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class GfxOpText extends GfxOp {

    private int x;
    private int y;
    private String text;
    private int color;

    public GfxOpText() {

    }

    public GfxOpText(int x, int y, String text, int color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    @Override
    public void render() {
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_TEXT;
    }

    @Override
    protected void readFromNBTInternal(NBTTagCompound tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        text = tag.getString("text");
        color = tag.getInteger("color");
    }

    @Override
    protected void writeToNBTInternal(NBTTagCompound tag) {
        tag.setByte("x", (byte) x);
        tag.setByte("y", (byte) y);
        tag.setString("text", text);
        tag.setInteger("color", color);
    }

    @Override
    protected void readFromBufInternal(ByteBuf buf) {
        x = buf.readByte();
        y = buf.readByte();
        text = NetworkTools.readString(buf);
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(ByteBuf buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        NetworkTools.writeString(buf, text);
        buf.writeInt(color);
    }
}

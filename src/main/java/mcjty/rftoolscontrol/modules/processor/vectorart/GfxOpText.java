package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        Minecraft.getInstance().fontRenderer.renderString(text, x, y, color, false, matrixStack.getLast().getMatrix(), buffer, false, 0, 0xf000f0);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_TEXT;
    }

    @Override
    protected void readFromNBTInternal(CompoundNBT tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        text = tag.getString("text");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundNBT tag) {
        tag.putByte("x", (byte) x);
        tag.putByte("y", (byte) y);
        tag.putString("text", text);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(PacketBuffer buf) {
        x = buf.readByte();
        y = buf.readByte();
        text = buf.readString(32767);
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(PacketBuffer buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        buf.writeString(text);
        buf.writeInt(color);
    }
}

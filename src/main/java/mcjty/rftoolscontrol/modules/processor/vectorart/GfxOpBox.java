package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        RenderHelper.drawBeveledBox(matrixStack, buffer, x, y, x+w-1, y+h-1, color, color, color, RenderHelper.MAX_BRIGHTNESS);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_BOX;
    }

    @Override
    protected void readFromNBTInternal(CompoundNBT tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        w = tag.getByte("w");
        h = tag.getByte("h");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundNBT tag) {
        tag.putByte("x", (byte) x);
        tag.putByte("y", (byte) y);
        tag.putByte("w", (byte) w);
        tag.putByte("h", (byte) h);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(PacketBuffer buf) {
        x = buf.readByte();
        y = buf.readByte();
        w = buf.readByte();
        h = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(PacketBuffer buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        buf.writeByte(w);
        buf.writeByte(h);
        buf.writeInt(color);
    }
}

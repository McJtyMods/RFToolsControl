package mcjty.rftoolscontrol.blocks.vectorart;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mcjty.lib.client.CustomRenderTypes;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class GfxOpLine extends GfxOp {

    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int color;

    public GfxOpLine() {

    }

    public GfxOpLine(int x1, int y1, int x2, int y2, int color) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.OVERLAY_LINES);
        builder.pos(matrixStack.getLast().getMatrix(), x1, y1, 0).color(red, green, blue, alpha).endVertex();
        builder.pos(matrixStack.getLast().getMatrix(), x2, y2, 0).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_LINE;
    }

    @Override
    protected void readFromNBTInternal(CompoundNBT tag) {
        x1 = tag.getByte("x1");
        y1 = tag.getByte("y1");
        x2 = tag.getByte("x2");
        y2 = tag.getByte("y2");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundNBT tag) {
        tag.putByte("x1", (byte) x1);
        tag.putByte("y1", (byte) y1);
        tag.putByte("x2", (byte) x2);
        tag.putByte("y2", (byte) y2);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(PacketBuffer buf) {
        x1 = buf.readByte();
        y1 = buf.readByte();
        x2 = buf.readByte();
        y2 = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(PacketBuffer buf) {
        buf.writeByte(x1);
        buf.writeByte(y1);
        buf.writeByte(x2);
        buf.writeByte(y2);
        buf.writeInt(color);
    }
}

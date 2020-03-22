package mcjty.rftoolscontrol.blocks.vectorart;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.BufferBuilder;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.CompoundNBT;
import org.lwjgl.opengl.GL11;

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
    public void render() {
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(x1, y1, 0.0D).endVertex();
        vertexbuffer.pos(x2, y2, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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
        color = tag.getInteger("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundNBT tag) {
        tag.setByte("x1", (byte) x1);
        tag.setByte("y1", (byte) y1);
        tag.setByte("x2", (byte) x2);
        tag.setByte("y2", (byte) y2);
        tag.setInteger("color", color);
    }

    @Override
    protected void readFromBufInternal(ByteBuf buf) {
        x1 = buf.readByte();
        y1 = buf.readByte();
        x2 = buf.readByte();
        y2 = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(ByteBuf buf) {
        buf.writeByte(x1);
        buf.writeByte(y1);
        buf.writeByte(x2);
        buf.writeByte(y2);
        buf.writeInt(color);
    }
}

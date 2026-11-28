package mcjty.rftoolscontrol.modules.processor.vectorart;

import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    public void render(GuiGraphics graphics, MultiBufferSource buffer) {
        renderAtDepth(graphics, buffer, -1.0f);
    }

    void renderAtDepth(GuiGraphics graphics, MultiBufferSource buffer, float depth) {
        int right = x + w;
        int bottom = y + h;
        RenderHelper.drawQuadGui(graphics.pose(), buffer.getBuffer(CustomRenderTypes.QUADS_NOTEXTURE),
                Math.min(x, right), Math.max(x, right), Math.min(y, bottom), Math.max(y, bottom),
                depth, color, RenderHelper.MAX_BRIGHTNESS);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_BOX;
    }

    @Override
    protected void readFromNBTInternal(CompoundTag tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        w = tag.getByte("w");
        h = tag.getByte("h");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundTag tag) {
        tag.putByte("x", (byte) x);
        tag.putByte("y", (byte) y);
        tag.putByte("w", (byte) w);
        tag.putByte("h", (byte) h);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(FriendlyByteBuf buf) {
        x = buf.readByte();
        y = buf.readByte();
        w = buf.readByte();
        h = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(FriendlyByteBuf buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        buf.writeByte(w);
        buf.writeByte(h);
        buf.writeInt(color);
    }
}

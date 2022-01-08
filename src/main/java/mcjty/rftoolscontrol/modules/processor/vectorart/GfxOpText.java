package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
    public void render(PoseStack matrixStack, MultiBufferSource buffer) {
        Minecraft.getInstance().font.drawInBatch(text, x, y, color, false, matrixStack.last().pose(), buffer, false, 0, RenderHelper.MAX_BRIGHTNESS);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_TEXT;
    }

    @Override
    protected void readFromNBTInternal(CompoundTag tag) {
        x = tag.getByte("x");
        y = tag.getByte("y");
        text = tag.getString("text");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundTag tag) {
        tag.putByte("x", (byte) x);
        tag.putByte("y", (byte) y);
        tag.putString("text", text);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(FriendlyByteBuf buf) {
        x = buf.readByte();
        y = buf.readByte();
        text = buf.readUtf(32767);
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(FriendlyByteBuf buf) {
        buf.writeByte(x);
        buf.writeByte(y);
        buf.writeUtf(text);
        buf.writeInt(color);
    }
}

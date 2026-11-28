package mcjty.rftoolscontrol.modules.processor.vectorart;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public abstract class GfxOp {

    public abstract void render(GuiGraphics graphics, MultiBufferSource buffer);

    public static void renderAll(List<GfxOp> operations, GuiGraphics graphics, MultiBufferSource buffer) {
        // Keep boxes between the screen backing and the plane used for lines and text.
        float depthStep = 1.0f / Math.max(operations.size(), 8);
        int boxLayer = 0;
        for (GfxOp operation : operations) {
            if (operation instanceof GfxOpBox box) {
                box.renderAtDepth(graphics, buffer, -1.0f + boxLayer * depthStep);
                boxLayer++;
            } else {
                operation.render(graphics, buffer);
            }
        }
    }

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
        return switch (type) {
            case OP_BOX -> new GfxOpBox();
            case OP_LINE -> new GfxOpLine();
            case OP_TEXT -> new GfxOpText();
        };
    }

    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeByte(getType().ordinal());
        writeToBufInternal(buf);
    }

    protected abstract void readFromBufInternal(FriendlyByteBuf buf);

    protected abstract void writeToBufInternal(FriendlyByteBuf buf);


}

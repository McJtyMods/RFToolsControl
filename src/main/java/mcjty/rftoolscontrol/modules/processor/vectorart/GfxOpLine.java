package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GfxOpLine extends GfxOp {

    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int color;

    public static final Codec<GfxOpLine> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("x1").forGetter(o -> (byte) o.x1),
            Codec.BYTE.fieldOf("y1").forGetter(o -> (byte) o.y1),
            Codec.BYTE.fieldOf("x2").forGetter(o -> (byte) o.x2),
            Codec.BYTE.fieldOf("y2").forGetter(o -> (byte) o.y2),
            Codec.INT.fieldOf("color").forGetter(o -> o.color)
    ).apply(instance, (x1, y1, x2, y2, color) -> new GfxOpLine(x1, y1, x2, y2, color)));

    public static final StreamCodec<RegistryFriendlyByteBuf, GfxOpLine> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, o -> (byte) o.x1,
            ByteBufCodecs.BYTE, o -> (byte) o.y1,
            ByteBufCodecs.BYTE, o -> (byte) o.x2,
            ByteBufCodecs.BYTE, o -> (byte) o.y2,
            ByteBufCodecs.INT, o -> o.color,
            (x1, y1, x2, y2, color) -> new GfxOpLine(x1, y1, x2, y2, color)
    );

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
    public void render(GuiGraphics graphics, MultiBufferSource buffer) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        VertexConsumer builder = buffer.getBuffer(CustomRenderTypes.OVERLAY_LINES);
        RenderHelper.line(builder, graphics.pose(), x1, y1, 0, x2, y2, 0, red, green, blue, alpha);
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_LINE;
    }

    @Override
    protected void readFromNBTInternal(CompoundTag tag) {
        x1 = tag.getByte("x1");
        y1 = tag.getByte("y1");
        x2 = tag.getByte("x2");
        y2 = tag.getByte("y2");
        color = tag.getInt("color");
    }

    @Override
    protected void writeToNBTInternal(CompoundTag tag) {
        tag.putByte("x1", (byte) x1);
        tag.putByte("y1", (byte) y1);
        tag.putByte("x2", (byte) x2);
        tag.putByte("y2", (byte) y2);
        tag.putInt("color", color);
    }

    @Override
    protected void readFromBufInternal(FriendlyByteBuf buf) {
        x1 = buf.readByte();
        y1 = buf.readByte();
        x2 = buf.readByte();
        y2 = buf.readByte();
        color = buf.readInt();
    }

    @Override
    protected void writeToBufInternal(FriendlyByteBuf buf) {
        buf.writeByte(x1);
        buf.writeByte(y1);
        buf.writeByte(x2);
        buf.writeByte(y2);
        buf.writeInt(color);
    }
}

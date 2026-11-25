package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GfxOpBox extends GfxOp {

    private int x;
    private int y;
    private int w;
    private int h;
    private int color;

    public static final Codec<GfxOpBox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("x").forGetter(o -> (byte) o.x),
            Codec.BYTE.fieldOf("y").forGetter(o -> (byte) o.y),
            Codec.BYTE.fieldOf("w").forGetter(o -> (byte) o.w),
            Codec.BYTE.fieldOf("h").forGetter(o -> (byte) o.h),
            Codec.INT.fieldOf("color").forGetter(o -> o.color)
    ).apply(instance, (x, y, w, h, color) -> new GfxOpBox(x, y, w, h, color)));

    public static final StreamCodec<RegistryFriendlyByteBuf, GfxOpBox> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, o -> (byte) o.x,
            ByteBufCodecs.BYTE, o -> (byte) o.y,
            ByteBufCodecs.BYTE, o -> (byte) o.w,
            ByteBufCodecs.BYTE, o -> (byte) o.h,
            ByteBufCodecs.INT, o -> o.color,
            (x, y, w, h, color) -> new GfxOpBox(x, y, w, h, color)
    );

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
    public void render(GuiGraphicsExtractor graphics) {
        graphics.fill(x, y, x + w, y + h, color);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(pose, RenderTypes.debugQuads(), (transform, vertices) -> {
            vertices.addVertex(transform, x, y, 0).setColor(color);
            vertices.addVertex(transform, x + w, y, 0).setColor(color);
            vertices.addVertex(transform, x + w, y + h, 0).setColor(color);
            vertices.addVertex(transform, x, y + h, 0).setColor(color);
        });
    }

    @Override
    public GfxOpType getType() {
        return GfxOpType.OP_BOX;
    }

    @Override
    protected void readFromNBTInternal(CompoundTag tag) {
        x = tag.getByteOr("x", (byte) 0);
        y = tag.getByteOr("y", (byte) 0);
        w = tag.getByteOr("w", (byte) 0);
        h = tag.getByteOr("h", (byte) 0);
        color = tag.getIntOr("color", 0);
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

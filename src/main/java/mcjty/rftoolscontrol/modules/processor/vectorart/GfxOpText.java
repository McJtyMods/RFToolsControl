package mcjty.rftoolscontrol.modules.processor.vectorart;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class GfxOpText extends GfxOp {

    private int x;
    private int y;
    private String text;
    private int color;

    public static final Codec<GfxOpText> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("x").forGetter(o -> (byte) o.x),
            Codec.BYTE.fieldOf("y").forGetter(o -> (byte) o.y),
            Codec.STRING.fieldOf("text").forGetter(o -> o.text),
            Codec.INT.fieldOf("color").forGetter(o -> o.color)
    ).apply(instance, (x, y, text, color) -> new GfxOpText(x, y, text, color)));

    public static final StreamCodec<RegistryFriendlyByteBuf, GfxOpText> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, o -> (byte) o.x,
            ByteBufCodecs.BYTE, o -> (byte) o.y,
            ByteBufCodecs.STRING_UTF8, o -> o.text,
            ByteBufCodecs.INT, o -> o.color,
            (x, y, text, color) -> new GfxOpText(x, y, text, color)
    );

    public GfxOpText() {

    }

    public GfxOpText(int x, int y, String text, int color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer) {
        RenderHelper.renderText(Minecraft.getInstance().font, text, x, y, color, graphics.pose(), buffer, RenderHelper.MAX_BRIGHTNESS);
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

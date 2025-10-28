package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record InteractionScreenModule(String line,
                                      String button,
                                      String signal,
                                      int color,
                                      int buttonColor,
                                      TextAlign align,
                                      ResourceKey<Level> dim,
                                      BlockPos coordinate) implements IScreenModule<InteractionScreenModule, IModuleDataBoolean> {

    public static final InteractionScreenModule DEFAULT = new InteractionScreenModule(
            "",
            "",
            "",
            0xffffff,
            0xffffff,
            TextAlign.ALIGN_LEFT,
            Level.OVERWORLD,
            BlockPosTools.INVALID
    );

    public static final Codec<InteractionScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("line").forGetter(InteractionScreenModule::line),
            Codec.STRING.fieldOf("button").forGetter(InteractionScreenModule::button),
            Codec.STRING.fieldOf("signal").forGetter(InteractionScreenModule::signal),
            Codec.INT.fieldOf("color").forGetter(InteractionScreenModule::color),
            Codec.INT.fieldOf("buttonColor").forGetter(InteractionScreenModule::buttonColor),
            Codec.STRING.fieldOf("align").forGetter(module -> module.align().getSerializedName()),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(InteractionScreenModule::dim),
            BlockPos.CODEC.fieldOf("coordinate").forGetter(InteractionScreenModule::coordinate)
    ).apply(instance, InteractionScreenModule::create));

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, InteractionScreenModule::line,
            ByteBufCodecs.STRING_UTF8, InteractionScreenModule::button,
            ByteBufCodecs.STRING_UTF8, InteractionScreenModule::signal,
            ByteBufCodecs.INT, InteractionScreenModule::color,
            ByteBufCodecs.INT, InteractionScreenModule::buttonColor,
            ByteBufCodecs.STRING_UTF8, module -> module.align().getSerializedName(),
            ResourceKey.streamCodec(Registries.DIMENSION), InteractionScreenModule::dim,
            BlockPos.STREAM_CODEC, InteractionScreenModule::coordinate,
            InteractionScreenModule::create
    );

    private static InteractionScreenModule create(String line, String button, String signal, int color, int buttonColor, String align, ResourceKey<Level> dim, BlockPos coordinate) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, TextAlign.get(align), dim, coordinate);
    }

    public InteractionScreenModule withLine(String line) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withButton(String button) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withSignal(String signal) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withColor(int color) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withButtonColor(int buttonColor) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withAlign(TextAlign align) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withDim(ResourceKey<Level> dim) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withCoordinate(BlockPos coordinate) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    public InteractionScreenModule withTarget(ResourceKey<Level> dim, BlockPos coordinate) {
        return new InteractionScreenModule(line, button, signal, color, buttonColor, align, dim, coordinate);
    }

    @Override
    public IModuleDataBoolean getData(IScreenDataHelper helper, Level worldObj, long millis) {
        return null;
    }

    @Override
    public int getRfPerTick() {
        return Config.INTERACTMODULE_RFPERTICK.get();
    }

    @Override
    public InteractionScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        if (!Objects.equals(world.dimension(), dim)) {
            return withCoordinate(BlockPosTools.INVALID);
        }
        if (!BlockPosTools.isValid(coordinate)) {
            return this;
        }
        int dx = Math.abs(coordinate.getX() - pos.getX());
        int dy = Math.abs(coordinate.getY() - pos.getY());
        int dz = Math.abs(coordinate.getZ() - pos.getZ());
        if (dx <= 64 && dy <= 64 && dz <= 64) {
            return this;
        }
        return withCoordinate(BlockPosTools.INVALID);
    }

    @Override
    public @NotNull ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
        int xoffset = line.isEmpty() ? 5 : 80;
        if (x < xoffset) {
            return moduleStack;
        }

        if (!BlockPosTools.isValid(coordinate)) {
            if (player != null) {
                player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to a processor!"), false);
            }
            return moduleStack;
        }

        if (!Objects.equals(world.dimension(), dim)) {
            return moduleStack;
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return moduleStack;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != ProcessorModule.PROCESSOR.block().get()) {
            return moduleStack;
        }

        if (clicked) {
            BlockEntity te = world.getBlockEntity(coordinate);
            if (te instanceof ProcessorTileEntity processor) {
                processor.signal(signal);
            }
        }
        return moduleStack;
    }
}

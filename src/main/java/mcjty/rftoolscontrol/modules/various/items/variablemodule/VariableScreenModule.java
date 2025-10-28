package mcjty.rftoolscontrol.modules.various.items.variablemodule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVariable;
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

public record VariableScreenModule(String line,
                                   int color,
                                   int varColor,
                                   TextAlign align,
                                   int varIdx,
                                   ResourceKey<Level> dim,
                                   BlockPos coordinate,
                                   String monitorName) implements IScreenModule<VariableScreenModule, ModuleDataVariable> {

    public static final VariableScreenModule DEFAULT = new VariableScreenModule(
            "",
            0xffffff,
            0xffffff,
            TextAlign.ALIGN_LEFT,
            -1,
            Level.OVERWORLD,
            BlockPosTools.INVALID,
            ""
    );

    public static final Codec<VariableScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("line").forGetter(VariableScreenModule::line),
            Codec.INT.fieldOf("color").forGetter(VariableScreenModule::color),
            Codec.INT.fieldOf("varColor").forGetter(VariableScreenModule::varColor),
            TextAlign.CODEC.fieldOf("align").forGetter(VariableScreenModule::align),
            Codec.INT.fieldOf("varIdx").forGetter(VariableScreenModule::varIdx),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(VariableScreenModule::dim),
            BlockPos.CODEC.fieldOf("coordinate").forGetter(VariableScreenModule::coordinate),
            Codec.STRING.fieldOf("monitorName").forGetter(VariableScreenModule::monitorName)
    ).apply(instance, VariableScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VariableScreenModule> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, VariableScreenModule::line,
            ByteBufCodecs.INT, VariableScreenModule::color,
            ByteBufCodecs.INT, VariableScreenModule::varColor,
            TextAlign.STREAM_CODEC, VariableScreenModule::align,
            ByteBufCodecs.INT, VariableScreenModule::varIdx,
            ResourceKey.streamCodec(Registries.DIMENSION), VariableScreenModule::dim,
            BlockPos.STREAM_CODEC, VariableScreenModule::coordinate,
            ByteBufCodecs.STRING_UTF8, VariableScreenModule::monitorName,
            VariableScreenModule::new
    );

    public VariableScreenModule withLine(String line) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withColor(int color) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withVarColor(int varColor) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withAlign(TextAlign align) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withVarIdx(int varIdx) {
        int clamped = Math.max(-1, Math.min(ProcessorTileEntity.MAXVARS - 1, varIdx));
        return new VariableScreenModule(line, color, varColor, align, clamped, dim, coordinate, monitorName);
    }

    public VariableScreenModule withDim(ResourceKey<Level> dim) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withCoordinate(BlockPos coordinate) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withMonitorName(String monitorName) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, coordinate, monitorName);
    }

    public VariableScreenModule withTarget(ResourceKey<Level> targetDim, BlockPos targetCoordinate, String name) {
        return new VariableScreenModule(line, color, varColor, align, varIdx, targetDim, targetCoordinate, name);
    }

    public VariableScreenModule clearTarget() {
        return new VariableScreenModule(line, color, varColor, align, varIdx, dim, BlockPosTools.INVALID, "");
    }

    @Override
    public ModuleDataVariable getData(IScreenDataHelper h, Level worldObj, long millis) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!BlockPosTools.isValid(coordinate) || !LevelTools.isLoaded(world, coordinate)) {
            return null;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != ProcessorModule.PROCESSOR.block().get()) {
            return null;
        }

        if (varIdx < 0 || varIdx >= ProcessorTileEntity.MAXVARS) {
            return null;
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te instanceof ProcessorTileEntity processor) {
            Parameter parameter = processor.getParameter(varIdx);
            return new ModuleDataVariable(parameter);
        }
        return null;
    }

    @Override
    public int getRfPerTick() {
        return Config.VARIABLEMODULE_RFPERTICK.get();
    }

    @Override
    public VariableScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        if (!Objects.equals(world.dimension(), dim)) {
            return withDim(world.dimension()).clearTarget();
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
        return clearTarget();
    }

    @Override
    public @NotNull ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
        if (!BlockPosTools.isValid(coordinate)) {
            if (player != null) {
                player.displayClientMessage(ComponentFactory.literal(ChatFormatting.RED + "Module is not linked to a processor!"), false);
            }
            return moduleStack;
        }
        return moduleStack;
    }
}

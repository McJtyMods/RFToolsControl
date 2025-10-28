package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.setup.Config;
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

import java.util.List;

public class ConsoleScreenModule implements IScreenModule<ConsoleScreenModule, ModuleDataLog> {
    public static final ConsoleScreenModule DEFAULT = new ConsoleScreenModule(Level.OVERWORLD, BlockPosTools.INVALID);

    public static final Codec<ConsoleScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(ConsoleScreenModule::getDim),
            BlockPos.CODEC.fieldOf("pos").forGetter(ConsoleScreenModule::getCoordinate)
    ).apply(instance, ConsoleScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConsoleScreenModule> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), ConsoleScreenModule::getDim,
            BlockPos.STREAM_CODEC, ConsoleScreenModule::getCoordinate,
            ConsoleScreenModule::new
    );

    private final ResourceKey<Level> dim;
    private final BlockPos coordinate;

    public ConsoleScreenModule(ResourceKey<Level> dim, BlockPos coordinate) {
        this.dim = dim;
        this.coordinate = coordinate;
    }

    public ResourceKey<Level> getDim() {
        return dim;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    public ConsoleScreenModule withDim(ResourceKey<Level> dim) {
        return new ConsoleScreenModule(dim, coordinate);
    }

    public ConsoleScreenModule withCoordinate(BlockPos pos) {
        return new ConsoleScreenModule(dim, pos);
    }

    @Override
    public ModuleDataLog getData(IScreenDataHelper h, Level worldObj, long millis) {
        Level world = LevelTools.getLevel(worldObj, dim);
        if (world == null) {
            return null;
        }

        if (!LevelTools.isLoaded(world, coordinate)) {
            return null;
        }

        Block block = world.getBlockState(coordinate).getBlock();
        if (block != ProcessorModule.PROCESSOR.block().get()) {
            return null;
        }

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te instanceof ProcessorTileEntity processor) {
            List<String> lastMessages = processor.getLastMessages(12);
            return new ModuleDataLog(lastMessages);
        }
        return null;
    }

    @Override
    public int getRfPerTick() {
        return Config.CONSOLEMODULE_RFPERTICK.get();
    }

    @Override
    public ConsoleScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        return this;
    }

    @Override
    public @NotNull ItemStack mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked, Player player) {
        return moduleStack;
    }
}

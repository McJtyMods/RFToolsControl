package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.control.parameters.Tuple;
import mcjty.rftoolsbase.api.screens.IScreenDataHelper;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
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

public record VectorArtScreenModule(ResourceKey<Level> dim,
                                    BlockPos coordinate,
                                    String monitorName) implements IScreenModule<VectorArtScreenModule, ModuleDataVectorArt> {

    public static final VectorArtScreenModule DEFAULT = new VectorArtScreenModule(
            Level.OVERWORLD,
            BlockPosTools.INVALID,
            ""
    );

    public static final Codec<VectorArtScreenModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dim").forGetter(VectorArtScreenModule::dim),
            BlockPos.CODEC.fieldOf("coordinate").forGetter(VectorArtScreenModule::coordinate),
            Codec.STRING.fieldOf("monitorName").forGetter(VectorArtScreenModule::monitorName)
    ).apply(instance, VectorArtScreenModule::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VectorArtScreenModule> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), VectorArtScreenModule::dim,
            BlockPos.STREAM_CODEC, VectorArtScreenModule::coordinate,
            ByteBufCodecs.STRING_UTF8, VectorArtScreenModule::monitorName,
            VectorArtScreenModule::new
    );

    public VectorArtScreenModule withTarget(ResourceKey<Level> newDim, BlockPos newCoordinate, String name) {
        return new VectorArtScreenModule(newDim, newCoordinate, name);
    }

    public VectorArtScreenModule clearTarget() {
        return new VectorArtScreenModule(dim, BlockPosTools.INVALID, "");
    }

    @Override
    public ModuleDataVectorArt getData(IScreenDataHelper helper, Level worldObj, long millis) {
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

        BlockEntity te = world.getBlockEntity(coordinate);
        if (te instanceof ProcessorTileEntity processor) {
            return new ModuleDataVectorArt(processor.getGfxOps(), processor.getOrderedOps());
        }
        return null;
    }

    @Override
    public int getRfPerTick() {
        return Config.VECTORARTMODULE_RFPERTICK.get();
    }

    @Override
    public VectorArtScreenModule validate(Level world, BlockPos pos, boolean isPlus) {
        if (!Objects.equals(world.dimension(), dim)) {
            return new VectorArtScreenModule(world.dimension(), BlockPosTools.INVALID, "");
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
                processor.signal(new Tuple(x, y + 7));
            }
        }
        return moduleStack;
    }
}

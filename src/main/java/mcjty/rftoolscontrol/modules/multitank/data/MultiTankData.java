package mcjty.rftoolscontrol.modules.multitank.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.TANKS;

public record MultiTankData(List<FluidStack> fluids) {

    public static final Codec<MultiTankData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(FluidStack.OPTIONAL_CODEC).fieldOf("fluids").forGetter(MultiTankData::fluids)
    ).apply(instance, MultiTankData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiTankData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), MultiTankData::fluids,
            MultiTankData::new
    );

    public static MultiTankData createDefault() {
        return new MultiTankData(defaultFluids());
    }

    public MultiTankData withFluids(List<FluidStack> fluids) {
        return new MultiTankData(copyFluids(fluids));
    }

    public static MultiTankData of(List<FluidStack> fluids) {
        return new MultiTankData(copyFluids(fluids));
    }

    private static List<FluidStack> defaultFluids() {
        List<FluidStack> defaults = new ArrayList<>(TANKS);
        for (int i = 0; i < TANKS; i++) {
            defaults.add(FluidStack.EMPTY);
        }
        return defaults;
    }

    private static List<FluidStack> copyFluids(List<FluidStack> fluids) {
        List<FluidStack> copy = new ArrayList<>(TANKS);
        for (int i = 0; i < TANKS; i++) {
            FluidStack stack = i < fluids.size() ? fluids.get(i) : FluidStack.EMPTY;
            copy.add(stack.isEmpty() ? FluidStack.EMPTY : stack.copy());
        }
        return copy;
    }
}

package mcjty.rftoolscontrol.modules.processor.logic;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ParameterSerializer implements ISerializer<Parameter> {
    @Override
    public Function<RegistryFriendlyByteBuf, Parameter> getDeserializer() {
        return ParameterTools::readFromBuf;
    }

    @Override
    public BiConsumer<RegistryFriendlyByteBuf, Parameter> getSerializer() {
        return ParameterTools::writeToBuf;
    }
}

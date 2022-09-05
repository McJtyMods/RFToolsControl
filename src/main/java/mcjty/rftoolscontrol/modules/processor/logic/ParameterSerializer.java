package mcjty.rftoolscontrol.modules.processor.logic;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ParameterSerializer implements ISerializer<Parameter> {
    @Override
    public Function<FriendlyByteBuf, Parameter> getDeserializer() {
        return ParameterTools::readFromBuf;
    }

    @Override
    public BiConsumer<FriendlyByteBuf, Parameter> getSerializer() {
        return ParameterTools::writeToBuf;
    }
}

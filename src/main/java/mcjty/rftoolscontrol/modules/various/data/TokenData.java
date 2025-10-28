package mcjty.rftoolscontrol.modules.various.data;

import com.mojang.serialization.Codec;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component for the Token item. Wraps a single Parameter value.
 */
public record TokenData(Parameter parameter) {

    public static final Codec<TokenData> CODEC = Parameter.CODEC.xmap(TokenData::new, TokenData::parameter);
    public static final StreamCodec<RegistryFriendlyByteBuf, TokenData> STREAM_CODEC = Parameter.STREAM_CODEC.map(TokenData::new, TokenData::parameter);
}

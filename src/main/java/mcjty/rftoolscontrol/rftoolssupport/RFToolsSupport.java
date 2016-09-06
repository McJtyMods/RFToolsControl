package mcjty.rftoolscontrol.rftoolssupport;

import com.google.common.base.Function;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(ModuleDataVariable.ID, buf -> {
                ParameterType type = ParameterType.values()[buf.readByte()];
                Parameter.Builder builder = Parameter.builder().type(type);
                switch (type) {
                    case PAR_STRING:
                        builder.value(ParameterValue.constant(NetworkTools.readString(buf)));
                        break;
                    case PAR_INTEGER:
                        builder.value(ParameterValue.constant(buf.readInt()));
                        break;
                    case PAR_FLOAT:
                        builder.value(ParameterValue.constant(buf.readFloat()));
                        break;
                    case PAR_SIDE:
                        String nodeName = NetworkTools.readString(buf);
                        int sideIdx = buf.readByte();
                        EnumFacing side = sideIdx == -1 ? null : EnumFacing.values()[sideIdx];
                        builder.value(ParameterValue.constant(new BlockSide(nodeName, side)));
                        break;
                    case PAR_BOOLEAN:
                        builder.value(ParameterValue.constant(buf.readBoolean()));
                        break;
                    case PAR_INVENTORY:
                        String nodeName2 = NetworkTools.readString(buf);
                        int sideIdx2 = buf.readByte();
                        EnumFacing side2 = EnumFacing.values()[sideIdx2];
                        sideIdx2 = buf.readByte();
                        EnumFacing intSide = sideIdx2 == -1 ? null : EnumFacing.values()[sideIdx2];
                        builder.value(ParameterValue.constant(new Inventory(nodeName2, side2, intSide)));
                        break;
                    case PAR_ITEM:
                        builder.value(ParameterValue.constant(NetworkTools.readItemStack(buf)));
                        break;
                }
                return new ModuleDataVariable(builder.build());
            });
            return null;
        }
    }

}

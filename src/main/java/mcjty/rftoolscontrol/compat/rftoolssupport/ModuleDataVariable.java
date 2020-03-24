package mcjty.rftoolscontrol.compat.rftoolssupport;

import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.ParameterTools;
import net.minecraft.network.PacketBuffer;

public class ModuleDataVariable implements IModuleData {

    public static final String ID = RFToolsControl.MODID + ":VAR";

    private final Parameter parameter;

    public ModuleDataVariable(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void writeToBuf(PacketBuffer buf) {
        if (parameter == null) {
            buf.writeByte(-1);
            return;
        }
        ParameterTools.writeToBuf(buf, parameter);
    }
}

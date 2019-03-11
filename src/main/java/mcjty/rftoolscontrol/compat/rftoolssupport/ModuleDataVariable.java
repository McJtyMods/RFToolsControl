package mcjty.rftoolscontrol.compat.rftoolssupport;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.logic.ParameterTools;

public class ModuleDataVariable implements mcjty.rftools.api.screens.data.IModuleData {

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
    public void writeToBuf(ByteBuf buf) {
        if (parameter == null) {
            buf.writeByte(-1);
            return;
        }
        ParameterTools.writeToBuf(buf, parameter);
    }
}

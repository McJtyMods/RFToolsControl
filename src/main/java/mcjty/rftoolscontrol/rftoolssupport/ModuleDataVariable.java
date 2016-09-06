package mcjty.rftoolscontrol.rftoolssupport;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import net.minecraft.item.ItemStack;

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
        buf.writeByte(parameter.getParameterType().ordinal());
        Object value = parameter.getParameterValue().getValue();
        switch (parameter.getParameterType()) {
            case PAR_STRING:
                NetworkTools.writeString(buf, (String) value);
                break;
            case PAR_INTEGER:
                buf.writeInt((Integer) value);
                break;
            case PAR_FLOAT:
                buf.writeFloat((Float) value);
                break;
            case PAR_SIDE:
                BlockSide bs = (BlockSide) value;
                NetworkTools.writeString(buf, bs.getNodeName());
                buf.writeByte(bs.getSide() == null ? -1 : bs.getSide().ordinal());
                break;
            case PAR_BOOLEAN:
                buf.writeBoolean((Boolean) value);
                break;
            case PAR_INVENTORY:
                Inventory inv = (Inventory) value;
                NetworkTools.writeString(buf, inv.getNodeName());
                buf.writeByte(inv.getSide().ordinal());
                buf.writeByte(inv.getIntSide() == null ? -1 : inv.getIntSide().ordinal());
                break;
            case PAR_ITEM:
                NetworkTools.writeItemStack(buf, (ItemStack) value);
                break;
        }
    }
}

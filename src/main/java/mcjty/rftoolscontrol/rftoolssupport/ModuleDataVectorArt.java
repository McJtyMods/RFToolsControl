package mcjty.rftoolscontrol.rftoolssupport;

import io.netty.buffer.ByteBuf;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;

import java.util.List;
import java.util.Map;

public class ModuleDataVectorArt implements mcjty.rftools.api.screens.data.IModuleData {

    public static final String ID = RFToolsControl.MODID + ":VECTOR";

    private final Map<String, GfxOp> operations;
    private final List<String> orderedOps;
    private final List<GfxOp> sortedOperations;

    public ModuleDataVectorArt(Map<String, GfxOp> operations, List<String> orderedOps) {
        this.operations = operations;
        this.orderedOps = orderedOps;
        this.sortedOperations = null;
    }

    public ModuleDataVectorArt(List<GfxOp> sortedOperations) {
        this.operations = null;
        this.orderedOps = null;
        this.sortedOperations = sortedOperations;
    }

    public Map<String, GfxOp> getOperations() {
        return operations;
    }

    public List<String> getOrderedOps() {
        return orderedOps;
    }

    public List<GfxOp> getSortedOperations() {
        return sortedOperations;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        if (operations == null) {
            buf.writeInt(0);
            return;
        }
        buf.writeInt(orderedOps.size());
        for (String id : orderedOps) {
            operations.get(id).writeToBuf(buf);
        }

//        for (Map.Entry<String, GfxOp> entry : operations.entrySet()) {
//            NetworkTools.writeString(buf, entry.getKey());
//            entry.getValue().writeToBuf(buf);
//        }
    }
}

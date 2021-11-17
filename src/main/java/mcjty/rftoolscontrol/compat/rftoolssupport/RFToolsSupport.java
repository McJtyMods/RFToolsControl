package mcjty.rftoolscontrol.compat.rftoolssupport;

import mcjty.rftoolsbase.api.screens.IScreenModuleRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(ModuleDataVariable.ID, buf -> new ModuleDataVariable(ParameterTools.readFromBuf(buf)));
            manager.registerModuleDataFactory(ModuleDataLog.ID, buf -> new ModuleDataLog(readLog(buf)));
            manager.registerModuleDataFactory(ModuleDataVectorArt.ID, buf -> new ModuleDataVectorArt(readGfxOp(buf)));
            return null;
        }
    }

    @Nullable
    private static List<GfxOp> readGfxOp(PacketBuffer buf) {
        int size = buf.readInt();
        if (size == 0) {
            return null;
        }
        List<GfxOp> operations = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            GfxOp op = GfxOp.readFromBuf(buf);
            operations.add(op);
        }

        return operations;
    }

    @Nullable
    private static List<String> readLog(PacketBuffer buf) {
        int size = buf.readInt();
        if (size == 0) {
            return null;
        }
        List<String> rc = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            rc.add(buf.readUtf(32767));
        }
        return rc;
    }

}

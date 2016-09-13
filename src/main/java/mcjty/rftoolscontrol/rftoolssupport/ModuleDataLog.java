package mcjty.rftoolscontrol.rftoolssupport;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.logic.Parameter;

import java.util.List;

public class ModuleDataLog implements mcjty.rftools.api.screens.data.IModuleData {

    public static final String ID = RFToolsControl.MODID + ":LOG";

    private final List<String> log;

    public ModuleDataLog(List<String> log) {
        this.log = log;
    }

    public List<String> getLog() {
        return log;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        if (log == null) {
            buf.writeInt(0);
            return;
        }
        buf.writeInt(log.size());
        for (String s : log) {
            NetworkTools.writeString(buf, s);
        }
    }
}

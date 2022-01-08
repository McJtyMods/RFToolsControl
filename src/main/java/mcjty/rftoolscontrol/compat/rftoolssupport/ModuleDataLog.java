package mcjty.rftoolscontrol.compat.rftoolssupport;

import mcjty.rftoolsbase.api.screens.data.IModuleData;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public class ModuleDataLog implements IModuleData {

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
    public void writeToBuf(FriendlyByteBuf buf) {
        if (log == null) {
            buf.writeInt(0);
            return;
        }
        buf.writeInt(log.size());
        for (String s : log) {
            buf.writeUtf(s);
        }
    }
}

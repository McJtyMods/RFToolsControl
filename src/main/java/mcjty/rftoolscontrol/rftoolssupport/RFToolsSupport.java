package mcjty.rftoolscontrol.rftoolssupport;

import com.google.common.base.Function;
import mcjty.rftools.api.screens.IScreenModuleRegistry;
import mcjty.rftoolscontrol.logic.Parameter;

import javax.annotation.Nullable;

public class RFToolsSupport {

    public static class GetScreenModuleRegistry implements Function<IScreenModuleRegistry, Void> {
        @Nullable
        @Override
        public Void apply(IScreenModuleRegistry manager) {
            manager.registerModuleDataFactory(ModuleDataVariable.ID, buf -> {
                return new ModuleDataVariable(Parameter.readFromBuf(buf));
            });
            return null;
        }
    }

}

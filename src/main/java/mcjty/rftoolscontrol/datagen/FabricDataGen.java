package mcjty.rftoolscontrol.datagen;

import java.util.Map;
import mcjty.lib.datagen.DataGen;
import mcjty.rftoolsbase.tools.ClientItemDefinitions;
import mcjty.rftoolscontrol.RFToolsControl;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class FabricDataGen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        pack.addProvider((output, lookup) -> {
            var data = new DataGen(output, lookup);
            RFToolsControl.modules.datagen(data, lookup);
            data.generate();
            return data;
        });
        pack.addProvider((output, lookup) -> new ClientItemDefinitions(output, RFToolsControl.MODID, Map.of()));
    }
}

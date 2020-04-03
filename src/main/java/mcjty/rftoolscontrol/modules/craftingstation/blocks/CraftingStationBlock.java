package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class CraftingStationBlock extends BaseBlock {

    public CraftingStationBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsControlTOPDriver.DRIVER)
                .info(key("message.rftoolscontrol.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(CraftingStationTileEntity::new));
    }
}

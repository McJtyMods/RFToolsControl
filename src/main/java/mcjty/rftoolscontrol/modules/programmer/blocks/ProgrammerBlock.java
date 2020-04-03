package mcjty.rftoolscontrol.modules.programmer.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ProgrammerBlock extends BaseBlock {

    public ProgrammerBlock() {
        super(new BlockBuilder()
                .topDriver(RFToolsControlTOPDriver.DRIVER)
                .info(key("message.rftoolscontrol.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(ProgrammerTileEntity::new));
        // @todo 1.15
//        setNeedsRedstoneCheck(true);
    }
}

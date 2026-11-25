package mcjty.rftoolscontrol.modules.programmer.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.compat.RFToolsControlTOPDriver;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ProgrammerBlock extends BaseBlock {

    public ProgrammerBlock(Properties properties) {
        super(new BlockBuilder()
                .properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .topDriver(RFToolsControlTOPDriver.DRIVER)
                .manualEntry(ManualHelper.create("rftoolsbase:programmer/programmer_intro"))
                .info(key("message.rftoolscontrol.shiftmessage"))
                .infoShift(header())
                .tileEntitySupplier(ProgrammerTileEntity::new));
    }
}

package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class CPUCoreItem extends Item implements ITooltipSettings {

    private final int tier;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    parameter("speed", stack -> Integer.toString(Config.coreSpeed[getTier()].get())),
                    parameter("power", stack -> Integer.toString(Config.coreRFPerTick[getTier()].get())));

    public CPUCoreItem(int tier) {
        super(RFToolsControl.setup.defaultProperties().stacksTo(1));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, list, flag);
    }

    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolscontrol:processor/expansions");
    }

}

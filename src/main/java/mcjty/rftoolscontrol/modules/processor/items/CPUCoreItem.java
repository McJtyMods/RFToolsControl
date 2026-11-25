package mcjty.rftoolscontrol.modules.processor.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class CPUCoreItem extends Item implements ITooltipSettings {

    private final int tier;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    parameter("speed", stack -> Integer.toString(Config.coreSpeed[getTier()].get())),
                    parameter("power", stack -> Integer.toString(Config.coreRFPerTick[getTier()].get())));

    public CPUCoreItem(Properties properties, int tier) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, display, consumer, flag);
        List<Component> list = new ArrayList<>();
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, list, flag);
        list.forEach(consumer);
    }

    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolscontrol:processor/expansions");
    }

}

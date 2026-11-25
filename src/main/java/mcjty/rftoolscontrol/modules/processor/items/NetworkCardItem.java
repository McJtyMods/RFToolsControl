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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import static mcjty.lib.builder.TooltipBuilder.*;

public class NetworkCardItem extends Item implements ITooltipSettings {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(),
                    general("range", ChatFormatting.GREEN),
                    general("extra", stack -> getTier() == TIER_ADVANCED, ChatFormatting.GREEN));

    public NetworkCardItem(Properties properties, int tier) {
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

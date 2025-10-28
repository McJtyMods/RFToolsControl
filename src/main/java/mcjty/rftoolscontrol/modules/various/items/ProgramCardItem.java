package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class ProgramCardItem extends Item implements ITooltipSettings {

    public static final ManualEntry MANUAL = ManualHelper.create("rftoolscontrol:various/program_card");

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), parameter("name", stack -> getCardName(stack)));

    public ProgramCardItem() {
        super(RFToolsControl.setup.defaultProperties().stacksTo(1));
    }

    @Override
    public ManualEntry getManualEntry() {
        return MANUAL;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, list, flag);
    }

    public static String getCardName(ItemStack stack) {
        String name = stack.get(VariousModule.PROGRAM_CARD_NAME.get());
        return name == null ? "" : name;
    }

    public static void setCardName(ItemStack stack, String name) {
        if (name == null) {
            // Clear the name component if null
            stack.remove(VariousModule.PROGRAM_CARD_NAME.get());
        } else {
            stack.set(VariousModule.PROGRAM_CARD_NAME.get(), name);
        }
    }

}

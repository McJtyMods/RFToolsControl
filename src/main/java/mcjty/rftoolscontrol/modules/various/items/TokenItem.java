package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.data.TokenData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class TokenItem extends Item implements ITooltipSettings {

    public static final ManualEntry MANUAL = ManualHelper.create("rftoolsbase:various/token");

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(key("message.rftoolscontrol.shiftmessage"))
            .infoShift(header(),
                    gold(this::isEmpty),
                    parameter("type", stack -> !isEmpty(stack), this::getParameterType),
                    parameter("value", stack -> !isEmpty(stack), this::getParameterValue));

    @Override
    public ManualEntry getManualEntry() {
        return MANUAL;
    }

    private boolean isEmpty(ItemStack stack) {
        TokenData data = stack.get(VariousModule.TOKEN_DATA);
        return data == null || data.parameter() == null || !data.parameter().isSet();
    }

    private String getParameterType(ItemStack stack) {
        TokenData data = stack.get(VariousModule.TOKEN_DATA);
        if (data != null && data.parameter() != null) {
            Parameter par = data.parameter();
            if (par != null && par.getParameterType() != null) {
                return par.getParameterType().getName();
            }
        }
        return "<unknown>";
    }

    private String getParameterValue(ItemStack stack) {
        TokenData data = stack.get(VariousModule.TOKEN_DATA);
        if (data != null && data.parameter() != null) {
            Parameter par = data.parameter();
            if (par != null && par.getParameterType() != null && par.getParameterValue() != null) {
                return ParameterTypeTools.stringRepresentation(par.getParameterType(), par.getParameterValue());
            }
        }
        return "<unknown>";
    }

    public TokenItem() {
        super(RFToolsControl.setup.defaultProperties().stacksTo(64));
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack stack, TooltipContext context, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, context, list, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, list, flag);
    }
}

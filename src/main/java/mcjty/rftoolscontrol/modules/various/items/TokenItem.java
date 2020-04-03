package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class TokenItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(key("message.rftoolscontrol.shiftmessage"))
            .infoShift(header(),
                    gold(this::isEmpty),
                    parameter("type", stack -> !isEmpty(stack), this::getParameterType),
                    parameter("value", stack -> !isEmpty(stack), this::getParameterValue));

    private boolean isEmpty(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundNBT parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getParameterType(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundNBT parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                return par.getParameterType().getName();
            }
        }
        return "<unknown>";
    }

    private String getParameterValue(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundNBT parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                return ParameterTypeTools.stringRepresentation(par.getParameterType(), par.getParameterValue());
            }
        }
        return "<unknown>";
    }

    public TokenItem() {
        super(new Properties()
                .maxStackSize(64)
                .group(RFToolsControl.setup.getTab()));
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, list, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, list, flag);
    }
}

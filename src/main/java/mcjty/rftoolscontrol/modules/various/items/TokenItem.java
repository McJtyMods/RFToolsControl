package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTypeTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class TokenItem extends Item implements ITooltipSettings {

    public static final ManualEntry MANUAL = ManualHelper.create("rftoolscontrol:various/token");

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
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
        if (stack.hasTag()) {
            CompoundTag parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getParameterType(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                return par.getParameterType().getName();
            }
        }
        return "<unknown>";
    }

    private String getParameterValue(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                return ParameterTypeTools.stringRepresentation(par.getParameterType(), par.getParameterValue());
            }
        }
        return "<unknown>";
    }

    public TokenItem() {
        super(new Properties()
                .stacksTo(64)
                .tab(RFToolsControl.setup.getTab()));
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), stack, list, flag);
    }
}

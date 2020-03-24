package mcjty.rftoolscontrol.items;

import mcjty.lib.McJtyLib;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.logic.ParameterTools;
import mcjty.rftoolscontrol.logic.ParameterTypeTools;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class TokenItem extends Item {

    public TokenItem() {
        super(new Properties()
                .maxStackSize(64)
                .group(RFToolsControl.setup.getTab()));

//        super((Properties) "token");
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);

        boolean hasContents = false;
        if (stack.hasTag()) {
            CompoundNBT parameter = stack.getTag().getCompound("parameter");
            if (!parameter.isEmpty()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                hasContents = true;
                list.add(new StringTextComponent(TextFormatting.BLUE + "Type: " + par.getParameterType().getName()));
                list.add(new StringTextComponent(TextFormatting.BLUE + "Value: " + ParameterTypeTools.stringRepresentation(par.getParameterType(), par.getParameterValue())));
            }
        }
        if (!hasContents) {
            list.add(new StringTextComponent(TextFormatting.BLUE + "This token is empty"));
        }

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This item is a simple token. It does"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "not do anything but it can store"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "information"));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + RFToolsControl.SHIFT_MESSAGE));
        }
    }
}

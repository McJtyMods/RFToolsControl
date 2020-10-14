package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.setup.GuiProxy;
import mcjty.rftoolscontrol.logic.ParameterTools;
import mcjty.rftoolscontrol.logic.ParameterTypeTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class TokenItem extends GenericRFToolsItem {

    public TokenItem() {
        super("token");
        setMaxStackSize(64);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);

        boolean hasContents = false;
        if (stack.hasTagCompound()) {
            NBTTagCompound parameter = stack.getTagCompound().getCompoundTag("parameter");
            if (!parameter.hasNoTags()) {
                Parameter par = ParameterTools.readFromNBT(parameter);
                hasContents = true;
                list.add(TextFormatting.BLUE + "Type: " + par.getParameterType().getName());
                list.add(TextFormatting.BLUE + "Value: " + ParameterTypeTools.stringRepresentation(par.getParameterType(), par.getParameterValue()));
            }
        }
        if (!hasContents) {
            list.add(TextFormatting.BLUE + I18n.format("tooltips." + RFToolsControl.MODID+"."+"token.hascontents"));
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            String[] strings = I18n.format("tooltips." + RFToolsControl.MODID+"."+"token.keyboard").split("0x0a");
            for (String str : strings){
                list.add(TextFormatting.WHITE + str);
            }
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }
}

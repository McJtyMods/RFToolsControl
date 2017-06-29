package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ProgramCardItem extends GenericRFToolsItem {

    public ProgramCardItem() {
        super("program_card");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this item in the programmer");
        list.add("to write your program and then");
        list.add("insert it in the processor to run");
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.GREEN + "Name: " + tagCompound.getString("name"));
        }
    }

    public static String getCardName(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null) {
            return tagCompound.getString("name");
        } else {
            return "";
        }
    }

    public static void setCardName(ItemStack stack, String name) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
        }
        tagCompound.setString("name", name);
    }

}

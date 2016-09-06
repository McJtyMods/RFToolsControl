package mcjty.rftoolscontrol.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Use this item in the programmer");
        list.add("to write your program and then");
        list.add("insert it in the processor to run");
    }

}

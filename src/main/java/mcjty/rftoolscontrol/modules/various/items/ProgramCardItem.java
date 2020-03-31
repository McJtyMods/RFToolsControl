package mcjty.rftoolscontrol.modules.various.items;

import mcjty.rftoolscontrol.RFToolsControl;
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

public class ProgramCardItem extends Item {

    public ProgramCardItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(RFToolsControl.setup.getTab()));
//        super((Properties) "program_card");
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("Use this item in the programmer"));
        list.add(new StringTextComponent("to write your program and then"));
        list.add(new StringTextComponent("insert it in the processor to run"));
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.GREEN + "Name: " + tagCompound.getString("name")));
        }
    }

    public static String getCardName(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            return tagCompound.getString("name");
        } else {
            return "";
        }
    }

    public static void setCardName(ItemStack stack, String name) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        tagCompound.putString("name", name);
    }

}

package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftingCardItem extends GenericRFToolsItem {

    public CraftingCardItem() {
        super("crafting_card");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This item can be used for auto");
        list.add("crafting. It stores ingredients");
        list.add("and end result for a recipe");
        ItemStack result = getResult(stack);
        if (result != null) {
            if (result.stackSize > 1) {
                list.add(TextFormatting.BLUE + "Item: " + TextFormatting.WHITE + result.getDisplayName() + "(" +
                        result.stackSize + ")");
            } else {
                list.add(TextFormatting.BLUE + "Item: " + TextFormatting.WHITE + result.getDisplayName());
            }
        }
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            player.openGui(RFToolsControl.instance, RFToolsControl.GUI_CRAFTINGCARD, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static ItemStack getResult(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(CraftingCardContainer.SLOT_OUT);
        return ItemStack.loadItemStackFromNBT(nbtTagCompound);
    }

    public static List<ItemStack> getIngredients(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return Collections.emptyList();
        }
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            if (i < CraftingCardContainer.INPUT_SLOTS) {
                NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                if (s != null && s.stackSize > 0) {
                    stacks.add(s);
                }
            }
        }
        return stacks;
    }
}

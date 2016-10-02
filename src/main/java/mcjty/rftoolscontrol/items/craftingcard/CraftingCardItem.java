package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
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

import static mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer.GRID_WIDTH;
import static mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer.INPUT_SLOTS;

public class CraftingCardItem extends GenericRFToolsItem {

    public CraftingCardItem() {
        super("crafting_card");
    }

    public static void testRecipe(World world, ItemStack craftingCard) {
        ItemStack[] stacks = getStacksFromItem(craftingCard);

        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);
        for (int y = 0 ; y < 3 ; y++) {
            for (int x = 0 ; x < 3 ; x++) {
                int idx = y*3+x;
                int idxCard = y*GRID_WIDTH + x;
                workInventory.setInventorySlotContents(idx, stacks[idxCard]);
            }
        }
        ItemStack stack = CraftingManager.getInstance().findMatchingRecipe(workInventory, world);
        stacks[INPUT_SLOTS] = stack;
        putStacksInItem(craftingCard, stacks);
    }

    public static ItemStack[] getStacksFromItem(ItemStack craftingCard) {
        NBTTagCompound tagCompound = craftingCard.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            craftingCard.setTagCompound(tagCompound);
        }
        ItemStack[] stacks = new ItemStack[CraftingCardContainer.INPUT_SLOTS+1];
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
        return stacks;
    }

    public static void putStacksInItem(ItemStack craftingCard, ItemStack[] stacks) {
        NBTTagCompound tagCompound = craftingCard.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            craftingCard.setTagCompound(tagCompound);
        }
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : stacks) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
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
        if (hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
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

    private static boolean isInGrid(int index) {
        int x = index % 5;
        int y = index / 5;
        return x <= 2 && y <= 2;
    }

    // Return true if this crafting card fits a 3x3 crafting grid nicely
    public static boolean fitsGrid(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return false;
        }
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            if (i < CraftingCardContainer.INPUT_SLOTS) {
                NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = ItemStack.loadItemStackFromNBT(nbtTagCompound);
                if (s != null && s.stackSize > 0) {
                    if (!isInGrid(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<ItemStack> getIngredientsGrid(ItemStack card) {
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
                if (isInGrid(i)) {
                    stacks.add(s);
                }
            }
        }
        return stacks;
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

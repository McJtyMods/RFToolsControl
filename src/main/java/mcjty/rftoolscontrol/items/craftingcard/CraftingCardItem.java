package mcjty.rftoolscontrol.items.craftingcard;

import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.items.GenericRFToolsItem;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;

import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

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
        ItemStackList stacks = getStacksFromItem(craftingCard);

        InventoryCrafting workInventory = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(PlayerEntity var1) {
                return false;
            }
        }, 3, 3);
        for (int y = 0 ; y < 3 ; y++) {
            for (int x = 0 ; x < 3 ; x++) {
                int idx = y*3+x;
                int idxCard = y*GRID_WIDTH + x;
                workInventory.setInventorySlotContents(idx, stacks.get(idxCard));
            }
        }
        IRecipe recipe = CraftingManager.findMatchingRecipe(workInventory, world);
        if (recipe != null) {
            ItemStack stack = recipe.getCraftingResult(workInventory);
            stacks.set(INPUT_SLOTS, stack);
        } else {
            stacks.set(INPUT_SLOTS, ItemStack.EMPTY);
        }
        putStacksInItem(craftingCard, stacks);
    }

    public static ItemStackList getStacksFromItem(ItemStack craftingCard) {
        CompoundNBT tagCompound = craftingCard.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
            craftingCard.setTagCompound(tagCompound);
        }
        ItemStackList stacks = ItemStackList.create(CraftingCardContainer.INPUT_SLOTS+1);
        ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            CompoundNBT nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks.set(i, new ItemStack(nbtTagCompound));
        }
        return stacks;
    }

    public static void putStacksInItem(ItemStack craftingCard, ItemStackList stacks) {
        CompoundNBT tagCompound = craftingCard.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
            craftingCard.setTagCompound(tagCompound);
        }
        ListNBT bufferTagList = new ListNBT();
        for (ItemStack stack : stacks) {
            CompoundNBT nbtTagCompound = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This item can be used for auto");
        list.add("crafting. It stores ingredients");
        list.add("and end result for a recipe");
        boolean strictnbt = CraftingCardItem.isStrictNBT(stack);
        list.add(TextFormatting.GREEN + "Strict NBT: " + TextFormatting.WHITE + (strictnbt ? "yes" : "no"));
        ItemStack result = getResult(stack);
        if (!result.isEmpty()) {
            if (result.getCount() > 1) {
                list.add(TextFormatting.BLUE + "Item: " + TextFormatting.WHITE + result.getDisplayName() + "(" +
                        result.getCount() + ")");
            } else {
                list.add(TextFormatting.BLUE + "Item: " + TextFormatting.WHITE + result.getDisplayName());
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand != Hand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!world.isRemote) {
            player.openGui(RFToolsControl.instance, GuiProxy.GUI_CRAFTINGCARD, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static ItemStack getResult(ItemStack card) {
        CompoundNBT tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return ItemStack.EMPTY;
        }
        ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        CompoundNBT nbtTagCompound = bufferTagList.getCompoundTagAt(CraftingCardContainer.SLOT_OUT);
        return new ItemStack(nbtTagCompound);
    }

    private static boolean isInGrid(int index) {
        int x = index % 5;
        int y = index / 5;
        return x <= 2 && y <= 2;
    }

    // Return true if this crafting card fits a 3x3 crafting grid nicely
    public static boolean fitsGrid(ItemStack card) {
        CompoundNBT tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return false;
        }
        ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            if (i < CraftingCardContainer.INPUT_SLOTS) {
                CompoundNBT nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(nbtTagCompound);
                if (!s.isEmpty()) {
                    if (!isInGrid(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isStrictNBT(ItemStack card) {
        CompoundNBT tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return false;
        }
        return tagCompound.getBoolean("strictnbt");
    }

    public static List<ItemStack> getIngredientsGrid(ItemStack card) {
        CompoundNBT tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return Collections.emptyList();
        }
        ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            if (i < CraftingCardContainer.INPUT_SLOTS) {
                CompoundNBT nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(nbtTagCompound);
                if (isInGrid(i)) {
                    stacks.add(s);
                }
            }
        }
        return stacks;
    }

    public static List<ItemStack> getIngredients(ItemStack card) {
        CompoundNBT tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return Collections.emptyList();
        }
        ListNBT bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            if (i < CraftingCardContainer.INPUT_SLOTS) {
                CompoundNBT nbtTagCompound = bufferTagList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(nbtTagCompound);
                if (!s.isEmpty()) {
                    stacks.add(s);
                }
            }
        }
        return stacks;
    }
}

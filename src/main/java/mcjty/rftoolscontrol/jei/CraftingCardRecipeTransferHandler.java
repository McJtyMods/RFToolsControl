package mcjty.rftoolscontrol.jei;

import mcjty.lib.jei.CompatRecipeTransferHandler;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CraftingCardRecipeTransferHandler implements CompatRecipeTransferHandler {

    @Override
    public Class<? extends Container> getContainerClass() {
        return CraftingCardContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull Container container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsControlJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}

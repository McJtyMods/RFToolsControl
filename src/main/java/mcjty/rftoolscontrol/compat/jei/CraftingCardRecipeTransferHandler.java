package mcjty.rftoolscontrol.compat.jei;

import mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CraftingCardRecipeTransferHandler implements IRecipeTransferHandler<CraftingCardContainer> {

    @Override
    public Class<CraftingCardContainer> getContainerClass() {
        return CraftingCardContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull CraftingCardContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients();

        if (doTransfer) {
            RFToolsControlJeiPlugin.transferRecipe(guiIngredients, null);
        }

        return null;
    }

}

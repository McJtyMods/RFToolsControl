package mcjty.rftoolscontrol.jei;

import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchContainer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JEIPlugin
public class RFToolsControlJeiPlugin extends BlankModPlugin {

    public static void transferRecipe(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>(10);
        for (int i = 0 ; i < 10 ; i++) {
            items.add(null);
        }
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : guiIngredients.entrySet()) {
            int recipeSlot = entry.getKey();
            List<ItemStack> allIngredients = entry.getValue().getAllIngredients();
            if (!allIngredients.isEmpty()) {
                items.set(recipeSlot, allIngredients.get(0));
            }
        }

        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketSendRecipe(items));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
        transferRegistry.addRecipeTransferHandler(new CraftingCardRecipeTransferHandler());

        transferRegistry.addRecipeTransferHandler(WorkbenchContainer.class, VanillaRecipeCategoryUid.CRAFTING, WorkbenchContainer.SLOT_CRAFTINPUT, 9, WorkbenchContainer.SLOT_BUFFER, WorkbenchContainer.BUFFER_SIZE + 9*4);
        registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.workbenchBlock), VanillaRecipeCategoryUid.CRAFTING);
    }
}

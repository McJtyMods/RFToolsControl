package mcjty.rftoolscontrol.compat.jei;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.workbench.WorkbenchContainer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import mcjty.rftoolscontrol.setup.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class RFToolsControlJeiPlugin implements IModPlugin {

    public static void transferRecipe(Map<Integer, ? extends IGuiIngredient<ItemStack>> guiIngredients, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>(10);
        for (int i = 0 ; i < 10 ; i++) {
            items.add(ItemStack.EMPTY);
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
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFToolsControl.MODID, "rftoolscontrol");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        CraftingCardRecipeTransferHandler.register(registration);
        registration.addRecipeTransferHandler(WorkbenchContainer.class, VanillaRecipeCategoryUid.CRAFTING, WorkbenchContainer.SLOT_CRAFTINPUT, 9, WorkbenchContainer.SLOT_BUFFER, WorkbenchContainer.BUFFER_SIZE + 9*4);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Registration.WORKBENCH.get()), VanillaRecipeCategoryUid.CRAFTING);
    }
}

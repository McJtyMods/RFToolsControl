package mcjty.rftoolscontrol.compat.jei;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class RFToolsControlJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFToolsControl.MODID, "rftoolscontrol");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(WorkbenchContainer.class, VanillaRecipeCategoryUid.CRAFTING, WorkbenchContainer.SLOT_CRAFTINPUT, 9, WorkbenchContainer.SLOT_BUFFER, WorkbenchContainer.BUFFER_SIZE + 9*4);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(VariousSetup.WORKBENCH.get()), VanillaRecipeCategoryUid.CRAFTING);
    }
}

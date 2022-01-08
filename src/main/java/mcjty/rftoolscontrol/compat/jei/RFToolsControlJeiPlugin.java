package mcjty.rftoolscontrol.compat.jei;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchContainer;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

@JeiPlugin
public class RFToolsControlJeiPlugin implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(RFToolsControl.MODID, "rftoolscontrol");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(WorkbenchContainer.class, VanillaRecipeCategoryUid.CRAFTING, WorkbenchTileEntity.SLOT_CRAFTINPUT, 9, WorkbenchTileEntity.SLOT_BUFFER, WorkbenchTileEntity.BUFFER_SIZE + 9*4);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(VariousModule.WORKBENCH.get()), VanillaRecipeCategoryUid.CRAFTING);
    }
}

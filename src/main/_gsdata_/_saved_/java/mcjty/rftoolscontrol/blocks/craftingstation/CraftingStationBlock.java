package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class CraftingStationBlock extends GenericRFToolsBlock<CraftingStationTileEntity, CraftingStationContainer> {

    public CraftingStationBlock() {
        super(Material.IRON, CraftingStationTileEntity.class, CraftingStationContainer::new, "craftingstation", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<CraftingStationTileEntity, CraftingStationContainer, GenericGuiContainer<? super CraftingStationTileEntity>> getGuiFactory() {
        return GuiCraftingStation::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_CRAFTINGSTATION;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID + "." + "craftingstation").split("0x0a")));
    }
}

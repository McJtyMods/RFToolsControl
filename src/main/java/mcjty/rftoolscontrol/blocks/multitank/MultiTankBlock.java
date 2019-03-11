package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.rftoolscontrol.proxy.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiFunction;

public class MultiTankBlock extends GenericRFToolsBlock<MultiTankTileEntity, EmptyContainer> {

    public MultiTankBlock() {
        super(Material.IRON, MultiTankTileEntity.class, EmptyContainer::new, "tank", false);
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BiFunction<MultiTankTileEntity, EmptyContainer, GenericGuiContainer<? super MultiTankTileEntity>> getGuiFactory() {
        return GuiMultiTank::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_TANK;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This is a tank that has capacity");
        list.add("for four types of liquids");
        list.add("This block is meant for the processor");
        list.add("and cannot otherwise be used directly");
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}

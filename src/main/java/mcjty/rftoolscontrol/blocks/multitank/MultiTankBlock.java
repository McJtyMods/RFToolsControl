package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.blocks.RotationType;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.setup.GuiProxy;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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


    @Override
    public BiFunction<MultiTankTileEntity, EmptyContainer, GenericGuiContainer<? super MultiTankTileEntity>> getGuiFactory() {
        return GuiMultiTank::new;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_TANK;
    }


    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This is a tank that has capacity");
        list.add("for four types of liquids");
        list.add("This block is meant for the processor");
        list.add("and cannot otherwise be used directly");
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }
}

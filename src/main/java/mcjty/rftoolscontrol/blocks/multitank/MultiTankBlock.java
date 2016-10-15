package mcjty.rftoolscontrol.blocks.multitank;

import mcjty.lib.container.EmptyContainer;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class MultiTankBlock extends GenericRFToolsBlock<MultiTankTileEntity, EmptyContainer> {

    public MultiTankBlock() {
        super(Material.IRON, MultiTankTileEntity.class, EmptyContainer.class, "tank", false);
    }

    @Override
    public boolean hasNoRotation() {
        return true;
    }

    //    @SideOnly(Side.CLIENT)
//    @Override
//    public Class<? extends GenericGuiContainer> getGuiClass() {
//        return GuiNode.class;
//    }

    @Override
    public int getGuiID() {
        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This is a tank that has capacity");
        list.add("for four types of liquids");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
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

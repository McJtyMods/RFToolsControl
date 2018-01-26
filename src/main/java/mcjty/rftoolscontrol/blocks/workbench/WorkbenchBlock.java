package mcjty.rftoolscontrol.blocks.workbench;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.GenericRFToolsBlock;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WorkbenchBlock extends GenericRFToolsBlock<WorkbenchTileEntity, WorkbenchContainer> {

    public WorkbenchBlock() {
        super(Material.IRON, WorkbenchTileEntity.class, WorkbenchContainer.class, "workbench", false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<GuiWorkbench> getGuiClass() {
        return GuiWorkbench.class;
    }

    @Override
    public int getGuiID() {
        return RFToolsControl.GUI_WORKBENCH;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("A general workbench that works well");
        list.add("with a processor but can also be");
        list.add("used standalone");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
    }
}

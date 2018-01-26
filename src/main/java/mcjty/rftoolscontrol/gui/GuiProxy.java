package mcjty.rftoolscontrol.gui;

import mcjty.lib.container.GenericBlock;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardContainer;
import mcjty.rftoolscontrol.items.craftingcard.GuiCraftingCard;
import mcjty.rftoolscontrol.items.manual.GuiRFToolsManual;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFToolsControl.GUI_MANUAL_CONTROL) {
            return null;
        } else if (guiid == RFToolsControl.GUI_CRAFTINGCARD) {
            return new CraftingCardContainer(entityPlayer);
        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createServerContainer(entityPlayer, te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiid, EntityPlayer entityPlayer, World world, int x, int y, int z) {
        if (guiid == RFToolsControl.GUI_MANUAL_CONTROL) {
            return new GuiRFToolsManual(GuiRFToolsManual.MANUAL_CONTROL);
        } else if (guiid == RFToolsControl.GUI_CRAFTINGCARD) {
            return new GuiCraftingCard(new CraftingCardContainer(entityPlayer));
        }

        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericBlock) {
            GenericBlock<?, ?> genericBlock = (GenericBlock<?, ?>) block;
            TileEntity te = world.getTileEntity(pos);
            return genericBlock.createClientGui(entityPlayer, te);
        }
        return null;
    }
}

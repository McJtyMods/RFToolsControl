package mcjty.rftoolscontrol.blocks;

import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericItemBlock;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class GenericRFToolsBlock<T extends GenericTileEntity, C extends Container> extends GenericBlock<T, C> {

    public GenericRFToolsBlock(Material material,
                               Class<? extends T> tileEntityClass,
                               Class<? extends C> containerClass,
                               String name, boolean isContainer) {
        super(RFToolsControl.instance, material, tileEntityClass, containerClass, GenericItemBlock.class, name, isContainer);
        setCreativeTab(RFToolsControl.tabRFToolsControl);
    }

    public GenericRFToolsBlock(Material material,
                               Class<? extends T> tileEntityClass,
                               Class<? extends C> containerClass,
                               Class<? extends ItemBlock> itemBlockClass,
                               String name, boolean isContainer) {
        super(RFToolsControl.instance, material, tileEntityClass, containerClass, itemBlockClass, name, isContainer);
        setCreativeTab(RFToolsControl.tabRFToolsControl);
    }

    @Override
    protected boolean checkAccess(World world, EntityPlayer player, TileEntity te) {
        // @todo
        return false;
    }


}

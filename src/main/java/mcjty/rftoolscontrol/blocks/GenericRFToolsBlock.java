package mcjty.rftoolscontrol.blocks;

import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class GenericRFToolsBlock<T extends GenericTileEntity, C extends Container> extends GenericBlock<T, C> {

    public GenericRFToolsBlock(Material material, Class<? extends T> tileEntityClass, BiFunction<EntityPlayer, IInventory, C> containerFactory, String name, boolean isContainer) {
        super(RFToolsControl.instance, material, tileEntityClass, containerFactory, name, isContainer);
        setCreativeTab(RFToolsControl.setup.getTab());
    }

    public GenericRFToolsBlock(Material material, Class<? extends T> tileEntityClass, BiFunction<EntityPlayer, IInventory, C> containerFactory,
                               Function<Block, ItemBlock> itemBlockFunction, String name, boolean isContainer) {
        super(RFToolsControl.instance, material, tileEntityClass, containerFactory, itemBlockFunction, name, isContainer);
        setCreativeTab(RFToolsControl.setup.getTab());
    }

    @Override
    protected boolean checkAccess(World world, EntityPlayer player, TileEntity te) {
        // @todo
        return false;
    }


}

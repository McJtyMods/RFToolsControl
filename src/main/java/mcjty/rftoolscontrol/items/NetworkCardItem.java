package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.config.GeneralConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NetworkCardItem extends GenericRFToolsItem {

    public NetworkCardItem() {
        super("network_card");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> list, boolean advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("Insert this item in the processor");
        list.add("to allow access to nearby nodes");
        list.add("Use 'net setup <name>' in Processor");
        list.add("console to setup the network");
    }
}

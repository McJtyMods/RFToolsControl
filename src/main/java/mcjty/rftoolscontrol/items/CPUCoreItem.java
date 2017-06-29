package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.config.GeneralConfiguration;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class CPUCoreItem extends GenericRFToolsItem {

    private final int tier;

    public CPUCoreItem(String name, int tier) {
        super(name);
        setMaxStackSize(1);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World playerIn, List<String> list, ITooltipFlag advanced) {
        super.addInformation(stack, playerIn, list, advanced);
        list.add("This CPU core can be used in the");
        list.add("processor to allow it to run programs");
        list.add(TextFormatting.GREEN + "" + GeneralConfiguration.coreSpeed[tier] + " operations per tick");
        list.add(TextFormatting.GREEN + "" + GeneralConfiguration.coreRFPerTick[tier] + " RF per tick");
    }
}

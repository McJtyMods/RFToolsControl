package mcjty.rftoolscontrol.items;

import mcjty.rftoolscontrol.config.ConfigSetup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;

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
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID+"."+"cpu_core_500").split("0x0a")));

        list.add(TextFormatting.GREEN + "" + ConfigSetup.coreSpeed[tier].get() + " operations per tick");
        list.add(TextFormatting.GREEN + "" + ConfigSetup.coreRFPerTick[tier].get() + " RF per tick");
    }
}

package mcjty.rftoolscontrol.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class NetworkCardItem extends GenericRFToolsItem {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    public NetworkCardItem(String name, int tier) {
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
        
        list.addAll(Arrays.asList(I18n.format("tooltips." + RFToolsControl.MODID + "." + "network_card").split("0x0a")));
        
        String[] strings = I18n.format("tooltips." + RFToolsControl.MODID + "." + "network_card.range").split("0x0a");
        
        if (tier == TIER_NORMAL) {
            list.add(TextFormatting.GREEN + strings[0]);
        } else {
            list.add(TextFormatting.GREEN + strings[1]);
            list.add(TextFormatting.GREEN + strings[2]);
        }
    }
}

package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

import net.minecraft.item.Item.Properties;

public class NetworkCardItem extends Item implements ITooltipSettings {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(header(),
                    gold(),
                    general("range", TextFormatting.GREEN),
                    general("extra", stack -> getTier() == TIER_ADVANCED, TextFormatting.GREEN));

    public NetworkCardItem(int tier) {
        super(new Properties()
                .stacksTo(1)
                .tab(RFToolsControl.setup.getTab()));
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> list, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), stack, list, flag);
    }
}

package mcjty.rftoolscontrol.modules.processor.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;

public class NetworkCardItem extends Item implements ITooltipSettings {

    private final int tier;

    public static final int TIER_NORMAL = 0;
    public static final int TIER_ADVANCED = 1;

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(header(),
                    gold(),
                    general("range", ChatFormatting.GREEN),
                    general("extra", stack -> getTier() == TIER_ADVANCED, ChatFormatting.GREEN));

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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(Tools.getId(this), stack, list, flag);
    }
}

package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.NBTTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

import net.minecraft.world.item.Item.Properties;

public class ProgramCardItem extends Item implements ITooltipSettings {

    public static final ManualEntry MANUAL = ManualHelper.create("rftoolscontrol:various/program_card");

    private final Lazy<TooltipBuilder> tooltipBuilder = () -> new TooltipBuilder()
            .info(header(), parameter("name", stack -> NBTTools.getString(stack, "name", "<unset>")));

    public ProgramCardItem() {
        super(new Properties()
                .stacksTo(1)
                .tab(RFToolsControl.setup.getTab()));
    }

    @Override
    public ManualEntry getManualEntry() {
        return MANUAL;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> list, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), stack, list, flag);
    }

    public static String getCardName(ItemStack stack) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound != null) {
            return tagCompound.getString("name");
        } else {
            return "";
        }
    }

    public static void setCardName(ItemStack stack, String name) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.putString("name", name);
    }

}

package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.NBTTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

import net.minecraft.item.Item.Properties;

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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> list, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, list, flag);
        tooltipBuilder.get().makeTooltip(getRegistryName(), stack, list, flag);
    }

    public static String getCardName(ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound != null) {
            return tagCompound.getString("name");
        } else {
            return "";
        }
    }

    public static void setCardName(ItemStack stack, String name) {
        CompoundNBT tagCompound = stack.getOrCreateTag();
        tagCompound.putString("name", name);
    }

}

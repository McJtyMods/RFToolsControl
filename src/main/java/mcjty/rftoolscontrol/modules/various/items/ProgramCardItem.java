package mcjty.rftoolscontrol.modules.various.items;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.NBTTools;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class ProgramCardItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), parameter("name", stack -> NBTTools.getString(stack, "name", "<unset>")));

    public ProgramCardItem() {
        super(new Properties()
                .maxStackSize(1)
                .group(RFToolsControl.setup.getTab()));
//        super((Properties) "program_card");
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, list, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, list, flag);
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

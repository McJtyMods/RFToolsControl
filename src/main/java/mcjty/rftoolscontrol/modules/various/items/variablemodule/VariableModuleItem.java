package mcjty.rftoolscontrol.modules.various.items.variablemodule;

import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.setup.ConfigSetup;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class VariableModuleItem extends Item implements IModuleProvider {

    public VariableModuleItem() {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(1)
                .group(RFToolsControl.setup.getTab()));

//        super("variable_module");
    }

    @Override
    public Class<VariableScreenModule> getServerScreenModule() {
        return VariableScreenModule.class;
    }

    @Override
    public Class<VariableClientScreenModule> getClientScreenModule() {
        return VariableClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "VAR";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("Stats:").color("varcolor", "Color for the variable text").nl()
                .label("Var:").integer("varIdx", "Index of the variable").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .block("monitor").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(itemStack, world, list, flagIn);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ConfigSetup.VARIABLEMODULE_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text")));
            if (tagCompound.contains("monitorx")) {
                int monitorx = tagCompound.getInt("monitorx");
                int monitory = tagCompound.getInt("monitory");
                int monitorz = tagCompound.getInt("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")"));
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on a processor to set the"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "target for this module"));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = player.getHeldItem(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }

        if (block == ProcessorSetup.PROCESSOR.get()) {
            tagCompound.putString("monitordim", world.getDimension().getType().getRegistryName().toString());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            if (world.isRemote) {
                Logging.message(player, "Variable module is set to block");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            if (world.isRemote) {
                Logging.message(player, "Variable module is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

}
package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class VectorArtModuleItem extends GenericModuleItem {

    public VectorArtModuleItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(1)
                .tab(RFToolsControl.setup.getTab()));
    }

    @Override
    protected int getUses(ItemStack stack) {
        return Config.VECTORARTMODULE_RFPERTICK.get();
    }

    @Override
    protected boolean hasGoldMessage(ItemStack stack) {
        return !ModuleTools.hasModuleTarget(stack);
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        return ModuleTools.getTargetString(stack);
    }

    @Override
    public Class<VectorArtScreenModule> getServerScreenModule() {
        return VectorArtScreenModule.class;
    }

    @Override
    public Class<VectorArtClientScreenModule> getClientScreenModule() {
        return VectorArtClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "VAR";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .block("monitor").nl();
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }

        if (block == ProcessorModule.PROCESSOR.get()) {
            tagCompound.putString("monitordim", world.dimension().location().toString());
            tagCompound.putInt("monitorx", pos.getX());
            tagCompound.putInt("monitory", pos.getY());
            tagCompound.putInt("monitorz", pos.getZ());
            if (world.isClientSide) {
                Logging.message(player, "Vector art module is set to block");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            if (world.isClientSide) {
                Logging.message(player, "Vector art module is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

}
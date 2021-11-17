package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import mcjty.lib.client.GuiTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.various.ITabletSupport;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConsoleModuleItem extends GenericModuleItem implements ITabletSupport {

    public ConsoleModuleItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(1)
                .tab(RFToolsControl.setup.getTab()));
    }

    @Override
    public Item getInstalledTablet() {
        return VariousModule.TABLET_PROCESSOR.get();
    }

    @Override
    public void openGui(@Nonnull PlayerEntity player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
        BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
        RegistryKey<World> dimensionType = ModuleTools.getDimensionFromModule(containingItem);
        GuiTools.openRemoteGui(player, dimensionType, pos, te -> new INamedContainerProvider() {
            @Override
            @Nonnull
            public ITextComponent getDisplayName() {
                return new StringTextComponent("Remote Processor Console");
            }

            @Nonnull
            @Override
            public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) {
                ProcessorContainer container = ProcessorContainer.createRemote(id, pos, (GenericTileEntity) te);
                te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                    container.setupInventories(h, inventory);
                });
                return container;
            }
        });
    }

    @Override
    protected int getUses(ItemStack stack) {
        return Config.CONSOLEMODULE_RFPERTICK.get();
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
    public Class<ConsoleScreenModule> getServerScreenModule() {
        return ConsoleScreenModule.class;
    }

    @Override
    public Class<ConsoleClientScreenModule> getClientScreenModule() {
        return ConsoleClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "VAR";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder.
                block("monitor").nl();
    }

    @Override
    @Nonnull
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
                Logging.message(player, "Console module is set to block");
            }
        } else {
            tagCompound.remove("monitordim");
            tagCompound.remove("monitorx");
            tagCompound.remove("monitory");
            tagCompound.remove("monitorz");
            if (world.isClientSide) {
                Logging.message(player, "Console module is cleared");
            }
        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }

}
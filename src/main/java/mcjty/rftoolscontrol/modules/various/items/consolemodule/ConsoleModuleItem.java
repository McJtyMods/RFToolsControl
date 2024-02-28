package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import mcjty.lib.client.GuiTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ComponentFactory;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;

import javax.annotation.Nonnull;

public class ConsoleModuleItem extends GenericModuleItem implements ITabletSupport {

    public ConsoleModuleItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    @Override
    public Item getInstalledTablet() {
        return VariousModule.TABLET_PROCESSOR.get();
    }

    @Override
    public void openGui(@Nonnull Player player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
        BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
        ResourceKey<Level> dimensionType = ModuleTools.getDimensionFromModule(containingItem);
        GuiTools.openRemoteGui(player, dimensionType, pos, te -> new MenuProvider() {
            @Override
            @Nonnull
            public Component getDisplayName() {
                return ComponentFactory.literal("Remote Processor Console");
            }

            @Nonnull
            @Override
            public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) {
                ProcessorContainer container = ProcessorContainer.createRemote(id, pos, (GenericTileEntity) te, player);
                te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundTag();
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
        return InteractionResult.SUCCESS;
    }

}
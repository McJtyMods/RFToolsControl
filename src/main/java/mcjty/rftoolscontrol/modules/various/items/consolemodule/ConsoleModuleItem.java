package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import com.mojang.serialization.Codec;
import mcjty.lib.client.GuiTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ComponentFactory;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.various.ITabletSupport;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ConsoleModuleItem extends GenericModuleItem implements ITabletSupport {

    public ConsoleModuleItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return ConsoleScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return ConsoleScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return VariousModule.CONSOLE_MODULE_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return ConsoleScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new ConsoleClientScreenModule();
    }

    @Override
    public Item getInstalledTablet() {
        return VariousModule.TABLET_PROCESSOR.get();
    }

    @Override
    public void openGui(@Nonnull Player player, @Nonnull ItemStack tabletItem, @Nonnull ItemStack containingItem) {
        BlockPos pos = ModuleTools.getPositionFromModule(containingItem);
        var dimensionType = ModuleTools.getDimensionFromModule(containingItem);
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
                IItemHandler handler = te.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, te.getBlockPos(), null);
                if (handler != null) {
                    container.setupInventories(handler, inventory);
                }
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
        return !mcjty.lib.varia.BlockPosTools.isValid(consoleData(stack).getCoordinate());
    }

    @Override
    protected String getInfoString(ItemStack stack) {
        ConsoleScreenModule data = consoleData(stack);
        return ModuleTools.getTargetString("Processor", net.minecraft.core.GlobalPos.of(data.getDim(), data.getCoordinate()));
    }

    @Override
    public String getModuleName() {
        return "VAR";
    }

    public static ConsoleScreenModule consoleData(ItemStack stack) {
        ConsoleScreenModule data = stack.get(VariousModule.CONSOLE_MODULE_DATA);
        if (data == null) {
            data = ConsoleScreenModule.DEFAULT;
        }
        return data;
    }

    public static void consoleData(ItemStack stack, java.util.function.Function<ConsoleScreenModule, ConsoleScreenModule> setter) {
        ConsoleScreenModule data = consoleData(stack);
        data = setter.apply(data);
        stack.set(VariousModule.CONSOLE_MODULE_DATA.get(), data);
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Block:")
                .block(stack -> GlobalPos.of(consoleData(stack).getDim(), consoleData(stack).getCoordinate()), stack -> "Processor")
                .nl();
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        var hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        ConsoleScreenModule data = consoleData(stack);
        if (block == ProcessorModule.PROCESSOR.block().get()) {
            data = data.withDim(world.dimension());
            data = data.withCoordinate(pos);
            String name = Tools.getReadableName(world, pos);
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
            if (world.isClientSide) {
                Logging.message(player, "Console module is set to block");
            }
        } else {
            data = data.withCoordinate(mcjty.lib.varia.BlockPosTools.INVALID);
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Console module is cleared");
            }
        }
        stack.set(VariousModule.CONSOLE_MODULE_DATA.get(), data);
        return InteractionResult.SUCCESS;
    }


    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolscontrol:advanced/modules");
    }

}

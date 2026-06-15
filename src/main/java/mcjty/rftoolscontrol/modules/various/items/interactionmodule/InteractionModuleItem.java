package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.setup.Config;
import mcjty.lib.gui.ManualEntry;
import mcjty.rftoolsbase.tools.ManualHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class InteractionModuleItem extends GenericModuleItem {

    public InteractionModuleItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return InteractionScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return InteractionScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return VariousModule.INTERACTION_MODULE_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return InteractionScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new InteractionClientScreenModule();
    }

    @Override
    protected int getUses(ItemStack stack) {
        return Config.INTERACTMODULE_RFPERTICK.get();
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
    public String getModuleName() {
        return "INT";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:")
                .text((stack, s) -> data(stack, module -> module.withLine(s)), stack -> data(stack).line(), "Label text")
                .color((stack, c) -> data(stack, module -> module.withColor(c)), stack -> data(stack).color(), "Label color")
                .nl()
                .label("Button:")
                .text((stack, s) -> data(stack, module -> module.withButton(s)), stack -> data(stack).button(), "Button text")
                .color((stack, c) -> data(stack, module -> module.withButtonColor(c)), stack -> data(stack).buttonColor(), "Button color")
                .nl()
                .label("Signal:")
                .text((stack, s) -> data(stack, module -> module.withSignal(s)), stack -> data(stack).signal(), "Signal name")
                .nl()
                .choices((stack, choice) -> data(stack, module -> module.withAlign(TextAlign.get(choice))), stack -> data(stack).align().getSerializedName(), "Label alignment", "Left", "Center", "Right")
                .nl();
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = player.getItemInHand(hand);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == ProcessorModule.PROCESSOR.block().get()) {
            data(stack, module -> module.withTarget(world.dimension(), pos));
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, Tools.getReadableName(world, pos));
            if (world.isClientSide) {
                Logging.message(player, "Interaction module is set to block");
            }
        } else {
            data(stack, module -> module.withCoordinate(BlockPosTools.INVALID));
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Interaction module is cleared");
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionScreenModule data(ItemStack stack) {
        InteractionScreenModule data = stack.get(VariousModule.INTERACTION_MODULE_DATA);
        if (data == null) {
            data = InteractionScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Function<InteractionScreenModule, InteractionScreenModule> setter) {
        InteractionScreenModule data = data(stack);
        data = setter.apply(data);
        stack.set(VariousModule.INTERACTION_MODULE_DATA.get(), data);
    }

    @Override
    public ManualEntry getManualEntry() {
        return ManualHelper.create("rftoolscontrol:advanced/modules");
    }

}

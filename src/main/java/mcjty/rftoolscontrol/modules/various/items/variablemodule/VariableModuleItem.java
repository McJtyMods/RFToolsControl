package mcjty.rftoolscontrol.modules.various.items.variablemodule;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.GlobalPos;
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

public class VariableModuleItem extends GenericModuleItem {

    public VariableModuleItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return VariableScreenModule.CODEC;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return VariableScreenModule.STREAM_CODEC;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return VariousModule.VARIABLE_MODULE_DATA.get();
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return VariableScreenModule.DEFAULT;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return new VariableClientScreenModule();
    }

    @Override
    protected int getUses(ItemStack stack) {
        return Config.VARIABLEMODULE_RFPERTICK.get();
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
        return "VAR";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:")
                .text((stack, s) -> data(stack, module -> module.withLine(s)), stack -> data(stack).line(), "Label text")
                .color((stack, c) -> data(stack, module -> module.withColor(c)), stack -> data(stack).color(), "Label color")
                .nl()

                .label("Stats:")
                .color((stack, c) -> data(stack, module -> module.withVarColor(c)), stack -> data(stack).varColor(), "Color for the variable text")
                .nl()

                .label("Var:")
                .integer((stack, v) -> data(stack, module -> module.withVarIdx(v == null ? -1 : v)), stack -> data(stack).varIdx(), "Index of the variable")
                .nl()

                .choices((stack, choice) -> data(stack, module -> module.withAlign(TextAlign.get(choice))), stack -> data(stack).align().getSerializedName(), "Label alignment", "Left", "Center", "Right")
                .nl()

                .block(stack -> GlobalPos.of(data(stack).dim(), data(stack).coordinate()), stack -> data(stack).monitorName())
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
            String name = Tools.getReadableName(world, pos);
            data(stack, module -> module.withTarget(world.dimension(), pos, name));
            ModuleTools.setPositionInModule(stack, world.dimension(), pos, name);
            if (world.isClientSide) {
                Logging.message(player, "Variable module is set to block");
            }
        } else {
            data(stack, module -> module.clearTarget());
            ModuleTools.clearPositionInModule(stack);
            if (world.isClientSide) {
                Logging.message(player, "Variable module is cleared");
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static VariableScreenModule data(ItemStack stack) {
        VariableScreenModule data = stack.get(VariousModule.VARIABLE_MODULE_DATA);
        if (data == null) {
            data = VariableScreenModule.DEFAULT;
        }
        return data;
    }

    public static void data(ItemStack stack, Function<VariableScreenModule, VariableScreenModule> setter) {
        VariableScreenModule data = data(stack);
        data = setter.apply(data);
        stack.set(VariousModule.VARIABLE_MODULE_DATA.get(), data);
    }
}

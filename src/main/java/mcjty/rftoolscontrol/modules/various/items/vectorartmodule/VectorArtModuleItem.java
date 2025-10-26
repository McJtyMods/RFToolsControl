package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import com.mojang.serialization.Codec;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.ModuleTools;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IScreenModule;
import mcjty.rftoolsbase.tools.GenericModuleItem;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
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

public class VectorArtModuleItem extends GenericModuleItem {

    public VectorArtModuleItem() {
        super(RFToolsControl.setup.defaultProperties()
                .stacksTo(1)
                .durability(1));
    }

    // @todo 1.21
    @Override
    public @Nullable Codec<? extends IScreenModule<?, ?>> codec() {
        return null;
    }

    @Override
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, ? extends IScreenModule<?, ?>> streamCodec() {
        return null;
    }

    @Override
    public @Nullable DataComponentType<? extends IScreenModule<?, ?>> componentType() {
        return null;
    }

    @Override
    public IScreenModule<?, ?> createServerScreenModule() {
        return null;
    }

    @Override
    public IClientScreenModule<?> createClientScreenModule() {
        return null;
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

    // @todo 1.21
//    @Override
//    public Class<VectorArtScreenModule> getServerScreenModule() {
//        return VectorArtScreenModule.class;
//    }
//
//    @Override
//    public Class<VectorArtClientScreenModule> getClientScreenModule() {
//        return VectorArtClientScreenModule.class;
//    }

    @Override
    public String getModuleName() {
        return "VAR";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        // @todo 1.21
//        guiBuilder
//                .block("monitor").nl();
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
        CompoundTag tagCompound = new CompoundTag(); // @todo 1.21 stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundTag();
        }

        if (block == ProcessorModule.PROCESSOR.block().get()) {
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
        // @todo 1.21
//        stack.setTag(tagCompound);
        return InteractionResult.SUCCESS;
    }

}

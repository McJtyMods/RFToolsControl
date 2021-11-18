package mcjty.rftoolscontrol.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.rftoolsbase.api.control.registry.IFunctionRegistry;
import mcjty.rftoolsbase.api.control.registry.IOpcodeRegistry;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.compat.rftoolssupport.RFToolsSupport;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.registry.FunctionRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import mcjty.rftoolscontrol.modules.processor.logic.registry.OpcodeRegistry;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.extensions.IForgePacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModSetup extends DefaultModSetup {

    public ModSetup() {
        createTab("rftoolscontrol", () -> new ItemStack(ProcessorModule.PROCESSOR.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        e.enqueueWork(() -> {
            CommandHandler.registerCommands();
            McJtyLib.registerCommandInfo(CraftingStationTileEntity.CMD_GETCRAFTABLE.getName(), ItemStack.class, PacketBuffer::readItem, PacketBuffer::writeItem);
            McJtyLib.registerCommandInfo(CraftingStationTileEntity.CMD_GETREQUESTS.getName(), CraftingRequest.class, CraftingRequest::fromPacket, CraftingRequest::toPacket);
            McJtyLib.registerCommandInfo(MultiTankTileEntity.CMD_GETFLUIDS.getName(), FluidStack.class, IForgePacketBuffer::readFluidStack, IForgePacketBuffer::writeFluidStack);

            McJtyLib.registerCommandInfo(ProcessorTileEntity.CMD_GETLOG.getName(), String.class, buf -> buf.readUtf(32767), PacketBuffer::writeUtf);
            McJtyLib.registerCommandInfo(ProcessorTileEntity.CMD_GETVARS.getName(), Parameter.class, ParameterTools::readFromBuf, ParameterTools::writeToBuf);
            McJtyLib.registerCommandInfo(ProcessorTileEntity.CMD_GETFLUIDS.getName(), PacketGetFluids.FluidEntry.class, PacketGetFluids.FluidEntry::fromPacket, PacketGetFluids.FluidEntry::toPacket);

            McJtyLib.registerCommandInfo(ProcessorTileEntity.CMD_GETDEBUGLOG.getName(), String.class, buf -> buf.readUtf(32767), PacketBuffer::writeUtf);
        });

        RFToolsCtrlMessages.registerMessages("rftoolsctrl");

        Opcodes.init();
        Functions.init();
    }

    public void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("getOpcodeRegistry".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<IOpcodeRegistry, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(new OpcodeRegistry());
            } else if ("getFunctionRegistry".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<IFunctionRegistry, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(new FunctionRegistry());
            }
        });
    }

    @Override
    protected void setupModCompat() {
        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();
        InterModComms.sendTo("rftoolsutility", "getScreenModuleRegistry", RFToolsSupport.GetScreenModuleRegistry::new);
    }
}

package mcjty.rftoolscontrol.compat;

import mcjty.lib.compat.theoneprobe.McJtyLibTOPDriver;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.varia.Tools;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RFToolsControlTOPDriver implements TOPDriver {

    public static final RFToolsControlTOPDriver DRIVER = new RFToolsControlTOPDriver();

    private final Map<ResourceLocation, TOPDriver> drivers = new HashMap<>();

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        ResourceLocation id = blockState.getBlock().getRegistryName();
        if (!drivers.containsKey(id)) {
            if (blockState.getBlock() == VariousSetup.NODE.get()) {
                drivers.put(id, new NodeDriver());
            } else if (blockState.getBlock() == ProcessorSetup.PROCESSOR.get()) {
                drivers.put(id, new ProcessorDriver());
            } else {
                drivers.put(id, new DefaultDriver());
            }
        }
        TOPDriver driver = drivers.get(id);
        if (driver != null) {
            driver.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class DefaultDriver implements TOPDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            McJtyLibTOPDriver.DRIVER.addStandardProbeInfo(mode, probeInfo, player, world, blockState, data);
        }
    }

    private static class NodeDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (NodeTileEntity node) -> {
                probeInfo.text(TextFormatting.GREEN + "Channel: " + node.getChannelName());
                probeInfo.text(TextFormatting.GREEN + "Name: " + node.getNodeName());
            }, "Bad tile entity!");
        }
    }

    private static class ProcessorDriver extends DefaultDriver {
        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
            super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
            Tools.safeConsume(world.getTileEntity(data.getPos()), (ProcessorTileEntity processor) -> {
            if (processor.hasNetworkCard()) {
                probeInfo.text(TextFormatting.GREEN + "Channel: " + processor.getChannelName());
                probeInfo.text(TextFormatting.GREEN + "Nodes: " + processor.getNodeCount());
            }
            if (mode == ProbeMode.EXTENDED) {
                List<String> lastMessages = processor.getLastMessages(6);
                if (!lastMessages.isEmpty()) {
                    IProbeInfo v = probeInfo.vertical(probeInfo.defaultLayoutStyle().borderColor(0xffff0000));
                    for (String s : lastMessages) {
                        v.text("    " + s);
                    }
                }
            }
            }, "Bad tile entity!");
        }
    }

}

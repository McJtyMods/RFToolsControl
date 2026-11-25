package mcjty.rftoolscontrol.modules.processor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.network.Networking;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.data.HudMode;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetLog;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.CMD_GETDEBUGLOG;

public class ProcessorRenderer implements BlockEntityRenderer<ProcessorTileEntity, ProcessorRenderer.State> {

    public static class State extends BlockEntityRenderState {
        private HudMode mode;
        private Direction facing;
        private boolean aboveAir;
        private List<GfxOp> operations = List.of();
        private List<String> log = List.of();
    }

    public ProcessorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ProcessorTileEntity tile, State state, float partialTicks, Vec3 camera, CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, camera, overlay);
        state.mode = tile.getShowHud();
        BlockState blockState = tile.getBlockState();
        if (state.mode == HudMode.OFF || !(blockState.getBlock() instanceof BaseBlock block)) {
            state.mode = HudMode.OFF;
            return;
        }

        state.facing = block.getFrontDirection(block.getRotationType(), blockState);
        state.aboveAir = tile.getLevel().isEmptyBlock(tile.getBlockPos().above());
        long time = System.currentTimeMillis();
        if (time - tile.clientTime > 250) {
            switch (state.mode) {
                case DB -> Networking.sendToServer(PacketGetListFromServer.create(tile.getBlockPos(), CMD_GETDEBUGLOG.name()));
                case GFX -> RFToolsCtrlMessages.sendToServer(PacketSendServerCommand.create(
                        RFToolsControl.MODID, CommandHandler.CMD_GETGRAPHICS,
                        TypedMap.builder().put(CommandHandler.PARAM_POS, tile.getBlockPos()).build()));
                default -> RFToolsCtrlMessages.sendToServer(PacketGetLog.create(tile.getDimension(), tile.getBlockPos(), false));
            }
            tile.clientTime = time;
        }
        if (state.mode == HudMode.GFX) {
            List<GfxOp> operations = tile.getClientGfxOps();
            state.operations = operations == null ? List.of() : List.copyOf(operations);
        } else {
            state.log = List.copyOf(state.mode == HudMode.DB ? tile.getClientDebugLog() : tile.getClientLog());
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.mode == HudMode.OFF) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 1.5F, 0.5F);
        switch (state.facing) {
            case UP -> RenderHelper.rotateXP(pose, -90);
            case DOWN -> RenderHelper.rotateXP(pose, 90);
            case NORTH -> RenderHelper.rotateYP(pose, -180);
            case WEST -> RenderHelper.rotateYP(pose, -90);
            case EAST -> RenderHelper.rotateYP(pose, 90);
        }
        pose.translate(-0.5F, 0.5F, state.aboveAir ? .0325F : .5325F);
        pose.scale(.0075F, -.0075F, .0075F);

        if (state.mode == HudMode.GFX) {
            for (GfxOp operation : state.operations) {
                operation.submit(pose, collector);
            }
        } else {
            Font font = Minecraft.getInstance().font;
            int y = 7;
            for (int index = Math.max(0, state.log.size() - 11); index < state.log.size(); index++) {
                String line = font.plainSubstrByWidth(state.log.get(index), 115);
                RenderHelper.renderText(font, line, 7, y, 0xffffffff, pose, collector, RenderHelper.MAX_BRIGHTNESS);
                y += 10;
            }
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static void register() {
        BlockEntityRenderers.register(ProcessorModule.PROCESSOR.be().get(), ProcessorRenderer::new);
    }
}

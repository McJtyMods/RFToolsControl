package mcjty.rftoolscontrol.modules.processor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.network.PacketGetListFromServer;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetLog;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.List;

import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;


public class ProcessorRenderer implements BlockEntityRenderer<ProcessorTileEntity> {

    public ProcessorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ProcessorTileEntity te, float partialTicks, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getShowHud() == HUD_OFF) {
            return;
        }

        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        Block block = state.getBlock();
        if (!(block instanceof BaseBlock baseBlock)) {
            return;
        }

        matrixStack.pushPose();

        Direction facing = baseBlock.getFrontDirection(baseBlock.getRotationType(), state);

        matrixStack.translate(0.5F, 1.5F, 0.5F);

        if (facing == Direction.UP) {
            RenderHelper.rotateXP(matrixStack, -90.f);
        } else if (facing == Direction.DOWN) {
            RenderHelper.rotateXP(matrixStack, 90.f);
        } else {
            float rotY = switch (facing) {
                case NORTH -> 180.0F;
                case WEST -> 90.0F;
                case EAST -> -90.0F;
                default -> 0.0F;
            };
            RenderHelper.rotateYP(matrixStack, -rotY);
        }

        if (te.getLevel().isEmptyBlock(te.getBlockPos().above())) {
            matrixStack.translate(0.0F, 0, -0.4375F + .4f);
        } else {
            matrixStack.translate(0.0F, 0, -0.4375F + .9f);
        }

        GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
        graphics.pose().last().pose().set(matrixStack.last().pose());
        graphics.pose().last().normal().set(matrixStack.last().normal());
        renderHud(graphics, buffer, Minecraft.getInstance().font, te);

        matrixStack.popPose();
    }

    private void renderHud(GuiGraphics graphics, MultiBufferSource buffer, Font fontrenderer, ProcessorTileEntity tileEntity) {
        float f3;
        float factor = 0 + 1.0f;
        int currenty = 7;

        PoseStack matrixStack = graphics.pose();
        matrixStack.translate(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        matrixStack.scale(f3 * factor, -f3 * factor, f3);
//        GlStateManager.normal3f(0.0F, 0.0F, 1.0F);
//        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (tileEntity.getShowHud() == HUD_GFX) {
            renderGfx(graphics, buffer, tileEntity);
        } else {
            renderLog(matrixStack, buffer, fontrenderer, tileEntity, currenty);
        }
    }

    private void renderLog(PoseStack matrixStack, MultiBufferSource buffer, Font fontrenderer, ProcessorTileEntity tileEntity, int currenty) {
        List<String> log = tileEntity.getShowHud() == HUD_DB ? tileEntity.getClientDebugLog() : tileEntity.getClientLog();
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            if (tileEntity.getShowHud() == HUD_DB) {
                RFToolsCtrlMessages.sendToServer(PacketGetListFromServer.create(tileEntity.getBlockPos(), CMD_GETDEBUGLOG.name()));
            } else {
                RFToolsCtrlMessages.sendToServer(PacketGetLog.create(tileEntity.getDimension(), tileEntity.getBlockPos(), false));
            }
            tileEntity.clientTime = t;
        }

        int height = 10;
        int logsize = log.size();
        int i = 0;
        for (String s : log) {
            if (i >= logsize - 11) {
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    RenderHelper.renderText(fontrenderer, fontrenderer.plainSubstrByWidth(s, 115), 7, currenty, 0xffffffff, matrixStack, buffer, RenderHelper.MAX_BRIGHTNESS);
                    currenty += height;
                }
            }
            i++;
        }
    }

    private void renderGfx(GuiGraphics graphics, MultiBufferSource buffer, ProcessorTileEntity tileEntity) {
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            RFToolsCtrlMessages.sendToServer(PacketSendServerCommand.create(RFToolsControl.MODID, CommandHandler.CMD_GETGRAPHICS,
                    TypedMap.builder().put(CommandHandler.PARAM_POS, tileEntity.getBlockPos()).build()));
            tileEntity.clientTime = t;
        }
        List<GfxOp> ops = tileEntity.getClientGfxOps();
        if (ops != null) {
            for (GfxOp op : ops) {
                op.render(graphics, buffer);
            }
        }
    }

    public static void register() {
        BlockEntityRenderers.register(ProcessorModule.TYPE_PROCESSOR.get(), ProcessorRenderer::new);
    }
}

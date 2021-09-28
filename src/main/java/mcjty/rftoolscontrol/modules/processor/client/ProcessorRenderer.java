package mcjty.rftoolscontrol.modules.processor.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetDebugLog;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetLog;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import mcjty.rftoolscontrol.setup.RFToolsCtrlMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.List;

import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity.*;


public class ProcessorRenderer extends TileEntityRenderer<ProcessorTileEntity> {

    public ProcessorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(ProcessorTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getShowHud() == HUD_OFF) {
            return;
        }

        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        Block block = state.getBlock();
        if (!(block instanceof BaseBlock)) {
            return;
        }
        BaseBlock baseBlock = (BaseBlock) block;

        matrixStack.pushPose();

        Direction facing = baseBlock.getFrontDirection(baseBlock.getRotationType(), state);

        matrixStack.translate(0.5F, 1.5F, 0.5F);

        if (facing == Direction.UP) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
        } else if (facing == Direction.DOWN) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        } else {
            float rotY = 0.0F;
            if (facing == Direction.NORTH) {
                rotY = 180.0F;
            } else if (facing == Direction.WEST) {
                rotY = 90.0F;
            } else if (facing == Direction.EAST) {
                rotY = -90.0F;
            }
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-rotY));
        }

        if (te.getLevel().isEmptyBlock(te.getBlockPos().above())) {
            matrixStack.translate(0.0F, 0, -0.4375F + .4f);
        } else {
            matrixStack.translate(0.0F, 0, -0.4375F + .9f);
        }

        renderHud(matrixStack, buffer, Minecraft.getInstance().font, te);

        matrixStack.popPose();
    }

    private void renderHud(MatrixStack matrixStack, IRenderTypeBuffer buffer, FontRenderer fontrenderer, ProcessorTileEntity tileEntity) {
        float f3;
        float factor = 0 + 1.0f;
        int currenty = 7;

        matrixStack.translate(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        matrixStack.scale(f3 * factor, -f3 * factor, f3);
//        GlStateManager.normal3f(0.0F, 0.0F, 1.0F);
//        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (tileEntity.getShowHud() == HUD_GFX) {
            renderGfx(matrixStack, buffer, tileEntity);
        } else {
            renderLog(matrixStack, buffer, fontrenderer, tileEntity, currenty);
        }
    }

    private void renderLog(MatrixStack matrixStack, IRenderTypeBuffer buffer, FontRenderer fontrenderer, ProcessorTileEntity tileEntity, int currenty) {
        List<String> log = tileEntity.getShowHud() == HUD_DB ? tileEntity.getClientDebugLog() : tileEntity.getClientLog();
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            if (tileEntity.getShowHud() == HUD_DB) {
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetDebugLog(tileEntity.getBlockPos()));
            } else {
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetLog(tileEntity.getDimension(), tileEntity.getBlockPos(), false));
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
                    fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(s, 115), 7, currenty, 0xffffffff, false,
                            matrixStack.last().pose(), buffer, false, 0, RenderHelper.MAX_BRIGHTNESS);
                    currenty += height;
                }
            }
            i++;
        }
    }

    private void renderGfx(MatrixStack matrixStack, IRenderTypeBuffer buffer, ProcessorTileEntity tileEntity) {
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsControl.MODID, CommandHandler.CMD_GETGRAPHICS,
                    TypedMap.builder().put(CommandHandler.PARAM_POS, tileEntity.getBlockPos()).build()));
            tileEntity.clientTime = t;
        }
        List<GfxOp> ops = tileEntity.getClientGfxOps();
        if (ops != null) {
            for (GfxOp op : ops) {
                op.render(matrixStack, buffer);
            }
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(ProcessorModule.PROCESSOR_TILE.get(), ProcessorRenderer::new);
    }
}

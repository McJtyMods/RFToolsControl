package mcjty.rftoolscontrol.blocks.processor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolscontrol.CommandHandler;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.network.PacketGetDebugLog;
import mcjty.rftoolscontrol.network.PacketGetLog;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity.*;


public class ProcessorRenderer extends TileEntityRenderer<ProcessorTileEntity> {

    public ProcessorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(ProcessorTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        if (tileEntity.getShowHud() == HUD_OFF) {
            return;
        }

        // @todo 1.15 no state manager
        GlStateManager.pushMatrix();
        float f3 = 0.0f;

        // @todo 1.15 no meta
//        int meta = tileEntity.getBlockMetadata();
//
//        if (meta == 2) {
//            f3 = 180.0F;
//        } else if (meta == 4) {
//            f3 = 90.0F;
//        } else if (meta == 5) {
//            f3 = -90.0F;
//        } else {
//            f3 = 0.0F;
//        }

        // @todo 1.15
//        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.75F, (float) z + 0.5F);
        GlStateManager.rotatef(-f3, 0.0F, 1.0F, 0.0F);
        if (tileEntity.getWorld().isAirBlock(tileEntity.getPos().up())) {
            GlStateManager.translatef(0.0F, -0.2500F, -0.4375F + .4f);
        } else {
            GlStateManager.translatef(0.0F, -0.2500F, -0.4375F + .9f);
        }

        RenderHelper.disableStandardItemLighting();
// @todo 1.15
        //        Minecraft.getInstance().getItemRenderer().disableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();

        renderText(Minecraft.getInstance().fontRenderer, tileEntity);
// @todo 1.15
        //        Minecraft.getInstance().entityRenderer.enableLightmap();

//        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.popMatrix();
    }

    private void renderText(FontRenderer fontrenderer, ProcessorTileEntity tileEntity) {
        float f3;
        float factor = 0 + 1.0f;
        int currenty = 7;

        GlStateManager.translatef(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        GlStateManager.scalef(f3 * factor, -f3 * factor, f3);
        GlStateManager.normal3f(0.0F, 0.0F, 1.0F);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (tileEntity.getShowHud() == HUD_GFX) {
            renderGfx(tileEntity);
        } else {
            renderLog(fontrenderer, tileEntity, currenty);
        }
    }

    private void renderLog(FontRenderer fontrenderer, ProcessorTileEntity tileEntity, int currenty) {
        List<String> log = tileEntity.getShowHud() == HUD_DB ? tileEntity.getClientDebugLog() : tileEntity.getClientLog();
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            if (tileEntity.getShowHud() == HUD_DB) {
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetDebugLog(tileEntity.getPos()));
            } else {
                RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetLog(tileEntity.getPos()));
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
                    fontrenderer.drawString(fontrenderer.trimStringToWidth(s, 115), 7, currenty, 0xffffff);
                    currenty += height;
                }
            }
            i++;
        }
    }

    private void renderGfx(ProcessorTileEntity tileEntity) {
        long t = System.currentTimeMillis();
        if (t - tileEntity.clientTime > 250) {
            RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketSendServerCommand(RFToolsControl.MODID, CommandHandler.CMD_GETGRAPHICS,
                    TypedMap.builder().put(CommandHandler.PARAM_POS, tileEntity.getPos()).build()));
            tileEntity.clientTime = t;
        }
        List<GfxOp> ops = tileEntity.getClientGfxOps();
        if (ops != null) {
            for (GfxOp op : ops) {
                op.render();
            }
        }
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.PROCESSOR_TILE.get(), ProcessorRenderer::new);
    }
}

package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.network.PacketGetDebugLog;
import mcjty.rftoolscontrol.network.PacketGetGraphics;
import mcjty.rftoolscontrol.network.PacketGetLog;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity.*;

@SideOnly(Side.CLIENT)
public class ProcessorRenderer extends TileEntitySpecialRenderer<ProcessorTileEntity> {

    @Override
    public void renderTileEntityAt(ProcessorTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tileEntity.getShowHud() == HUD_OFF) {
            return;
        }

        GlStateManager.pushMatrix();
        float f3;

        int meta = tileEntity.getBlockMetadata();

        if (meta == 2) {
            f3 = 180.0F;
        } else if (meta == 4) {
            f3 = 90.0F;
        } else if (meta == 5) {
            f3 = -90.0F;
        } else {
            f3 = 0.0F;
        }

        GlStateManager.translate((float) x + 0.5F, (float) y + 1.75F, (float) z + 0.5F);
        GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
        if (getWorld().isAirBlock(tileEntity.getPos().up())) {
            GlStateManager.translate(0.0F, -0.2500F, -0.4375F + .4);
        } else {
            GlStateManager.translate(0.0F, -0.2500F, -0.4375F + .9);
        }

        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();

        renderText(this.getFontRenderer(), tileEntity);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

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

        GlStateManager.translate(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        GlStateManager.scale(f3 * factor, -f3 * factor, f3);
        GlStateManager.glNormal3f(0.0F, 0.0F, 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

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
            RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetGraphics(tileEntity.getPos()));
            tileEntity.clientTime = t;
        }
        List<GfxOp> ops = tileEntity.getClientGfxOps();
        if (ops != null) {
            for (GfxOp op : ops) {
                op.render();
            }
        }
    }
}

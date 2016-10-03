package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.network.PacketGetLog;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ProcessorRenderer extends TileEntitySpecialRenderer<ProcessorTileEntity> {

    @Override
    public void renderTileEntityAt(ProcessorTileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
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

        // TileEntity can be null if this is used for an item renderer.
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.75F, (float) z + 0.5F);
        GlStateManager.rotate(-f3, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -0.2500F, -0.4375F);

        FontRenderer fontrenderer = this.getFontRenderer();

        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();

        renderText(fontrenderer, tileEntity);

        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    private static long time = 0;

    private void renderText(FontRenderer fontrenderer, ProcessorTileEntity tileEntity) {
        float f3;
        float factor = 0 + 1.0f;
        int currenty = 7;

        GlStateManager.translate(-0.5F, 0.5F, 0.07F);
        f3 = 0.0075F;
        GlStateManager.scale(f3 * factor, -f3 * factor, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        long t = System.currentTimeMillis();
        if (t-time > 250) {
            RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketGetLog(tileEntity.getPos()));
            time = t;
        }

        int height = 10;
        int logsize = GuiProcessor.fromServer_log.size();
        int i = 0;
        for (String s : GuiProcessor.fromServer_log) {
            if (i >= logsize-11) {
                // Check if this module has enough room
                if (currenty + height <= 124) {
                    fontrenderer.drawString(fontrenderer.trimStringToWidth(s, 115), 7, currenty, 0xffffffff);
                    currenty += height;
                }
            }
            i++;
        }
    }
}

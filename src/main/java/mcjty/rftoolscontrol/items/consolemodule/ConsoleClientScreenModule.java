package mcjty.rftoolscontrol.items.consolemodule;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ConsoleClientScreenModule implements IClientScreenModule<ModuleDataLog> {

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 114;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataLog screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        int xoffset = 7;
        if (screenData != null) {
            List<String> log = screenData.getLog();
            if (log != null) {
                for (String s : log) {
                    renderHelper.renderTextTrimmed(xoffset, currenty, 0xffffffff, renderInfo, s, 480);
                    currenty += 10;
                }
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

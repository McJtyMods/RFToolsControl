package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataLog screenData, ModuleRenderInfo renderInfo) {
        // @todo 1.15 render system @todo 1.18
//        GlStateManager._disableLighting();
        int xoffset = 7;
        if (screenData != null) {
            List<String> log = screenData.getLog();
            if (log != null) {
                for (String s : log) {
                    // @todo 1.15
//                    renderHelper.renderTextTrimmed(xoffset, currenty, 0xffffffff, renderInfo, s, 480);
                    currenty += 10;
                }
            }
        }
    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {

    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {

    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

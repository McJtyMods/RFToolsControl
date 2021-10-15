package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataLog screenData, ModuleRenderInfo renderInfo) {
        // @todo 1.15 render system
        GlStateManager._disableLighting();
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
    public void setupFromNBT(CompoundNBT tagCompound, RegistryKey<World> dim, BlockPos pos) {

    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

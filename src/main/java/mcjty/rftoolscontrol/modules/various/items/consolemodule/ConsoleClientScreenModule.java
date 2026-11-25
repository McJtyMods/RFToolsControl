package mcjty.rftoolscontrol.modules.various.items.consolemodule;

import java.util.List;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataLog;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConsoleClientScreenModule implements IClientScreenModule<ModuleDataLog> {

    @Override
    public TransformMode getTransformMode(ItemStack moduleItem) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleItem) {
        return 114;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, SubmitNodeCollector buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataLog screenData, ModuleRenderInfo renderInfo) {
        int xoffset = 7;
        if (screenData != null) {
            List<String> log = screenData.getLog();
            if (log != null) {
                for (String s : log) {
                    renderHelper.renderText(graphics, buffer, xoffset, currenty, 0xffffffff, renderInfo, s);
                    currenty += 10;
                }
            }
        }
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

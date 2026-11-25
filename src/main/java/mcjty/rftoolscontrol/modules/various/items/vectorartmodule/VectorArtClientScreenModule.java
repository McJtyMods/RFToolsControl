package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import java.util.List;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VectorArtClientScreenModule implements IClientScreenModule<ModuleDataVectorArt> {

    @Override
    public TransformMode getTransformMode(ItemStack moduleStack) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleStack) {
        return 114;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, SubmitNodeCollector buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataVectorArt screenData, ModuleRenderInfo renderInfo) {
        if (screenData != null) {
            List<GfxOp> ops = screenData.getSortedOperations();
            if (ops != null) {
                for (GfxOp op : ops) {
                    op.render(graphics);
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

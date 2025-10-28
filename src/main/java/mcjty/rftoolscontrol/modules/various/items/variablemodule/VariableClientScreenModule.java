package mcjty.rftoolscontrol.modules.various.items.variablemodule;

import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ITextRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsbase.api.screens.TextAlign;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVariable;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VariableClientScreenModule implements IClientScreenModule<ModuleDataVariable> {

    private final ITextRenderHelper labelCache = new ScreenTextHelper();

    public VariableClientScreenModule() {
        labelCache.align(TextAlign.ALIGN_LEFT);
    }

    @Override
    public TransformMode getTransformMode(ItemStack moduleStack) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleStack) {
        return 10;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataVariable screenData, ModuleRenderInfo renderInfo) {
        VariableScreenModule data = VariableModuleItem.data(renderInfo.moduleStack);

        int xoffset = 7;
        if (!data.line().isEmpty()) {
            labelCache.setup(data.line(), 160, renderInfo);
            labelCache.align(data.align());
            labelCache.renderText(graphics, buffer, 0, currenty, data.color(), renderInfo);
            xoffset = 7 + 40;
        }

        if (screenData != null) {
            Parameter parameter = screenData.getParameter();
            if (parameter != null && parameter.getParameterValue() != null) {
                String str = TypeConverters.convertToString(parameter);
                renderHelper.renderText(graphics, buffer, xoffset, currenty, data.varColor(), renderInfo, str);
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

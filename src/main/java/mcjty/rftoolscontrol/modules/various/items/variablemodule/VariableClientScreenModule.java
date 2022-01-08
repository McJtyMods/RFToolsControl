package mcjty.rftoolscontrol.modules.various.items.variablemodule;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVariable;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;

public class VariableClientScreenModule implements IClientScreenModule<ModuleDataVariable> {
    private String line = "";
    private TextAlign textAlign = TextAlign.ALIGN_LEFT;

    private ITextRenderHelper labelCache = null;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataVariable screenData, ModuleRenderInfo renderInfo) {
        if (labelCache == null) {
            labelCache = renderHelper.createTextRenderHelper().align(textAlign);
        }

//        GlStateManager._disableLighting();    // @todo 1.18
        int xoffset;
        if (!line.isEmpty()) {
            labelCache.setup(line, 160, renderInfo);
            // @todo 1.15 render system
//            labelCache.renderText(0, currenty, color, renderInfo);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }
        if (screenData != null) {
            Parameter parameter = screenData.getParameter();
            if (parameter != null && parameter.getParameterValue() != null) {
                String str = TypeConverters.convertToString(parameter);
                // @todo 1.15 render system
//                renderHelper.renderText(xoffset, currenty, varcolor, renderInfo, str);
            }
        }
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            int varIdx = -1;
            if (tagCompound.contains("varIdx")) {
                varIdx = tagCompound.getInt("varIdx");
            } else {
                varIdx = -1;
            }
            line = tagCompound.getString("text");
            int color = 0xffffff;
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            int varcolor = 0xffffff;
            if (tagCompound.contains("varcolor")) {
                varcolor = tagCompound.getInt("varcolor");
            } else {
                varcolor = 0xffffff;
            }
            if (tagCompound.contains("align")) {
                String alignment = tagCompound.getString("align");
                textAlign = TextAlign.get(alignment);
            } else {
                textAlign = TextAlign.ALIGN_LEFT;
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

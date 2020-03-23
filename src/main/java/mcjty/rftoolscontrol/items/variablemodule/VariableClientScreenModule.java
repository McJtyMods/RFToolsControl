package mcjty.rftoolscontrol.items.variablemodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVariable;
import mcjty.rftoolscontrol.logic.TypeConverters;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class VariableClientScreenModule implements IClientScreenModule<ModuleDataVariable> {
    private String line = "";
    private int color = 0xffffff;
    private int varcolor = 0xffffff;
    private int varIdx = -1;
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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataVariable screenData, ModuleRenderInfo renderInfo) {
        if (labelCache == null) {
            labelCache = renderHelper.createTextRenderHelper().align(textAlign);
        }

        GlStateManager.disableLighting();
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
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            if (tagCompound.contains("varIdx")) {
                varIdx = tagCompound.getInt("varIdx");
            } else {
                varIdx = -1;
            }
            line = tagCompound.getString("text");
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
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

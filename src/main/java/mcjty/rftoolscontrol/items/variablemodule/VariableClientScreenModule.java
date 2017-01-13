package mcjty.rftoolscontrol.items.variablemodule;

import mcjty.rftools.api.screens.*;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.logic.TypeConverters;
import mcjty.rftoolscontrol.rftoolssupport.ModuleDataVariable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataVariable screenData, ModuleRenderInfo renderInfo) {
        if (labelCache == null) {
            labelCache = renderHelper.createTextRenderHelper().align(textAlign);
        }

        GlStateManager.disableLighting();
        int xoffset;
        if (!line.isEmpty()) {
            labelCache.setup(line, 160, renderInfo);
            labelCache.renderText(0, currenty, color, renderInfo);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }
        if (screenData != null) {
            Parameter parameter = screenData.getParameter();
            if (parameter != null && parameter.getParameterValue() != null) {
                String str = TypeConverters.convertToString(parameter);
                renderHelper.renderText(xoffset, currenty, varcolor, renderInfo, str);
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("Stats:").color("varcolor", "Color for the variable text").nl()
                .label("Var:").integer("varIdx", "Index of the variable").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .block("monitor").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            if (tagCompound.hasKey("varIdx")) {
                varIdx = tagCompound.getInteger("varIdx");
            } else {
                varIdx = -1;
            }
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("varcolor")) {
                varcolor = tagCompound.getInteger("varcolor");
            } else {
                varcolor = 0xffffff;
            }
            if (tagCompound.hasKey("align")) {
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

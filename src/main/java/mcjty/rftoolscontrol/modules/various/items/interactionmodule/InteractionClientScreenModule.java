package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class InteractionClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {
    private String line = "";
    private String button = "";
    private int color = 0xffffff;
    private int buttonColor = 0xffffff;
    private boolean activated = false;
    private TextAlign textAlign = TextAlign.ALIGN_LEFT;

    private ITextRenderHelper labelCache = null;
    private ITextRenderHelper buttonCache = null;

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {
        if (labelCache == null) {
            labelCache = renderHelper.createTextRenderHelper().align(textAlign);
            buttonCache = renderHelper.createTextRenderHelper();
        }

        // @todo 1.15 proper render system
        GlStateManager.disableLighting();
        GlStateManager.enableDepthTest();
        GlStateManager.depthMask(false);
        int xoffset;
        int buttonWidth;
        if (!line.isEmpty()) {
            labelCache.setup(line, 316, renderInfo);
            // @todo 1.15
//            labelCache.renderText(0, currenty + 2, color, renderInfo);
            xoffset = 7 + 80;
            buttonWidth = 170;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        boolean act = activated;

        RenderHelper.drawBeveledBox(xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666);
        buttonCache.setup(button, buttonWidth, renderInfo);
        // @todo 1.15
//        buttonCache.renderText(xoffset -10 + (act ? 1 : 0), currenty + 2, buttonColor, renderInfo);
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 80;
        } else {
            xoffset = 5;
        }
        activated = false;
        if (x >= xoffset) {
            activated = clicked;
        }
    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.contains("buttonColor")) {
                buttonColor = tagCompound.getInt("buttonColor");
            } else {
                buttonColor = 0xffffff;
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

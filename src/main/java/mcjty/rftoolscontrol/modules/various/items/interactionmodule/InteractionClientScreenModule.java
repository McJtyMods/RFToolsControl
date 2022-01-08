package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import mcjty.rftoolsbase.api.screens.IClientScreenModule.TransformMode;

public class InteractionClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {
    private String line = "";
    private String button = "";
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
    public void render(PoseStack matrixStack, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {


        if (labelCache == null) {
            labelCache = renderHelper.createTextRenderHelper().align(textAlign);
            buttonCache = renderHelper.createTextRenderHelper();
        }

        // @todo 1.15 proper render system
//        GlStateManager._disableLighting();    // @todo 1.18
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(false);
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

        RenderHelper.drawBeveledBox(matrixStack, xoffset - 5, currenty, 130 - 7, currenty + 12, act ? 0xff333333 : 0xffeeeeee, act ? 0xffeeeeee : 0xff333333, 0xff666666);
        buttonCache.setup(button, buttonWidth, renderInfo);
        // @todo 1.15
//        buttonCache.renderText(xoffset -10 + (act ? 1 : 0), currenty + 2, buttonColor, renderInfo);
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {
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
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            button = tagCompound.getString("button");
            int color;
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            int buttonColor;
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

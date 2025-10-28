package mcjty.rftoolscontrol.modules.various.items.interactionmodule;

import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataBoolean;
import mcjty.rftoolsbase.tools.ScreenTextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InteractionClientScreenModule implements IClientScreenModule<IModuleDataBoolean> {
    private boolean activated = false;

    private final ITextRenderHelper labelCache = new ScreenTextHelper();
    private final ITextRenderHelper buttonCache = new ScreenTextHelper();

    public InteractionClientScreenModule() {
        labelCache.align(TextAlign.ALIGN_LEFT);
        buttonCache.setDirty();
    }

    @Override
    public TransformMode getTransformMode(ItemStack moduleStack) {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight(ItemStack moduleStack) {
        return 14;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, IModuleDataBoolean screenData, ModuleRenderInfo renderInfo) {

        InteractionScreenModule data = InteractionModuleItem.data(renderInfo.moduleStack);

        int xoffset;
        int buttonWidth;
        if (!data.line().isEmpty()) {
            labelCache.setup(data.line(), 316, renderInfo);
            labelCache.align(data.align());
            labelCache.renderText(graphics, buffer, 0, currenty + 2, data.color(), renderInfo);
            xoffset = 7 + 80;
            buttonWidth = 170;
        } else {
            xoffset = 7 + 5;
            buttonWidth = 490;
        }

        boolean act = activated;

        RenderHelper.drawBeveledBox(graphics, buffer, xoffset - 5, currenty, 130 - 7, currenty + 12,
                act ? 0xff333333 : 0xffeeeeee,
                act ? 0xffeeeeee : 0xff333333,
                0xff666666,
                renderInfo.getLightmapValue());
        buttonCache.setup(data.button(), buttonWidth, renderInfo);
        buttonCache.renderText(graphics, buffer, xoffset - 10 + (act ? 1 : 0), currenty + 2, data.buttonColor(), renderInfo);
    }

    @Override
    public void mouseClick(ItemStack moduleStack, Level world, int x, int y, boolean clicked) {
        int xoffset;
        InteractionScreenModule data = InteractionModuleItem.data(moduleStack);
        if (!data.line().isEmpty()) {
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
    public boolean needsServerData() {
        return false;
    }
}

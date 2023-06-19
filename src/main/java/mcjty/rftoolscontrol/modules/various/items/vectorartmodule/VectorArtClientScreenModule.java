package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class VectorArtClientScreenModule implements IClientScreenModule<ModuleDataVectorArt> {

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 114;
    }

    @Override
    public void render(GuiGraphics graphics, MultiBufferSource buffer, IModuleRenderHelper renderHelper, Font fontRenderer, int currenty, ModuleDataVectorArt screenData, ModuleRenderInfo renderInfo) {
        if (screenData != null) {
            List<GfxOp> ops = screenData.getSortedOperations();
            if (ops != null) {
                for (GfxOp op : ops) {
                    op.render(graphics, buffer);
                }
            }
        }
    }

    @Override
    public void mouseClick(Level world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundTag tagCompound, ResourceKey<Level> dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

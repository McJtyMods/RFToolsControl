package mcjty.rftoolscontrol.items.vectorartmodule;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
import net.minecraft.client.gui.FontRenderer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataVectorArt screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);

        if (screenData != null) {
            List<GfxOp> ops = screenData.getSortedOperations();
            if (ops != null) {
                for (GfxOp op : ops) {
                    op.render();
                }
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

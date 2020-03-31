package mcjty.rftoolscontrol.modules.various.items.vectorartmodule;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import mcjty.rftoolscontrol.compat.rftoolssupport.ModuleDataVectorArt;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

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
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, ModuleDataVectorArt screenData, ModuleRenderInfo renderInfo) {
        if (screenData != null) {
            List<GfxOp> ops = screenData.getSortedOperations();
            if (ops != null) {
                for (GfxOp op : ops) {
                    op.render(matrixStack, buffer);
                }
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, DimensionType dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

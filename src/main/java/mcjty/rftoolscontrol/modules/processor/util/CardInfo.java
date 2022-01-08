package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import net.minecraft.nbt.CompoundTag;

import static mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType.EXCEPT_NOINTERNALFLUIDSLOT;
import static mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType.EXCEPT_NOINTERNALSLOT;

public class CardInfo {

    // 32-bit field for item allocation
    private int itemAllocation;
    // 32-bit field for variable allocation
    private int varAllocation;
    // 32-bit field for fluid allocation
    private int fluidAllocation;

    private CompiledCard compiledCard;

    private int slotCache[] = null;
    private int varCache[] = null;
    private int fluidCache[] = null;

    public int getFluidAllocation() {
        return fluidAllocation;
    }

    public void setFluidAllocation(int fluidAllocation) {
        this.fluidAllocation = fluidAllocation;
        fluidCache = null;
    }

    public int getItemAllocation() {
        return itemAllocation;
    }

    public void setItemAllocation(int itemAllocation) {
        this.itemAllocation = itemAllocation;
        slotCache = null;
    }

    public int getVarAllocation() {
        return varAllocation;
    }

    public void setVarAllocation(int varAllocation) {
        this.varAllocation = varAllocation;
        varCache = null;
    }

    public void setCompiledCard(CompiledCard compiledCard) {
        this.compiledCard = compiledCard;
    }

    public CompiledCard getCompiledCard() {
        return compiledCard;
    }

    public Integer getRealFluidSlot(Integer virtualSlot) {
        if (virtualSlot == null) {
            return null;
        }
        return getRealFluidSlot((int)virtualSlot);
    }

    public int getRealFluidSlot(int virtualSlot) {
        if (fluidCache == null) {
            fluidCache = new int[MultiTankTileEntity.TANKS * 6];
            int idx = 0;
            for (int i = 0 ; i < MultiTankTileEntity.TANKS * 6 ; i++) {
                if (((fluidAllocation >> i) & 1) == 1) {
                    fluidCache[idx] = i;
                    idx++;
                }
            }
            for ( ; idx < MultiTankTileEntity.TANKS * 6 ; idx++) {
                fluidCache[idx] = -1;
            }
        }
        int realSlot = fluidCache[virtualSlot];
        if (realSlot == -1) {
            throw new ProgException(EXCEPT_NOINTERNALFLUIDSLOT);
        }

        return realSlot;
    }

    public Integer getRealSlot(Integer virtualSlot) {
        if (virtualSlot == null) {
            return null;
        }
        return getRealSlot((int)virtualSlot);
    }

    public int getRealSlot(int virtualSlot) {
        if (slotCache == null) {
            slotCache = new int[ProcessorTileEntity.ITEM_SLOTS];
            int idx = 0;
            for (int i = 0 ; i < ProcessorTileEntity.ITEM_SLOTS ; i++) {
                if (((itemAllocation >> i) & 1) == 1) {
                    slotCache[idx] = i;
                    idx++;
                }
            }
            for ( ; idx < ProcessorTileEntity.ITEM_SLOTS ; idx++) {
                slotCache[idx] = -1;
            }
        }
        int realSlot = slotCache[virtualSlot];
        if (realSlot == -1) {
            throw new ProgException(EXCEPT_NOINTERNALSLOT);
        }

        return realSlot + ProcessorContainer.SLOT_BUFFER;
    }

    public int getRealVar(int virtualVar) {
        if (varCache == null) {
            varCache = new int[ProcessorTileEntity.MAXVARS];
            int idx = 0;
            for (int i = 0 ; i < ProcessorTileEntity.MAXVARS ; i++) {
                if (((varAllocation >> i) & 1) == 1) {
                    varCache[idx] = i;
                    idx++;
                }
            }
            for ( ; idx < ProcessorTileEntity.MAXVARS ; idx++) {
                varCache[idx] = -1;
            }
        }
        return varCache[virtualVar];
    }

    public Integer getRealVar(Integer virtualVar) {
        if (virtualVar == null) {
            return null;
        }
        return getRealVar((int)virtualVar);
    }

    public CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("itemAlloc", itemAllocation);
        tag.putInt("varAlloc", varAllocation);
        tag.putInt("fluidAlloc", fluidAllocation);
        return tag;
    }

    public static CardInfo readFromNBT(CompoundTag tag) {
        CardInfo cardInfo = new CardInfo();
        cardInfo.itemAllocation = tag.getInt("itemAlloc");
        cardInfo.varAllocation = tag.getInt("varAlloc");
        cardInfo.fluidAllocation = tag.getInt("fluidAlloc");
        cardInfo.slotCache = null;
        cardInfo.varCache = null;
        cardInfo.fluidCache = null;
        return cardInfo;
    }
}

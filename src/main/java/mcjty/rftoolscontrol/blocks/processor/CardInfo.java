package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.blocks.multitank.MultiTankTileEntity;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.running.ProgException;
import net.minecraft.nbt.CompoundNBT;

import static mcjty.rftoolscontrol.logic.running.ExceptionType.EXCEPT_NOINTERNALFLUIDSLOT;
import static mcjty.rftoolscontrol.logic.running.ExceptionType.EXCEPT_NOINTERNALSLOT;

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
        if (virtualSlot < 0 && virtualSlot >= MultiTankTileEntity.TANKS * 6) {
            throw new ProgException(EXCEPT_NOINTERNALFLUIDSLOT);
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
        if (virtualSlot < 0 && virtualSlot >= ProcessorTileEntity.ITEM_SLOTS) {
            throw new ProgException(EXCEPT_NOINTERNALSLOT);
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
        if (virtualVar < 0 && virtualVar >= ProcessorTileEntity.MAXVARS) {
            return -1;
        }
        return varCache[virtualVar];
    }

    public Integer getRealVar(Integer virtualVar) {
        if (virtualVar == null) {
            return null;
        }
        return getRealVar((int)virtualVar);
    }

    public CompoundNBT writeToNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("itemAlloc", itemAllocation);
        tag.putInt("varAlloc", varAllocation);
        tag.putInt("fluidAlloc", fluidAllocation);
        return tag;
    }

    public static CardInfo readFromNBT(CompoundNBT tag) {
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

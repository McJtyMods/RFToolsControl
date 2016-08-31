package mcjty.rftoolscontrol.blocks.processor;

import net.minecraft.nbt.NBTTagCompound;

public class CardInfo {

    // 32-bit field for item allocation
    private int itemAllocation;
    // 32-bit field for variable allocation
    private int varAllocation;

    public int getItemAllocation() {
        return itemAllocation;
    }

    public void setItemAllocation(int itemAllocation) {
        this.itemAllocation = itemAllocation;
    }

    public int getVarAllocation() {
        return varAllocation;
    }

    public void setVarAllocation(int varAllocation) {
        this.varAllocation = varAllocation;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("itemAlloc", itemAllocation);
        tag.setInteger("varAlloc", varAllocation);
        return tag;
    }

    public static CardInfo readFromNBT(NBTTagCompound tag) {
        CardInfo cardInfo = new CardInfo();
        cardInfo.itemAllocation = tag.getInteger("itemAlloc");
        cardInfo.varAllocation = tag.getInteger("varAlloc");
        return cardInfo;
    }
}

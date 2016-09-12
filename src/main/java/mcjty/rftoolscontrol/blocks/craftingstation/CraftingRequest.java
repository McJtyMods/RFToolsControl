package mcjty.rftoolscontrol.blocks.craftingstation;

import net.minecraft.item.ItemStack;

public class CraftingRequest {
    private final String craftId;
    private final ItemStack stack;
    private long failed = -1;             // If != -1 we failed but show for a while longer
    private long ok = -1;                 // If != -1we are ok but show for a while longer


    public CraftingRequest(String craftId, ItemStack stack) {
        this.craftId = craftId;
        this.stack = stack;
    }

    public String getCraftId() {
        return craftId;
    }

    public ItemStack getStack() {
        return stack;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long failed) {
        this.failed = failed;
    }

    public long getOk() {
        return ok;
    }

    public void setOk(long ok) {
        this.ok = ok;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CraftingRequest that = (CraftingRequest) o;

        if (!craftId.equals(that.craftId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return craftId.hashCode();
    }
}

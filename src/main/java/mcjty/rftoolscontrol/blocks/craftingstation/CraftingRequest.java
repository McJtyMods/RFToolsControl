package mcjty.rftoolscontrol.blocks.craftingstation;

import net.minecraft.item.ItemStack;

public class CraftingRequest {
    private final String craftId;
    private final ItemStack stack;

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

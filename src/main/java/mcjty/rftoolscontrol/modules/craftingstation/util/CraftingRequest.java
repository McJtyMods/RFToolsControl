package mcjty.rftoolscontrol.modules.craftingstation.util;

import mcjty.lib.network.NetworkTools;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class CraftingRequest {
    private final String ticket;
    private final ItemStack stack;
    private long failed = -1;             // If != -1 we failed but show for a while longer
    private long ok = -1;                 // If != -1we are ok but show for a while longer
    private int todo = 0;                 // Todo counter

    public CraftingRequest(String ticket, ItemStack stack, int todo) {
        this.ticket = ticket;
        this.stack = stack;
        this.todo = todo;
    }

    public static CraftingRequest fromPacket(PacketBuffer buf) {
        String id = buf.readUtf(32767);
        ItemStack stack = NetworkTools.readItemStack(buf);
        int amount = buf.readInt();
        CraftingRequest request = new CraftingRequest(id, stack, amount);
        request.setOk(buf.readLong());
        request.setFailed(buf.readLong());
        return request;
    }

    public static void toPacket(PacketBuffer buf, CraftingRequest item) {
        buf.writeUtf(item.getTicket());
        NetworkTools.writeItemStack(buf, item.getStack());
        buf.writeInt(item.getTodo());
        buf.writeLong(item.getOk());
        buf.writeLong(item.getFailed());
    }

    public String getTicket() {
        return ticket;
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

    public int getTodo() {
        return todo;
    }

    public void setTodo(int todo) {
        this.todo = todo;
    }

    public void decrTodo() {
        this.todo--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CraftingRequest that = (CraftingRequest) o;

        if (!ticket.equals(that.ticket)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return ticket.hashCode();
    }
}

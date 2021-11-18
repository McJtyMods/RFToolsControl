package mcjty.rftoolscontrol.modules.craftingstation.util;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.network.NetworkTools;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CraftingRequest {
    private final String ticket;
    private final ItemStack stack;
    private long failed = -1;             // If != -1 we failed but show for a while longer
    private long ok = -1;                 // If != -1we are ok but show for a while longer
    private int todo = 0;                 // Todo counter

    public static class Serializer implements ISerializer<CraftingRequest> {
        @Override
        public Function<PacketBuffer, CraftingRequest> getDeserializer() {
            return buf -> {
                String id = buf.readUtf(32767);
                ItemStack stack1 = NetworkTools.readItemStack(buf);
                int amount = buf.readInt();
                CraftingRequest request = new CraftingRequest(id, stack1, amount);
                request.setOk(buf.readLong());
                request.setFailed(buf.readLong());
                return request;
            };
        }

        @Override
        public BiConsumer<PacketBuffer, CraftingRequest> getSerializer() {
            return (buf, item) -> {
                buf.writeUtf(item.getTicket());
                NetworkTools.writeItemStack(buf, item.getStack());
                buf.writeInt(item.getTodo());
                buf.writeLong(item.getOk());
                buf.writeLong(item.getFailed());
            };
        }
    }

    public CraftingRequest(String ticket, ItemStack stack, int todo) {
        this.ticket = ticket;
        this.stack = stack;
        this.todo = todo;
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

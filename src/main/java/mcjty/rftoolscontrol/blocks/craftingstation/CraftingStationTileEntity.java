package mcjty.rftoolscontrol.blocks.craftingstation;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CraftingStationTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    public static final String CMD_GETCRAFTABLE = "getCraftable";
    public static final String CLIENTCMD_GETCRAFTABLE = "getCraftable";
    public static final String CMD_GETREQUESTS = "getRequests";
    public static final String CLIENTCMD_GETREQUESTS = "getRequests";
    public static final String CMD_REQUEST = "request";
    public static final String CMD_CANCEL = "cancel";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CraftingStationContainer.factory, 9);

    private List<BlockPos> processorList = new ArrayList<>();
    private int currentTicket = 0;
    private List<CraftingRequest> activeCraftingRequests = new ArrayList<>();
    private int cleanupCounter = 50;

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    public void registerProcessor(BlockPos pos) {
        if (!processorList.contains(pos)) {
            processorList.add(pos);
        }
        markDirty();
    }


    // @todo optimize finding requests on craftid!!!!

    public ItemStack getCraftResult(String craftId) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (craftId.equals(request.getTicket())) {
                return request.getStack();
            }
        }
        return null;
    }

    private Pair<ProcessorTileEntity, ItemStack> findCraftableItem(int index) {
        for (BlockPos p : processorList) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                List<ItemStack> items = new ArrayList<>();
                processor.getCraftableItems(items);
                for (ItemStack item : items) {
                    if (index == 0) {
                        // found!
                        return Pair.of(processor, item);
                    }
                    index--;
                }
            }
        }
        return null;
    }

    public ItemStack craftOk(ProcessorTileEntity processor, String ticket, @Nullable ItemStack stack) {
        CraftingRequest foundRequest = null;
        for (CraftingRequest request : activeCraftingRequests) {
            if (ticket.equals(request.getTicket())) {
                foundRequest = request;
                break;
            }
        }
        if (foundRequest != null) {
            markDirty();
            foundRequest.decrTodo();
            if (foundRequest.getTodo() <= 0) {
                foundRequest.setOk(System.currentTimeMillis() + 1000);
            } else {
                processor.fireCraftEvent(ticket, stack);
            }
            if (stack != null) {
                return ItemHandlerHelper.insertItem(getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), stack, false);
            }
        }
        return stack;
    }

    public void craftFail(String ticket) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (ticket.equals(request.getTicket())) {
                request.setFailed(System.currentTimeMillis()+2000);
                markDirty();
            }
        }
    }

    private void cancelCraft(int index) {
        try {
            activeCraftingRequests.remove(index);
        } catch (Exception e) {

        }
    }

    private void startCraft(int index, int amount) {
        Pair<ProcessorTileEntity, ItemStack> pair = findCraftableItem(index);
        if (pair == null) {
            // Somehow not possible
            System.out.println("What? Can't happen");
            return;
        }
        String craftID = getNewTicket();
        ItemStack stack = pair.getValue();
        int count = (amount + stack.stackSize-1) / stack.stackSize;
        CraftingRequest request = new CraftingRequest(craftID, stack, count);

        activeCraftingRequests.add(request);
        pair.getKey().fireCraftEvent(craftID, stack);

        cleanupCounter--;
        if (cleanupCounter <= 0) {
            cleanupCounter = 50;
            cleanupStaleRequests();
        }
    }



    public boolean isRequested(ItemStack item) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (request.getStack().isItemEqual(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean request(ItemStack item) {
        for (BlockPos p : processorList) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                List<ItemStack> items = new ArrayList<>();
                processor.getCraftableItems(items);
                for (ItemStack i : items) {
                    if (item.isItemEqual(i)) {
                        String craftID = getNewTicket();
                        activeCraftingRequests.add(new CraftingRequest(craftID, i, 1));
                        processor.fireCraftEvent(craftID, i);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String getNewTicket() {
        currentTicket++;
        markDirty();
        return BlockPosTools.toString(pos) + ":" + currentTicket;
    }

    public List<ItemStack> getCraftableItems() {
        List<ItemStack> items = new ArrayList<>();
        for (BlockPos p : processorList) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                processor.getCraftableItems(items);
            }
        }
        return items;
    }

    private void cleanupStaleRequests() {
        long time = System.currentTimeMillis();
        List<CraftingRequest> oldRequests = this.activeCraftingRequests;
        activeCraftingRequests = new ArrayList<>();
        for (CraftingRequest request : oldRequests) {
            long failed = request.getFailed();
            long ok = request.getOk();
            if ((failed == -1 || time <= failed) && (ok == -1 || time <= ok)) {
                activeCraftingRequests.add(request);
            }
        }

    }

    public List<CraftingRequest> getRequests() {
        cleanupStaleRequests();
        return new ArrayList<>(activeCraftingRequests);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        readProcessorList(tagCompound);
        readRequests(tagCompound);
    }

    private void readRequests(NBTTagCompound tagCompound) {
        NBTTagList list = tagCompound.getTagList("requests", Constants.NBT.TAG_COMPOUND);
        activeCraftingRequests.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound requestTag = list.getCompoundTagAt(i);
            String craftId = requestTag.getString("craftId");
            ItemStack stack = ItemStack.loadItemStackFromNBT(requestTag.getCompoundTag("stack"));
            int count = requestTag.getInteger("count");
            CraftingRequest request = new CraftingRequest(craftId, stack, count);
            request.setFailed(requestTag.getLong("failed"));
            request.setOk(requestTag.getLong("ok"));
            activeCraftingRequests.add(request);
        }
    }

    private void readProcessorList(NBTTagCompound tagCompound) {
        NBTTagList list = tagCompound.getTagList("processors", Constants.NBT.TAG_COMPOUND);
        processorList.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            processorList.add(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        writeProcessorList(tagCompound);
        writeRequests(tagCompound);
        return tagCompound;
    }

    private void writeRequests(NBTTagCompound tagCompound) {
        NBTTagList list = new NBTTagList();
        for (CraftingRequest request : activeCraftingRequests) {
            NBTTagCompound requestTag = new NBTTagCompound();
            requestTag.setString("craftId", request.getTicket());
            NBTTagCompound stackNbt = new NBTTagCompound();
            request.getStack().writeToNBT(stackNbt);
            requestTag.setTag("stack", stackNbt);
            requestTag.setInteger("count", request.getTodo());
            requestTag.setLong("failed", request.getFailed());
            requestTag.setLong("ok", request.getOk());
            list.appendTag(requestTag);
        }

        tagCompound.setTag("requests", list);
    }

    private void writeProcessorList(NBTTagCompound tagCompound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos p : processorList) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", p.getX());
            tag.setInteger("y", p.getY());
            tag.setInteger("z", p.getZ());
            list.appendTag(tag);
        }
        tagCompound.setTag("processors", list);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        currentTicket = tagCompound.getInteger("craftId");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("craftId", currentTicket);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_REQUEST.equals(command)) {
            int index = args.get("index").getInteger();
            int amount = args.get("amount").getInteger();
            startCraft(index, amount);
            return true;
        } else if (CMD_CANCEL.equals(command)) {
            int index = args.get("index").getInteger();
            cancelCraft(index);
            return true;
        }
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETCRAFTABLE.equals(command)) {
            return getCraftableItems();
        } else if (CMD_GETREQUESTS.equals(command)) {
            return getRequests();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETCRAFTABLE.equals(command)) {
            GuiCraftingStation.storeCraftableForClient(list);
            return true;
        } else if (CLIENTCMD_GETREQUESTS.equals(command)) {
            GuiCraftingStation.storeRequestsForClient(list);
            return true;
        }
        return false;
    }
}

package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationContainer.CONTAINER_FACTORY;

public class CraftingStationTileEntity extends GenericTileEntity {

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final NoDirectionItemHander items = createItemHandler();

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<CraftingStationContainer>("Crafter")
            .containerSupplier((windowId,player) -> new CraftingStationContainer(windowId, CONTAINER_FACTORY.get(), getBlockPos(), CraftingStationTileEntity.this))
            .itemHandler(() -> items));

    private final List<BlockPos> processorList = new ArrayList<>();
    private int currentTicket = 0;
    private List<CraftingRequest> activeCraftingRequests = new ArrayList<>();
    private int cleanupCounter = 50;

    public CraftingStationTileEntity() {
        super(CraftingStationModule.CRAFTING_STATION_TILE.get());
    }

    public void registerProcessor(BlockPos pos) {
        if (!processorList.contains(pos)) {
            processorList.add(pos);
        }
        setChanged();
    }


    // @todo optimize finding requests on craftid!!!!

    public ItemStack getCraftResult(String craftId) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (craftId.equals(request.getTicket())) {
                return request.getStack();
            }
        }
        return ItemStack.EMPTY;
    }

    private Pair<ProcessorTileEntity, ItemStack> findCraftableItem(int index) {
        for (BlockPos p : processorList) {
            TileEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                ItemStackList items = ItemStackList.create();
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

    public ItemStack craftOk(ProcessorTileEntity processor, String ticket, ItemStack stack) {
        CraftingRequest foundRequest = null;
        for (CraftingRequest request : activeCraftingRequests) {
            if (ticket.equals(request.getTicket())) {
                foundRequest = request;
                break;
            }
        }
        if (foundRequest != null) {
            setChanged();
            foundRequest.decrTodo();
            if (foundRequest.getTodo() <= 0) {
                foundRequest.setOk(System.currentTimeMillis() + 1000);
            } else {
                processor.fireCraftEvent(ticket, foundRequest.getStack());
            }
            if (!stack.isEmpty()) {
                Inventory inventory = getInventoryFromTicket(ticket);
                if (inventory != null) {
                    return processor.getItemHandlerAt(inventory)
                            .map(handlerAt -> ItemHandlerHelper.insertItem(handlerAt, stack, false))
                            .orElseThrow(() -> new ProgException(ExceptionType.EXCEPT_INVALIDINVENTORY));
                 } else {
                    getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                            .map(h -> ItemHandlerHelper.insertItem(h, stack, false))
                            .orElse(ItemStack.EMPTY);
                }
            }
        }
        return stack;
    }

    public void craftFail(String ticket) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (ticket.equals(request.getTicket())) {
                request.setFailed(System.currentTimeMillis() + 2000);
                setChanged();
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
        String ticket = getNewTicket(null);
        ItemStack stack = pair.getValue();
        int count = (amount + stack.getCount() - 1) / stack.getCount();
        CraftingRequest request = new CraftingRequest(ticket, stack, count);

        if (!checkRequestAmount()) {
            return;
        }
        activeCraftingRequests.add(request);
        pair.getKey().fireCraftEvent(ticket, stack);

        cleanupCounter--;
        if (cleanupCounter <= 0) {
            cleanupCounter = 50;
            cleanupStaleRequests();
        }
    }

    private boolean checkRequestAmount() {
        if (activeCraftingRequests.size() >= Config.maxCraftRequests.get()) {
            cleanupCounter = 50;
            cleanupStaleRequests();
            if (activeCraftingRequests.size() >= Config.maxCraftRequests.get()) {
                // To many requests
                return false;
            }
        }
        return true;
    }


    public boolean isRequested(Ingredient item) {
        for (CraftingRequest request : activeCraftingRequests) {
            long failed = request.getFailed();
            long ok = request.getOk();
            if ((failed == -1) && (ok == -1)) {
                if (item.test(request.getStack())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean request(@Nonnull Ingredient item, @Nullable Inventory destination) {
        for (BlockPos p : processorList) {
            TileEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                ItemStackList items = ItemStackList.create();
                processor.getCraftableItems(items);
                for (ItemStack i : items) {
                    if (item.test(i)) {
                        String ticket = getNewTicket(destination);
                        if (!checkRequestAmount()) {
                            return false;
                        }
                        activeCraftingRequests.add(new CraftingRequest(ticket, i, 1));
                        processor.fireCraftEvent(ticket, i);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String getNewTicket(@Nullable Inventory destInv) {
        currentTicket++;
        setChanged();
        if (destInv != null) {
            return destInv.serialize() + "#" + currentTicket;
        } else {
            return BlockPosTools.toString(worldPosition) + ":" + currentTicket;
        }
    }

    /// Returns null if the ticket represents a crafting station instead
    @Nullable
    private Inventory getInventoryFromTicket(String ticket) {
        if (ticket.startsWith("#")) {
            return Inventory.deserialize(ticket);
        }
        return null;
    }

    /// Returns null if the ticket represents an inventory
    @Nullable
    private BlockPos getPositionFromTicket(String ticket) {
        if (ticket.startsWith("#")) {
            return null;
        }
        String[] splitted = StringUtils.split(ticket, ';');
        String[] poss = StringUtils.split(splitted[0], ',');
        return new BlockPos(Integer.parseInt(poss[0]), Integer.parseInt(poss[1]), Integer.parseInt(poss[2]));
    }

    public ItemStackList getCraftableItems() {
        ItemStackList items = ItemStackList.create();
        for (BlockPos p : processorList) {
            TileEntity te = level.getBlockEntity(p);
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readProcessorList(tagCompound);
        readRequests(tagCompound);
    }

    @Override
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        currentTicket = info.getInt("craftId");
    }

    private void readRequests(CompoundNBT tagCompound) {
        ListNBT list = tagCompound.getList("requests", Constants.NBT.TAG_COMPOUND);
        activeCraftingRequests.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT requestTag = list.getCompound(i);
            String craftId = requestTag.getString("craftId");
            ItemStack stack = ItemStack.of(requestTag.getCompound("stack"));
            int count = requestTag.getInt("count");
            CraftingRequest request = new CraftingRequest(craftId, stack, count);
            request.setFailed(requestTag.getLong("failed"));
            request.setOk(requestTag.getLong("ok"));
            activeCraftingRequests.add(request);
        }
    }

    private void readProcessorList(CompoundNBT tagCompound) {
        ListNBT list = tagCompound.getList("processors", Constants.NBT.TAG_COMPOUND);
        processorList.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tag = list.getCompound(i);
            processorList.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tagCompound) {
        super.save(tagCompound);
        writeProcessorList(tagCompound);
        writeRequests(tagCompound);
        return tagCompound;
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("craftId", currentTicket);
    }

    private void writeRequests(CompoundNBT tagCompound) {
        ListNBT list = new ListNBT();
        for (CraftingRequest request : activeCraftingRequests) {
            CompoundNBT requestTag = new CompoundNBT();
            requestTag.putString("craftId", request.getTicket());
            CompoundNBT stackNbt = new CompoundNBT();
            request.getStack().save(stackNbt);
            requestTag.put("stack", stackNbt);
            requestTag.putInt("count", request.getTodo());
            requestTag.putLong("failed", request.getFailed());
            requestTag.putLong("ok", request.getOk());
            list.add(requestTag);
        }

        tagCompound.put("requests", list);
    }

    private void writeProcessorList(CompoundNBT tagCompound) {
        ListNBT list = new ListNBT();
        for (BlockPos p : processorList) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("x", p.getX());
            tag.putInt("y", p.getY());
            tag.putInt("z", p.getZ());
            list.add(tag);
        }
        tagCompound.put("processors", list);
    }

    private int findItem(String itemName, String nbtString) {
        int index = 0;
        for (BlockPos p : processorList) {
            TileEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity) {
                ProcessorTileEntity processor = (ProcessorTileEntity) te;
                ItemStackList items = ItemStackList.create();
                processor.getCraftableItems(items);
                for (ItemStack item : items) {
                    if (itemName.equals(item.getItem().getRegistryName().toString())) {
                        if (item.hasTag()) {
                            if (nbtString.equalsIgnoreCase(item.serializeNBT().toString())) {
                                return index;
                            }
                        } else {
                            return index;
                        }
                    }
                    index++;
                }
            }
        }
        return -1;
    }


    public static final Key<String> PARAM_ITEMNAME = new Key<>("itemname", Type.STRING);
    public static final Key<String> PARAM_NBT = new Key<>("nbt", Type.STRING);
    public static final Key<Integer> PARAM_AMOUNT = new Key<>("amount", Type.INTEGER);
    @ServerCommand
    public static final Command<?> CMD_REQUEST = Command.<CraftingStationTileEntity>create("station.request",
            (te, player, params) -> {
                String itemName = params.get(PARAM_ITEMNAME);
                String nbtString = params.get(PARAM_NBT);
                int index = te.findItem(itemName, nbtString);
                if (index == -1) {
                    return;
                }
                te.startCraft(index, params.get(PARAM_AMOUNT));
            });

    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    @ServerCommand
    public static final Command<?> CMD_CANCEL = Command.<CraftingStationTileEntity>create("station.cancel",
            (te, player, params) -> te.cancelCraft(params.get(PARAM_INDEX)));

    @ServerCommand
    public static final ListCommand<?, ?> CMD_GETCRAFTABLE = ListCommand.<CraftingStationTileEntity, ItemStack>create("rftoolscontrol.station.getCraftable",
            (te, player, params) -> te.getCraftableItems(),
            (te, player, params, list) -> GuiCraftingStation.storeCraftableForClient(list));

    @ServerCommand
    public static final ListCommand<?, ?> CMD_GETREQUESTS = ListCommand.<CraftingStationTileEntity, CraftingRequest>create("rftoolscontrol.station.getRequests",
            (te, player, params) -> te.getRequests(),
            (te, player, params, list) -> GuiCraftingStation.storeRequestsForClient(list));

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(CraftingStationTileEntity.this, CONTAINER_FACTORY.get());
    }
}

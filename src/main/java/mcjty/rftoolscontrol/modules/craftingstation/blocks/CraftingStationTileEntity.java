package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.Tools;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule.CRAFTING_STATION_CONTAINER;

public class CraftingStationTileEntity extends GenericTileEntity {

    public static final int SLOT_OUTPUT = 0;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(9)
            .box(generic(), SLOT_OUTPUT, 6, 157, 3, 3)
            .playerSlots(66, 157));

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final GenericItemHandler items = GenericItemHandler.basic(this, CONTAINER_FACTORY);

    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<MenuProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Crafter")
            .containerSupplier(container(CRAFTING_STATION_CONTAINER, CONTAINER_FACTORY, this))
            .itemHandler(() -> items)
            .setupSync(this));

    private final List<BlockPos> processorList = new ArrayList<>();
    private int currentTicket = 0;
    private List<CraftingRequest> activeCraftingRequests = new ArrayList<>();
    private int cleanupCounter = 50;

    public CraftingStationTileEntity(BlockPos pos, BlockState state) {
        super(CraftingStationModule.CRAFTING_STATION_TILE.get(), pos, state);
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
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity processor) {
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
                    ItemStack finalStack = stack;
                    return processor.getItemHandlerAt(inventory)
                            .map(handlerAt -> ItemHandlerHelper.insertItem(handlerAt, finalStack, false))
                            .orElseThrow(() -> new ProgException(ExceptionType.EXCEPT_INVALIDINVENTORY));
                 } else {
                    stack = ItemHandlerHelper.insertItem(items, stack, false);
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
        } catch (Exception ignored) {

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
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity processor) {
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
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity processor) {
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        readProcessorList(tagCompound);
        readRequests(tagCompound);
    }

    @Override
    protected void loadInfo(CompoundTag tagCompound) {
        super.loadInfo(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        currentTicket = info.getInt("craftId");
    }

    private void readRequests(CompoundTag tagCompound) {
        ListTag list = tagCompound.getList("requests", Tag.TAG_COMPOUND);
        activeCraftingRequests.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag requestTag = list.getCompound(i);
            String craftId = requestTag.getString("craftId");
            ItemStack stack = ItemStack.of(requestTag.getCompound("stack"));
            int count = requestTag.getInt("count");
            CraftingRequest request = new CraftingRequest(craftId, stack, count);
            request.setFailed(requestTag.getLong("failed"));
            request.setOk(requestTag.getLong("ok"));
            activeCraftingRequests.add(request);
        }
    }

    private void readProcessorList(CompoundTag tagCompound) {
        ListTag list = tagCompound.getList("processors", Tag.TAG_COMPOUND);
        processorList.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            processorList.add(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        writeProcessorList(tagCompound);
        writeRequests(tagCompound);
    }

    @Override
    protected void saveInfo(CompoundTag tagCompound) {
        super.saveInfo(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("craftId", currentTicket);
    }

    private void writeRequests(CompoundTag tagCompound) {
        ListTag list = new ListTag();
        for (CraftingRequest request : activeCraftingRequests) {
            CompoundTag requestTag = new CompoundTag();
            requestTag.putString("craftId", request.getTicket());
            CompoundTag stackNbt = new CompoundTag();
            request.getStack().save(stackNbt);
            requestTag.put("stack", stackNbt);
            requestTag.putInt("count", request.getTodo());
            requestTag.putLong("failed", request.getFailed());
            requestTag.putLong("ok", request.getOk());
            list.add(requestTag);
        }

        tagCompound.put("requests", list);
    }

    private void writeProcessorList(CompoundTag tagCompound) {
        ListTag list = new ListTag();
        for (BlockPos p : processorList) {
            CompoundTag tag = new CompoundTag();
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
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity processor) {
                ItemStackList items = ItemStackList.create();
                processor.getCraftableItems(items);
                for (ItemStack item : items) {
                    if (itemName.equals(Tools.getId(item).toString())) {
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

    @ServerCommand(type = ItemStack.class)
    public static final ListCommand<?, ?> CMD_GETCRAFTABLE = ListCommand.<CraftingStationTileEntity, ItemStack>create("rftoolscontrol.station.getCraftable",
            (te, player, params) -> te.getCraftableItems(),
            (te, player, params, list) -> GuiCraftingStation.storeCraftableForClient(list));

    @ServerCommand(type = CraftingRequest.class, serializer = CraftingRequest.Serializer.class)
    public static final ListCommand<?, ?> CMD_GETREQUESTS = ListCommand.<CraftingStationTileEntity, CraftingRequest>create("rftoolscontrol.station.getRequests",
            (te, player, params) -> te.getRequests(),
            (te, player, params, list) -> GuiCraftingStation.storeRequestsForClient(list));

}

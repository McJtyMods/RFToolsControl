package mcjty.rftoolscontrol.modules.craftingstation.blocks;

import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.container.IItemHandler;
import mcjty.lib.container.ItemHandlerHelper;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.Lazy;
import mcjty.rftoolsbase.api.control.parameters.Inventory;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.craftingstation.data.CraftingStationData;
import mcjty.rftoolscontrol.modules.craftingstation.util.CraftingRequest;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.lib.api.container.DefaultContainerProvider.container;
import static mcjty.lib.container.SlotDefinition.generic;
import static mcjty.lib.setup.Registration.ITEM_INVENTORY;
import static mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule.CRAFTING_STATION_CONTAINER;

public class CraftingStationTileEntity extends GenericTileEntity {

    public static final int SLOT_OUTPUT = 0;

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(9)
            .box(generic(), SLOT_OUTPUT, 6, 157, 3, 3)
            .playerSlots(66, 157));

    private final GenericItemHandler items = GenericItemHandler.basic(this, CONTAINER_FACTORY);
    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<CraftingStationTileEntity, GenericItemHandler> ITEM_CAP = te -> te.items;

    @Cap(type = CapType.CONTAINER)
    private static final Function<CraftingStationTileEntity, MenuProvider> screenHandler = tile -> new DefaultContainerProvider<GenericContainer>("Crafter")
            .containerSupplier(container(CRAFTING_STATION_CONTAINER, CONTAINER_FACTORY, tile))
            .itemHandler(() -> tile.items)
            .setupSync(tile);

    private final List<BlockPos> processorList = new ArrayList<>();
    private List<CraftingRequest> activeCraftingRequests = new ArrayList<>();
    private int cleanupCounter = 50;

    public CraftingStationTileEntity(BlockPos pos, BlockState state) {
        super(CraftingStationModule.CRAFTING_STATION.be().get(), pos, state);
    }

    public void registerProcessor(BlockPos pos) {
        if (!processorList.contains(pos)) {
            processorList.add(pos);
        }
        setChanged();
    }


    public ItemStack getCraftResult(String craftId) {
        CraftingRequest request = findRequest(craftId);
        return request == null ? ItemStack.EMPTY : request.getStack();
    }

    @Nullable
    private CraftingRequest findRequest(String ticket) {
        for (CraftingRequest request : activeCraftingRequests) {
            if (ticket.equals(request.getTicket())) {
                return request;
            }
        }
        return null;
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
        CraftingRequest foundRequest = findRequest(ticket);
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
                    IItemHandler inv = processor.getItemHandlerAt(inventory);
                    if (inv != null) {
                        stack = ItemHandlerHelper.insertItem(inv, stack, false);
                    } else {
                        throw new ProgException(ExceptionType.EXCEPT_INVALIDINVENTORY);
                    }
                 } else {
                    stack = ItemHandlerHelper.insertItem(items, stack, false);
                }
            }
        }
        return stack;
    }

    public void craftFail(String ticket) {
        CraftingRequest request = findRequest(ticket);
        if (request != null) {
            request.setFailed(System.currentTimeMillis() + 2000);
            failDependents(request);
            setChanged();
        }
    }

    private void cancelCraft(int index) {
        if (index < 0 || index >= activeCraftingRequests.size()) {
            return;
        }
        CraftingRequest request = activeCraftingRequests.remove(index);
        if (request.isActive()) {
            failDependents(request);
        }
        setChanged();
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
            if (request.isActive() && item.test(request.getStack())) {
                return true;
            }
        }
        return false;
    }

    public boolean request(@Nonnull Ingredient item, @Nullable Inventory destination) {
        return request(item, destination, null);
    }

    public boolean hasFailedDependency(String ticket) {
        CraftingRequest request = findRequest(ticket);
        return request != null && request.hasFailedDependency();
    }

    public boolean requestDependency(String parentTicket, Ingredient item, Inventory destination) {
        CraftingRequest parent = findRequest(parentTicket);
        if (parent == null || !parent.isActive() || parent.hasFailedDependency()) {
            return false;
        }

        Set<String> ancestors = collectAncestorTickets(parent);
        boolean matchesAncestor = false;
        for (CraftingRequest request : activeCraftingRequests) {
            if (!request.isActive() || !item.test(request.getStack())) {
                continue;
            }
            if (ancestors.contains(request.getTicket())) {
                matchesAncestor = true;
            } else if (destination.equals(getInventoryFromTicket(request.getTicket()))) {
                if (request.getDependentTickets().add(parentTicket)) {
                    setChanged();
                }
                return true;
            }
        }
        return !matchesAncestor && request(item, destination, parentTicket);
    }

    private Set<String> collectAncestorTickets(CraftingRequest request) {
        Set<String> ancestors = new HashSet<>();
        ArrayDeque<CraftingRequest> pending = new ArrayDeque<>();
        pending.add(request);
        while (!pending.isEmpty()) {
            CraftingRequest current = pending.removeFirst();
            if (ancestors.add(current.getTicket())) {
                for (String ticket : current.getDependentTickets()) {
                    CraftingRequest dependent = findRequest(ticket);
                    if (dependent != null && dependent.isActive()) {
                        pending.add(dependent);
                    }
                }
            }
        }
        return ancestors;
    }

    private void failDependents(CraftingRequest request) {
        for (String ticket : request.getDependentTickets()) {
            CraftingRequest dependent = findRequest(ticket);
            if (dependent != null && dependent.isActive()) {
                dependent.setDependencyFailed(true);
            }
        }
    }

    private boolean request(Ingredient item, @Nullable Inventory destination, @Nullable String dependentTicket) {
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
                        CraftingRequest request = new CraftingRequest(ticket, i, 1);
                        if (dependentTicket != null) {
                            request.getDependentTickets().add(dependentTicket);
                        }
                        activeCraftingRequests.add(request);
                        processor.fireCraftEvent(ticket, i);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String getNewTicket(@Nullable Inventory destInv) {
        int craftId = nextCraftId();
        if (destInv != null) {
            return destInv.serialize() + "#" + craftId;
        } else {
            return BlockPosTools.toString(worldPosition) + ":" + craftId;
        }
    }

    private int nextCraftId() {
        CraftingStationData data = getData(CraftingStationModule.CRAFTING_STATION_DATA);
        int next = data.craftId() + 1;
        setData(CraftingStationModule.CRAFTING_STATION_DATA, data.withCraftId(next));
        return next;
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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        HolderLookup.Provider provider = input.lookup();
        readProcessorList(tag);
        readRequests(tag, provider);
        items.load(tag, "items", provider);
    }

    private void readRequests(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = tagCompound.getListOrEmpty("requests");
        activeCraftingRequests.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag requestTag = list.getCompoundOrEmpty(i);
            String craftId = requestTag.getStringOr("craftId", "");
            ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), requestTag.getCompoundOrEmpty("stack")).result().orElse(ItemStack.EMPTY);
            int count = requestTag.getIntOr("count", 0);
            CraftingRequest request = new CraftingRequest(craftId, stack, count);
            request.setFailed(requestTag.getLongOr("failed", 0L));
            request.setOk(requestTag.getLongOr("ok", 0L));
            request.setDependencyFailed(requestTag.getBooleanOr("dependencyFailed", false));
            ListTag dependents = requestTag.getListOrEmpty("dependents");
            for (int j = 0; j < dependents.size(); j++) {
                dependents.getString(j).ifPresent(request.getDependentTickets()::add);
            }
            activeCraftingRequests.add(request);
        }
    }

    private void readProcessorList(CompoundTag tagCompound) {
        ListTag list = tagCompound.getListOrEmpty("processors");
        processorList.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompoundOrEmpty(i);
            processorList.add(new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)));
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        HolderLookup.Provider provider = level == null ? RegistryAccess.EMPTY : level.registryAccess();
        writeProcessorList(tag);
        writeRequests(tag, provider);
        items.save(tag, "items", provider);
        output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
    }

    private void writeRequests(CompoundTag tagCompound, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (CraftingRequest request : activeCraftingRequests) {
            CompoundTag requestTag = new CompoundTag();
            requestTag.putString("craftId", request.getTicket());
            requestTag.put("stack", ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), request.getStack()).getOrThrow());
            requestTag.putInt("count", request.getTodo());
            requestTag.putLong("failed", request.getFailed());
            requestTag.putLong("ok", request.getOk());
            if (request.hasFailedDependency()) {
                requestTag.putBoolean("dependencyFailed", true);
            }
            if (!request.getDependentTickets().isEmpty()) {
                ListTag dependents = new ListTag();
                for (String ticket : request.getDependentTickets()) {
                    dependents.add(StringTag.valueOf(ticket));
                }
                requestTag.put("dependents", dependents);
            }
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

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        CraftingStationData data = input.get(CraftingStationModule.ITEM_CRAFTING_STATION_DATA.get());
        if (data != null) {
            setData(CraftingStationModule.CRAFTING_STATION_DATA, data);
        }
        items.applyImplicitComponents(input.get(ITEM_INVENTORY.get()));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(CraftingStationModule.ITEM_CRAFTING_STATION_DATA.get(), getData(CraftingStationModule.CRAFTING_STATION_DATA));
        items.collectImplicitComponents(builder);
    }

    private int findItem(ItemStack stack) {
        int index = 0;
        for (BlockPos p : processorList) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ProcessorTileEntity processor) {
                ItemStackList items = ItemStackList.create();
                processor.getCraftableItems(items);
                for (ItemStack item : items) {
                    if (ItemStack.isSameItemSameComponents(item, stack)) {
                        return index;
                    }
                    index++;
                }
            }
        }
        return -1;
    }


    public static final Key<ItemStack> PARAM_ITEM = new Key<>("item", Type.ITEMSTACK);
    public static final Key<Integer> PARAM_AMOUNT = new Key<>("amount", Type.INTEGER);
    @ServerCommand
    public static final Command<?> CMD_REQUEST = Command.<CraftingStationTileEntity>create("station.request",
            (te, player, params) -> {
                ItemStack stack = params.get(PARAM_ITEM);
                int index = te.findItem(stack);
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

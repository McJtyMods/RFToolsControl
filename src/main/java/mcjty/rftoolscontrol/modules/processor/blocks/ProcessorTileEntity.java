package mcjty.rftoolscontrol.modules.processor.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.GuiValue;
import mcjty.lib.bindings.Value;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ListCommand;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.setup.Registration;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.control.code.ICompiledOpcode;
import mcjty.rftoolsbase.api.control.code.IOpcodeRunnable;
import mcjty.rftoolsbase.api.control.machines.IProcessor;
import mcjty.rftoolsbase.api.control.machines.IProgram;
import mcjty.rftoolsbase.api.control.parameters.*;
import mcjty.rftoolsbase.api.machineinfo.CapabilityMachineInformation;
import mcjty.rftoolsbase.api.machineinfo.IMachineInformation;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolscontrol.compat.RFToolsStuff;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.data.*;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.modules.processor.logic.LogicInventoryTools;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterSerializer;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import mcjty.rftoolscontrol.modules.processor.logic.running.CpuCore;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.ProgException;
import mcjty.rftoolscontrol.modules.processor.logic.running.RunningProgram;
import mcjty.rftoolscontrol.modules.processor.network.PacketGetFluids;
import mcjty.rftoolscontrol.modules.processor.util.*;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOp;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOpBox;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOpLine;
import mcjty.rftoolscontrol.modules.processor.vectorart.GfxOpText;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.various.blocks.NodeTileEntity;
import mcjty.rftoolscontrol.modules.various.blocks.WorkbenchTileEntity;
import mcjty.rftoolscontrol.modules.various.data.TokenData;
import mcjty.rftoolscontrol.modules.various.items.TokenItem;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.MAXCAPACITY;
import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.TANKS;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer.CONTAINER_FACTORY;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer.SLOT_EXPANSION;
import static mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType.*;

public class ProcessorTileEntity extends TickingTileEntity implements IProcessor {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3 * 8;
    public static final int EXPANSION_SLOTS = 4 * 4;
    public static final int MAXVARS = 32;
    public static final int MAXFLUIDVARS = 4 * 6;

    private static final BiFunction<ParameterType, Object, ItemStack> CONVERTOR_ITEM = TypeConverters::convertToItem;
    private static final BiFunction<ParameterType, Object, FluidStack> CONVERTOR_FLUID = TypeConverters::convertToFluid;
    private static final BiFunction<ParameterType, Object, BlockSide> CONVERTOR_SIDE = TypeConverters::convertToSide;
    private static final BiFunction<ParameterType, Object, Inventory> CONVERTOR_INVENTORY = TypeConverters::convertToInventory;
    private static final BiFunction<ParameterType, Object, Tuple> CONVERTOR_TUPLE = TypeConverters::convertToTuple;
    private static final BiFunction<ParameterType, Object, List<Parameter>> CONVERTOR_VECTOR = TypeConverters::convertToVector;
    private static final BiFunction<ParameterType, Object, Integer> CONVERTOR_INTEGER = TypeConverters::convertToInteger;
    private static final BiFunction<ParameterType, Object, Long> CONVERTOR_LONG = TypeConverters::convertToLong;
    private static final BiFunction<ParameterType, Object, String> CONVERTOR_STRING = TypeConverters::convertToString;
    private static final BiFunction<ParameterType, Object, Boolean> CONVERTOR_BOOL = TypeConverters::convertToBool;
    private static final BiFunction<ParameterType, Object, Number> CONVERTOR_NUMBER = TypeConverters::convertToNumber;

    private final GenericItemHandler items = GenericItemHandler.create(this, CONTAINER_FACTORY)
            .itemValid((slot, stack) -> {
                if (isExpansionSlot(slot)) {
                    return isValidExpansionItem(stack.getItem());
                } else if (isCardSlot(slot)) {
                    return stack.getItem() == VariousModule.PROGRAM_CARD.get();
                }
                return true;
            })
            .onUpdate((slot, stack) -> onUpdateCard(slot))
            .build();
    @Cap(type = CapType.ITEMS_AUTOMATION)
    private static final Function<ProcessorTileEntity, GenericItemHandler> ITEM_CAP = tile -> tile.items;

    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, Config.processorMaxenergy.get(), Config.processorReceivepertick.get());
    @Cap(type = CapType.ENERGY)
    private static final Function<ProcessorTileEntity, GenericEnergyStorage> ENERGY_CAP = tile -> tile.energyStorage;

    @Cap(type = CapType.CONTAINER)
    private static final Function<ProcessorTileEntity, MenuProvider> SCREEN_CAP = tile -> new DefaultContainerProvider<ProcessorContainer>("Processor")
            .containerSupplier((windowId, player) -> ProcessorContainer.create(windowId, tile.getBlockPos(), tile, player))
            .itemHandler(() -> tile.items)
            .energyHandler(() -> tile.energyStorage)
            .data(ProcessorModule.PROCESSOR_SETTINGS_DATA, ProcessorSettingsData.STREAM_CODEC, ProcessorSettingsData.CODEC)
            .setupSync(tile);

    private final List<CpuCore> cpuCores = new ArrayList<>(); // MARK: covered by ProcessorCoreData.cores

    public static final int HUD_OFF = 0;

    public static BiFunction<ParameterType, Object, List<Parameter>> getConvertorVector() {
        return CONVERTOR_VECTOR;
    }

    public static final int HUD_LOG = 1;
    public static final int HUD_DB = 2;
    public static final int HUD_GFX = 3;

    // GUI bindings for settings stored in ProcessorSettingsData
    @GuiValue
    public static final Value<ProcessorTileEntity, Integer> VALUE_HUD = Value.create("hud", Type.INTEGER, ProcessorTileEntity::getShowHud, ProcessorTileEntity::setShowHud);
    @GuiValue
    public static final Value<ProcessorTileEntity, Boolean> VALUE_EXCLUSIVE = Value.create("exclusive", Type.BOOLEAN, ProcessorTileEntity::isExclusive, ProcessorTileEntity::setExclusive);
 
    // If true some cards might need compiling
    private boolean cardsDirty = true;
    // If true some cpu cores need updating
    private boolean coresDirty = true;

    private int maxVars = -1;   // If -1 we need updating
    private int hasNetworkCard = -1;
    private int storageCard = -2;   // -2 is unknown
    private boolean hasGraphicsCard = false;
    private final Cached<List<Predicate<ItemStack>>> filterCaches = Cached.of(this::getFilterCaches);

    private List<String> orderedOps = null;

    // Client-side only: for the HUD
    private final List<GfxOp> clientGfxOps = new ArrayList<>();


    private String channel = ""; // MARK: covered by ProcessorCoreData.channel
    private final Map<String, BlockPos> networkNodes = new HashMap<>(); // MARK: covered by ProcessorExtraData.networkNodes
    private final Set<BlockPos> craftingStations = new HashSet<>();

    // Bitmask for all six sides
    private int prevIn = 0;
    private final int[] powerOut = new int[]{0, 0, 0, 0, 0, 0};

    private int tickCount = 0; // MARK: covered by ProcessorCoreData.tickCount

    private final Parameter[] variables = new Parameter[MAXVARS]; // MARK: covered by ProcessorCoreData.variables
    private final WatchInfo[] watchInfos = new WatchInfo[MAXVARS]; // MARK: covered by ProcessorCoreData.watchInfos
    private int fluidSlotsAvailable = -1;    // Bitmask indexed by side (6 bits), -1 means unset

    private final CardInfo[] cardInfo = new CardInfo[CARD_SLOTS]; // MARK: covered by ProcessorCardInfoData.infos

    private Queue<QueuedEvent> eventQueue = new ArrayDeque<>();        // Integer == card index // MARK: covered by ProcessorEventData.queuedEvents

    private final List<WaitForItem> waitingForItems = new ArrayList<>();

    private final Queue<String> logMessages = new ArrayDeque<>(); // MARK: covered by ProcessorExtraData.logMessages

    // Client side: log from server
    public long clientTime = 0;
    private List<String> clientLog = new ArrayList<>();
    private List<String> clientDebugLog = new ArrayList<>();

    // Card index, Opcode index
    private Set<Pair<Integer, Integer>> runningEvents = new HashSet<>(); // MARK: covered by ProcessorEventData.runningEvents

    private final Set<String> locks = new HashSet<>(); // MARK: covered by ProcessorCoreData.locks

    // If set this is a dummy tile entity
    private ResourceKey<Level> dummyType = null;


    public ProcessorTileEntity(BlockPos pos, BlockState state) {
        super(ProcessorModule.PROCESSOR.be().get(), pos, state);
//        super(ConfigSetup.processorMaxenergy.get(), ConfigSetup.processorReceivepertick.get());
        for (int i = 0; i < cardInfo.length; i++) {
            cardInfo[i] = new CardInfo();
        }
        for (int i = 0; i < MAXVARS; i++) {
            variables[i] = null;
            watchInfos[i] = null;
        }
        fluidSlotsAvailable = -1;
    }

    // Used for a dummy tile entity (tablet usage)
    public ProcessorTileEntity(ResourceKey<Level> type, BlockPos pos) {
        this(pos, null);
        dummyType = type;
    }

    // Getters for processor data attachments/components
    public ProcessorCoreData getCoreData() {
        return getData(ProcessorModule.PROCESSOR_CORE_DATA.get());
    }

    public ProcessorCardInfoData getCardInfoData() {
        return getData(ProcessorModule.PROCESSOR_CARD_INFO_DATA.get());
    }

    public ProcessorGraphicsOperationsData getGraphicsOperationsData() {
        return getData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get());
    }

    public ProcessorEventData getEventData() {
        return getData(ProcessorModule.PROCESSOR_EVENTS_DATA.get());
    }

    public ProcessorCraftingData getCraftingData() {
        return getData(ProcessorModule.PROCESSOR_CRAFTING_DATA.get());
    }

    public ProcessorExtraData getExtraData() {
        return getData(ProcessorModule.PROCESSOR_EXTRA_DATA.get());
    }

    public ProcessorSettingsData getSettingsData() {
        return getData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get());
    }


    // Return true if this is a dummy tile entity for the tablet
    public boolean isDummy() {
        return dummyType != null;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        if (dummyType != null) {
            return dummyType;
        }
        return super.getDimension();
    }

    public boolean isExclusive() {
        return getSettingsData().exclusive();
    }

    public void setExclusive(boolean exclusive) {
        ProcessorSettingsData data = getSettingsData();
        if (data.exclusive() != exclusive) {
            setData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get(), data.withExclusive(exclusive));
        }
    }

    public Parameter getParameter(int idx) {
        return variables[idx];
    }

    public boolean isFluidSlotAvailable(int idx) {
        int sideIndex = idx / TANKS;
        return (getFluidSlotsAvailable() & (1 << sideIndex)) != 0;
    }

    private BlockPos getAdjacentPosition(@Nonnull BlockSide side) {
        BlockPos p;
        if (side.getNodeName() != null && !side.getNodeName().isEmpty()) {
            p = networkNodes.get(side.getNodeName());
            if (p == null) {
                throw new ProgException(EXCEPT_MISSINGNODE);
            }
            BlockEntity te = level.getBlockEntity(p);
            if (!(te instanceof NodeTileEntity)) {
                throw new ProgException(EXCEPT_MISSINGNODE);
            }
        } else {
            p = worldPosition;
        }
        return p;
    }

    @Override
    public int readRedstoneIn(@Nonnull BlockSide side) {
        Direction facing = side.getSide();
        BlockPos p = getAdjacentPosition(side);
        if (p == null) {
            return 0;
        }
        return level.getSignal(p.relative(facing), facing);
    }

    @Override
    public void setPowerOut(@Nonnull BlockSide side, int amount) {
        Direction facing = side.getSide();
        BlockPos p = getAdjacentPosition(side);
        if (p == null) {
            return;
        }

        if (amount < 0) {
            amount = 0;
        } else if (amount > 15) {
            amount = 15;
        }

        if (p.equals(worldPosition)) {
            powerOut[facing.ordinal()] = amount;
            setChanged();
            level.neighborChanged(this.worldPosition.relative(facing), this.getBlockState().getBlock(), this.worldPosition);
//            getLevel().neighborChanged(this.worldPosition.relative(outputSide), state.getBlock(), this.worldPosition);
        } else {
            NodeTileEntity te = (NodeTileEntity) level.getBlockEntity(p);
            te.setPowerOut(facing, amount);
        }
    }

    public int getPowerOut(Direction side) {
        return powerOut[side.ordinal()];
    }

    @Override
    public void tickServer() {
        process();
        prevIn = powerLevel;
    }

    private void process() {
        tickCount++;

        setChanged();
        updateCores();
        compileCards(level.registryAccess());
        processEventQueue();
        try {
            handleEvents();
        } catch (ProgException e) {
            exception(e.getExceptionType(), null);
        }
        run();
    }

    private void processEventQueue() {
        QueuedEvent queuedEvent = eventQueue.peek();
        if (queuedEvent != null) {
            CompiledEvent compiledEvent = queuedEvent.compiledEvent();
            if (compiledEvent.single() && runningEvents.contains(Pair.of(queuedEvent.cardIndex(), compiledEvent.index()))) {
                return;
            }
            CpuCore core = findAvailableCore(queuedEvent.cardIndex());
            if (core != null) {
                eventQueue.remove();
                RunningProgram program = new RunningProgram(queuedEvent.cardIndex());
                program.startFromEvent(compiledEvent);
                program.setCraftTicket(queuedEvent.ticket());
                program.setLastValue(queuedEvent.parameter());
                core.startProgram(program);
                if (compiledEvent.single()) {
                    runningEvents.add(Pair.of(queuedEvent.cardIndex(), compiledEvent.index()));
                }
            }
        }
    }

    public void getCraftableItems(List<ItemStack> stacks) {
        try {
            for (CardInfo info : cardInfo) {
                CompiledCard compiledCard = info.getCompiledCard();
                if (compiledCard != null) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_CRAFT)) {
                        int index = event.index();
                        CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                        ItemStack stack = evaluateItemParameter(compiledOpcode, null, 0);
                        Inventory inv = evaluateInventoryParameter(compiledOpcode, null, 1);
                        if (!stack.isEmpty() && inv != null) {
                            throw new ProgException(EXCEPT_BADPARAMETERS);
                        }
                        if (stack.isEmpty() && inv == null) {
                            throw new ProgException(EXCEPT_BADPARAMETERS);
                        }
                        if (!stack.isEmpty()) {
                            stacks.add(stack);
                        } else {
                            // Find all crafting cards in the inventory
                            IItemHandler handler = getItemHandlerAt(inv);
                            for (int i = 0; i < handler.getSlots(); i++) {
                                ItemStack s = handler.getStackInSlot(i);
                                if (!s.isEmpty() && s.getItem() == RFToolsStuff.CRAFTING_CARD.get()) {
                                    ItemStack result = CraftingCardItem.getResult(s);
                                    if (!result.isEmpty()) {
                                        stacks.add(result);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ProgException e) {
            exception(e.getExceptionType(), null);
        }
    }

    public void craftOk(IProgram program, @Nullable Integer slot) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        String ticket = program.getCraftTicket();

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        Integer realSlot = info.getRealSlot(slot);
        ItemStack craftedItem = ItemStack.EMPTY;
        if (realSlot != null) {
            craftedItem = ((IItemHandler) items).getStackInSlot(realSlot);
        }

        for (BlockPos p : craftingStations) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity craftingStation) {
                craftedItem = craftingStation.craftOk(this, ticket, craftedItem);
            }
        }

        if (realSlot != null) {
            // Put back what could not be accepted
            items.setStackInSlot(realSlot, craftedItem);
        }
    }

    public void craftFail(IProgram program) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        String ticket = program.getCraftTicket();

        for (BlockPos p : craftingStations) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                craftingStation.craftFail(ticket);
            }
        }
    }

    public boolean pushItemsWorkbench(IProgram program, @Nonnull BlockSide workbench, ItemStack item, int slot1, int slot2) {
        if (item.isEmpty()) {
            item = getCraftResult(program);
        }
        if (item.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }

        BlockEntity te = getTileEntityAt(workbench);
        if (!(te instanceof WorkbenchTileEntity)) {
            throw new ProgException(EXCEPT_NOTAWORKBENCH);
        }
        ItemStack finalItem = item;
        ItemStack card = findCraftingCard(getItemHandlerAt(te, Direction.EAST), finalItem);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }

        if (!CraftingCardItem.fitsGrid(card)) {
            throw new ProgException(EXCEPT_NOTAGRID);
        }

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        IItemHandler itemHandler = items;

        IItemHandler gridHandler = getItemHandlerAt(te, Direction.UP);
        List<Ingredient> ingredients = CraftingCardItem.getIngredientsGrid(card);
        boolean success = true;
        for (int i = 0; i < 9; i++) {
            ItemStack stackInWorkbench = gridHandler.getStackInSlot(i);
            Ingredient stackInIngredient = ingredients.get(i);
            if (!stackInWorkbench.isEmpty() && stackInIngredient == Ingredient.EMPTY) {
                // Can't work. There is already something in the workbench that doesn't belong
                success = false;
            } else if (stackInWorkbench.isEmpty() && stackInIngredient != Ingredient.EMPTY) {
                // Let's see if we can find the needed ingredient
                boolean found = false;
                for (int slot = slot1; slot <= slot2; slot++) {
                    int realSlot = info.getRealSlot(slot);
                    ItemStack localStack = itemHandler.getStackInSlot(realSlot);
                    if (stackInIngredient.test(localStack)) {
                        localStack = itemHandler.extractItem(realSlot, LogicInventoryTools.getCountFromIngredient(stackInIngredient), false);
                        gridHandler.insertItem(i, localStack, false);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    success = false;
                }
            } else if (!stackInWorkbench.isEmpty() && stackInIngredient != Ingredient.EMPTY) {
                // See if the item matches and we have enough
                if (!stackInIngredient.test(stackInWorkbench)) {
                    success = false;
                } else if (LogicInventoryTools.getCountFromIngredient(stackInIngredient) > stackInWorkbench.getCount()) {
                    success = false;
                }
            }
        }

        return success;
    }

    public int pushItemsMulti(IProgram program, @Nullable Inventory inv, int slot1, int slot2, @Nullable Integer extSlot) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int e = 0;
        if (extSlot != null) {
            e = extSlot;
        }

        int failed = 0;
        for (int slot = slot1; slot <= slot2; slot++) {
            int realSlot = info.getRealSlot(slot);
            ItemStack stack = ((IItemHandler) items).getStackInSlot(realSlot);
            if (!stack.isEmpty()) {
                ItemStack remaining = LogicInventoryTools.insertItem(handler, scanner, stack, extSlot == null ? null : e);
                if (!remaining.isEmpty()) {
                    failed++;
                }
                items.setStackInSlot(realSlot, remaining);
            }
            e++;
        }
        return failed;
    }

    public int countCardIngredients(IProgram program, @Nullable Inventory inv, ItemStack card) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);
        List<Ingredient> ingredients = CraftingCardItem.getIngredients(card);
        List<Ingredient> needed = combineIngredients(ingredients);
        return countPossibleCrafts(scanner, handler, needed);
    }

    public boolean checkIngredients(IProgram program, @Nonnull Inventory cardInv, ItemStack item, int slot1, int slot2) {
        if (item.isEmpty()) {
            item = getCraftResult(program);
        }
        if (item.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }
        ItemStack finalItem = item;
        ItemStack card = findCraftingCard(getItemHandlerAt(cardInv), finalItem);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];

        int slot = slot1;

        List<Ingredient> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2 - slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        int failed = 0;
        for (Ingredient ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            ItemStack localStack = ((IItemHandler) items).getStackInSlot(realSlot);
            if (ingredient != Ingredient.EMPTY) {
//                if (!InventoryTools.areItemsEqual(ingredient, localStack, true, false, oredict)) {
                if (!ingredient.test(localStack)) {
                    return false;
                }
                if (LogicInventoryTools.getCountFromIngredient(ingredient) != localStack.getCount()) {
                    return false;
                }
            } else {
                if (!localStack.isEmpty()) {
                    return false;
                }
            }
            slot++;
        }
        return true;
    }

    public int getIngredientsSmart(IProgram program, Inventory inv, @Nonnull Inventory cardInv,
                                   ItemStack inputStack, int slot1, int slot2, @Nonnull Inventory destInv) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);
        ItemStack item = inputStack;
        if (item.isEmpty()) {
            item = getCraftResult(program);
        }
        if (item.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }

        ItemStack finalItem = item;
        IItemHandler destHandler = getHandlerForInv(destInv);
        if (destHandler == null) {
            throw new ProgException(EXCEPT_INVALIDINVENTORY);
        }
        ;
        ItemStack card = findCraftingCard(getItemHandlerAt(cardInv), finalItem);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];

        List<Ingredient> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2 - slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        List<Ingredient> needed = combineIngredients(ingredients);
        int requested = checkAvailableItemsAndRequestMissing(destInv, scanner, handler, needed);
        if (requested != 0) {
            return requested;
        }
        // We got everything;
        int slot = slot1;

        for (Ingredient ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            if (ingredient != Ingredient.EMPTY) {
                ItemStack stack = LogicInventoryTools.extractItem(handler, scanner, LogicInventoryTools.getCountFromIngredient(ingredient), true, ingredient, null);
                if (!stack.isEmpty()) {
                    ((IItemHandler) items).insertItem(realSlot, stack, false);
                }
            }
            slot++;
        }
        return 0;
    }

    // Check the storage scanner or handler for a list of ingredients. Any missing
    // ingredient is requested if possible. Returns -1 if there were ingredients that
    // could not be requested. Returns 0 if nothing had to be requested and otherwise
    // returns the amount of requested items
    private int checkAvailableItemsAndRequestMissing(Inventory destInv, IStorageScanner scanner, IItemHandler handler, List<Ingredient> needed) {
        int requested = 0;
        for (Ingredient ingredient : needed) {
            if (ingredient != Ingredient.EMPTY) {
                int countFromIngredient = LogicInventoryTools.getCountFromIngredient(ingredient);
                int cnt = LogicInventoryTools.countItem(handler, scanner, ingredient, countFromIngredient);
                if (cnt < countFromIngredient) {
                    requested++;
                    if (!isRequested(ingredient)) {
                        if (!requestCraft(ingredient, destInv)) {
                            // It can't be requested, total failure
                            return -1;
                        }
                    }
                }
            }
        }
        return requested;
    }

    // Check the storage scanner or handler for a list of ingredients and count them
    private int countPossibleCrafts(IStorageScanner scanner, IItemHandler handler, List<Ingredient> needed) {
        int maxPossible = Integer.MAX_VALUE;
        for (Ingredient ingredient : needed) {
            if (ingredient != Ingredient.EMPTY) {
                int cnt = LogicInventoryTools.countItem(handler, scanner, ingredient, -1);
                int possible = cnt / LogicInventoryTools.getCountFromIngredient(ingredient);
                if (possible < maxPossible) {
                    maxPossible = possible;
                }
            }
        }
        return maxPossible;
    }

    // Given a list of ingredients make a combined list where all identical
    // items are grouped
    private List<Ingredient> combineIngredients(List<Ingredient> ingredients) {
        List<Ingredient> needed = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                boolean found = false;
                for (int i = 0 ; i < needed.size() ; i++) {
                    Ingredient neededStack = needed.get(i);
                    if (testIngredientEquality(ingredient, neededStack)) {
                        needed.set(i, combine(ingredient, neededStack));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    needed.add(ingredient);
                }
            }
        }
        return needed;
    }

    /**
     * Try to guess if two ingredients are equivalent
     */
    private boolean testIngredientEquality(Ingredient i1, Ingredient i2) {
        if (i1.isSimple() && i2.isSimple()) {
            // Both ingredients are simple
            ItemStack[] items1 = i1.getItems();
            ItemStack[] items2 = i2.getItems();
            if (items1.length == items2.length) {
                for (int i = 0 ; i < items1.length ; i++) {
                    if (!ItemStack.isSameItemSameComponents(items1[i], items2[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Combine two equivalent ingredients (that return true with testIngredientEquality)
     * into one
     */
    private Ingredient combine(Ingredient i1, Ingredient i2) {
        List<ItemStack> list = new ArrayList<>();
        ItemStack[] items1 = i1.getItems();
        ItemStack[] items2 = i2.getItems();
        if (items1.length == items2.length) {
            for (int i = 0 ; i < items1.length ; i++) {
                ItemStack copy = items1[i].copy();
                copy.grow(items2[i].getCount());
                list.add(copy);
            }
        }
        return Ingredient.of(list.toArray(new ItemStack[list.size()]));
    }

    public int getIngredients(IProgram program, Inventory inv, Inventory cardInv, ItemStack inputStack, int slot1, int slot2) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);
        ItemStack item = inputStack;
        if (item.isEmpty()) {
            item = getCraftResult(program);
        }
        if (item.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }

        ItemStack finalItem = item;
        ItemStack card = findCraftingCard(getItemHandlerAt(cardInv), finalItem);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];

        int slot = slot1;

        List<Ingredient> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2 - slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        int failed = 0;
        for (Ingredient ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            if (ingredient != Ingredient.EMPTY) {
                ItemStack stack = LogicInventoryTools.extractItem(handler, scanner, LogicInventoryTools.getCountFromIngredient(ingredient), true, ingredient, null);
                if (!stack.isEmpty()) {
                    ItemStack remainder = ((IItemHandler) items).insertItem(realSlot, stack, false);
                    if (!remainder.isEmpty()) {
                        LogicInventoryTools.insertItem(handler, scanner, remainder, null);
                    }
                } else {
                    failed++;
                }
            }
            slot++;
        }
        return failed;
    }

    public void craftWait(IProgram program, @Nonnull Inventory inv, ItemStack stack) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        if (stack.isEmpty()) {
            stack = getCraftResult(program);
            if (stack.isEmpty()) {
                throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
            }
        }
        WaitForItem waitForItem = new WaitForItem(program.getCraftTicket(), stack, inv);
        waitingForItems.add(waitForItem);
        setChanged();
    }

    public void craftWaitTimed(IProgram program) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        WaitForItem waitForItem = new WaitForItem(program.getCraftTicket(), ItemStack.EMPTY, null);
        waitingForItems.add(waitForItem);
        setChanged();
    }

    public boolean isRequested(Ingredient ingredient) {
        for (BlockPos p : craftingStations) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                if (craftingStation.isRequested(ingredient)) {
                    return true;
                }
                return false;
            }
        }
        throw new ProgException(EXCEPT_MISSINGCRAFTINGSTATION);

    }

    @Override
    public boolean requestCraft(@Nonnull Ingredient ingredient, @Nullable Inventory inventory) {
        for (BlockPos p : craftingStations) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                if (craftingStation.request(ingredient, inventory)) {
                    return true;
                }
                return false;
            }
        }
        throw new ProgException(EXCEPT_MISSINGCRAFTINGSTATION);
    }

    public void setCraftTicket(IProgram program, String ticket) {
        ((RunningProgram) program).setCraftTicket(ticket);
    }

    public ItemStack getItemFromCard(IProgram program) {
        Parameter lastValue = (Parameter) program.getLastValue();
        if (lastValue == null) {
            throw new ProgException(EXCEPT_MISSINGLASTVALUE);
        }
        ItemStack itemStack = TypeConverters.convertToItem(lastValue);
        if (itemStack.isEmpty()) {
            throw new ProgException(EXCEPT_NOTANITEM);
        }
        if (itemStack.getItem() instanceof CraftingCardItem) {
            return CraftingCardItem.getResult(itemStack);
        }
        if (itemStack.getItem() instanceof TokenItem) {
            TokenData token = itemStack.get(VariousModule.TOKEN_DATA);
            if (token == null) {
                return ItemStack.EMPTY;
            }
            return TypeConverters.convertToItem(token.parameter());
        }
        return ItemStack.EMPTY;
    }


    @Override
    public ItemStack getCraftResult(IProgram program) {
        if (!program.hasCraftTicket()) {
            return ItemStack.EMPTY;
        }
        for (BlockPos p : craftingStations) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                ItemStack stack = craftingStation.getCraftResult(program.getCraftTicket());
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack findCraftingCard(IProgram program, Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        IItemHandler handler = getHandlerForInv(inventory);
        if (handler == null) {
            throw new ProgException(EXCEPT_INVALIDINVENTORY);
        } else {
            return findCraftingCard(handler, stack);
        }
    }

    private ItemStack findCraftingCard(IItemHandler handler, ItemStack craftResult) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (!s.isEmpty() && s.getItem() == RFToolsStuff.CRAFTING_CARD.get()) {
                ItemStack result = CraftingCardItem.getResult(s);
                if (!result.isEmpty()) {
                    if (LogicInventoryTools.areItemsEqual(result, craftResult, true, true)) {
                        return s;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void fireCraftEvent(String ticket, ItemStack stackToCraft) {
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_CRAFT)) {
                    int index = event.index();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    ItemStack stack = evaluateItemParameter(compiledOpcode, null, 0);
                    Inventory inv = evaluateInventoryParameter(compiledOpcode, null, 1);
                    if (!stack.isEmpty()) {
                        if (ItemStack.isSameItem(stack, stackToCraft)) {
                            runOrQueueEvent(i, event, ticket, null);
                            return;
                        }
                    } else if (inv != null) {
                        ItemStack craftingCard = findCraftingCard(getItemHandlerAt(inv), stackToCraft);
                        if (!craftingCard.isEmpty()) {
                            runOrQueueEvent(i, event, ticket, null);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void handleEvents() {
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                handleEventsRedstoneOn(i, compiledCard);
                handleEventsRedstoneOff(i, compiledCard);
                handleEventsTimer(i, compiledCard);
                handleEventsCraftResume(i, compiledCard);
            }
        }
    }

    private void handleEventsCraftResume(int cardIndex, CompiledCard compiledCard) {
        for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_CRAFTRESUME)) {
            int index = event.index();
            CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
            int ticks = evaluateIntParameter(compiledOpcode, null, 0);
            if (ticks > 0 && tickCount % ticks == 0) {
                if (!waitingForItems.isEmpty()) {
                    WaitForItem found = null;
                    int foundIdx = -1;
                    for (int i = 0; i < waitingForItems.size(); i++) {
                        WaitForItem wfi = waitingForItems.get(i);
                        if (wfi.inventory() == null || wfi.itemStack().isEmpty()) {
                            foundIdx = i;
                            found = wfi;
                            break;
                        } else {
                            int cnt = countItemInHandler(wfi.itemStack(), getItemHandlerAt(wfi.inventory()));
                            if (cnt >= wfi.itemStack().getCount()) {
                                foundIdx = i;
                                found = wfi;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        waitingForItems.remove(foundIdx);
                        runOrQueueEvent(cardIndex, event, found.ticket(), null);
                    }
                }
            }
        }
    }

    private void handleEventsTimer(int i, CompiledCard compiledCard) {
        for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_TIMER)) {
            int index = event.index();
            CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
            int ticks = evaluateIntParameter(compiledOpcode, null, 0);
            if (ticks > 0 && tickCount % ticks == 0) {
                runOrDropEvent(i, event, null, null);
            }
        }
    }

    private void handleEventsRedstoneOff(int i, CompiledCard compiledCard) {
        int redstoneOffMask = prevIn & ~powerLevel;
        if (redstoneOffMask != 0) {
            for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_OFF)) {
                int index = event.index();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                BlockSide side = evaluateSideParameter(compiledOpcode, null, 0);
                if (side == null || !side.hasNodeName()) {
                    Direction facing = side == null ? null : side.getSide();
                    if (facing == null || ((redstoneOffMask >> facing.ordinal()) & 1) == 1) {
                        runOrQueueEvent(i, event, null, null);
                    }
                }
            }
        }
    }

    private void handleEventsRedstoneOn(int i, CompiledCard compiledCard) {
        int redstoneOnMask = powerLevel & ~prevIn;
        if (redstoneOnMask != 0) {
            for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_ON)) {
                int index = event.index();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                BlockSide side = evaluateSideParameter(compiledOpcode, null, 0);
                if (side == null || !side.hasNodeName()) {
                    Direction facing = side == null ? null : side.getSide();
                    if (facing == null || ((redstoneOnMask >> facing.ordinal()) & 1) == 1) {
                        runOrQueueEvent(i, event, null, null);
                    }
                }
            }
        }
    }

    private void handleEventsRedstoneOff(int i, CompiledCard compiledCard, String node, int prevMask, int newMask) {
        int redstoneOffMask = prevMask & ~newMask;
        if (redstoneOffMask != 0) {
            for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_OFF)) {
                int index = event.index();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                BlockSide side = evaluateSideParameter(compiledOpcode, null, 0);
                if (side != null && node.equals(side.getNodeName())) {
                    Direction facing = side.getSide();
                    if (facing == null || ((redstoneOffMask >> facing.ordinal()) & 1) == 1) {
                        runOrQueueEvent(i, event, null, null);
                    }
                }
            }
        }
    }

    private void handleEventsRedstoneOn(int i, CompiledCard compiledCard, String node, int prevMask, int newMask) {
        int redstoneOnMask = newMask & ~prevMask;
        if (redstoneOnMask != 0) {
            for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_ON)) {
                int index = event.index();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                BlockSide side = evaluateSideParameter(compiledOpcode, null, 0);
                if (side != null && node.equals(side.getNodeName())) {
                    Direction facing = side.getSide();
                    if (facing == null || ((redstoneOnMask >> facing.ordinal()) & 1) == 1) {
                        runOrQueueEvent(i, event, null, null);
                    }
                }
            }
        }
    }

    public void clearRunningEvent(int cardIndex, int eventIndex) {
        runningEvents.remove(Pair.of(cardIndex, eventIndex));
    }

    private void runOrDropEvent(int cardIndex, CompiledEvent event, @Nullable String ticket, @Nullable Parameter parameter) {
        if (event.single() && runningEvents.contains(Pair.of(cardIndex, event.index()))) {
            // Already running and single
            return;
        }
        CpuCore core = findAvailableCore(cardIndex);
        if (core == null) {
            // No available core. First we check if this exact event is already
            // in the queue. If so we drop it. Otherwise we add it
            for (QueuedEvent q : eventQueue) {
                if (q.cardIndex() == cardIndex) {
                    if (q.compiledEvent().equals(event)) {
                        // This event is already in the queue. Just drop it
                        return;
                    }
                }
            }
            // We could not find this event in the queue. Schedule it
            queueEvent(cardIndex, event, ticket, parameter);
        } else {
            RunningProgram program = new RunningProgram(cardIndex);
            program.startFromEvent(event);
            program.setCraftTicket(ticket);
            program.setLastValue(parameter);
            core.startProgram(program);
            if (event.single()) {
                runningEvents.add(Pair.of(cardIndex, event.index()));
            }
        }
    }

    private void runOrQueueEvent(int cardIndex, CompiledEvent event, @Nullable String ticket, @Nullable Parameter parameter) {
        if (event.single() && runningEvents.contains(Pair.of(cardIndex, event.index()))) {
            // Already running and single
            queueEvent(cardIndex, event, ticket, parameter);
            return;
        }
        CpuCore core = findAvailableCore(cardIndex);
        if (core == null) {
            // No available core
            queueEvent(cardIndex, event, ticket, parameter);
        } else {
            RunningProgram program = new RunningProgram(cardIndex);
            program.startFromEvent(event);
            program.setCraftTicket(ticket);
            program.setLastValue(parameter);
            core.startProgram(program);
            if (event.single()) {
                runningEvents.add(Pair.of(cardIndex, event.index()));
            }
        }
    }

    private void queueEvent(int cardIndex, CompiledEvent event, @Nullable String ticket, @Nullable Parameter parameter) {
        if (eventQueue.size() >= Config.maxEventQueueSize.get()) {
            // Too many events
            throw new ProgException(ExceptionType.EXCEPT_TOOMANYEVENTS);
        }
        eventQueue.add(new QueuedEvent(cardIndex, event, ticket, parameter));
    }

    @Override
    public int signal(String signal) {
        int cnt = 0;
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_SIGNAL)) {
                    int index = event.index();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    String sig = evaluateStringParameter(compiledOpcode, null, 0);
                    if (signal.equals(sig)) {
                        runOrQueueEvent(i, event, null, null);
                        cnt++;
                    }
                }
            }
        }
        return cnt;
    }

    @Override
    public int signal(Tuple location) {
        int cnt = 0;
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_GFX_SELECT)) {
                    runOrQueueEvent(i, event, null, Parameter.builder()
                            .type(ParameterType.PAR_TUPLE)
                            .value(ParameterValue.constant(location))
                            .build());
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public void receiveMessage(String name, @Nullable Parameter value) {
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_MESSAGE)) {
                    int index = event.index();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    String messageName = evaluateStringParameter(compiledOpcode, null, 0);
                    if (name.equals(messageName)) {
                        runOrQueueEvent(i, event, null, value);
                    }
                }
            }
        }
    }

    private String getStatus(int c) {
        CpuCore core = cpuCores.get(c);
        String db = core.isDebug() ? "[DB] " : "";
        if (core.hasProgram()) {
            RunningProgram program = core.getProgram();
            if (program.getDelay() > 0) {
                return db + "<delayed: " + program.getDelay() + ">";
            } else if (program.getLock() != null) {
                return db + "<locked: " + program.getLock() + ">";
            } else {
                return db + "<busy>";
            }
        } else {
            return db + "<idle>";
        }
    }

    public void listStatus() {
        int n = 0;
        for (CpuCore core : getCpuCores()) {
            log("Core: " + n + " -> " + getStatus(n));
            n++;
        }
        log("Event queue: " + eventQueue.size());
        log("Waiting items: " + waitingForItems.size());
        log("Locks: " + locks.size());

        ProcessorCoreData coreData = getCoreData();
        String lastException = coreData.lastException();
        if (!lastException.isEmpty()) {
            long dt = System.currentTimeMillis() - coreData.lastExceptionTime();
            log("Last: " + ChatFormatting.RED + lastException);
            if (dt > 60000 * 60) {
                log("(" + (dt / (60000 / 60)) + "hours ago)");
            } else if (dt > 60000) {
                log("(" + (dt / 60000) + "min ago)");
            } else if (dt > 1000) {
                log("(" + (dt / 1000) + "sec ago)");
            } else {
                log("(" + dt + "ms ago)");
            }
        }
    }

    public int stopPrograms() {
        int n = 0;
        for (CpuCore core : getCpuCores()) {
            if (core.hasProgram()) {
                n++;
                core.stopProgram();
            }
        }
        locks.clear();
        runningEvents.clear();
        return n;
    }

    public void reset() {
        waitingForItems.clear();
        eventQueue.clear();
        stopPrograms();
        for (Direction facing : Direction.values()) {
            powerOut[facing.ordinal()] = 0;
        }
        for (BlockPos np : networkNodes.values()) {
            BlockEntity te = level.getBlockEntity(np);
            if (te instanceof NodeTileEntity) {
                NodeTileEntity tileEntity = (NodeTileEntity) te;
                for (Direction facing : Direction.values()) {
                    tileEntity.setPowerOut(facing, 0);
                }
            }
        }
        ProcessorGraphicsOperationsData graphicsData = getGraphicsOperationsData();
        if (!graphicsData.operations().isEmpty()) {
            setData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get(), graphicsData.withOperations(Collections.emptyMap()));
        }
        orderedOps = null;
        for (CpuCore core : cpuCores) {
            core.setDebug(false);
        }

        setChanged();
    }

    @Override
    public IOpcodeRunnable.OpcodeResult placeLock(String name) {
        if (testLock(name)) {
            return IOpcodeRunnable.OpcodeResult.HOLD;
        }
        locks.add(name);
        return IOpcodeRunnable.OpcodeResult.POSITIVE;
    }

    @Override
    public void releaseLock(String name) {
        locks.remove(name);
    }

    @Override
    public boolean testLock(String name) {
        return locks.contains(name);
    }

    public void clearLog() {
        logMessages.clear();
        ProcessorCoreData coreData = getCoreData();
        ProcessorCoreData updated = coreData.withLastException("").withLastExceptionTime(0L);
        setData(ProcessorModule.PROCESSOR_CORE_DATA.get(), updated);
        setChanged();
    }

    public void exception(ExceptionType exception, @Nullable RunningProgram program) {
        // For too many events exception we don't want to queue another event for obvious reasons
        if (exception != EXCEPT_TOOMANYEVENTS) {
            for (int i = 0; i < cardInfo.length; i++) {
                CardInfo info = cardInfo[i];
                CompiledCard compiledCard = info.getCompiledCard();
                if (compiledCard != null) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_EXCEPTION)) {
                        int index = event.index();
                        CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                        String code = evaluateStringParameter(compiledOpcode, null, 0);
                        if (exception.getCode().equals(code)) {
                            runOrQueueEvent(i, event, program == null ? null : program.getCraftTicket(), null);
                            return;
                        }
                    }
                }
            }
        }

        String message;
        if (program != null) {
            CompiledCard card = getCompiledCard(program.getCardIndex());
            if (card == null) {
                message = ChatFormatting.RED + "INTERNAL: " + exception.getDescription();
            } else {
                CompiledOpcode opcode = program.getCurrentOpcode(this);
                int gridX = opcode.getGridX();
                int gridY = opcode.getGridY();


                message = ChatFormatting.RED + "[" + gridX + "," + gridY + "] " + exception.getDescription() + " (" + program.getCardIndex() + ")";
            }
        } else {
            message = ChatFormatting.RED + exception.getDescription();
        }
        long timestamp = System.currentTimeMillis();
        ProcessorCoreData coreData = getCoreData();
        ProcessorCoreData updated = coreData.withLastException(message).withLastExceptionTime(timestamp);
        setData(ProcessorModule.PROCESSOR_CORE_DATA.get(), updated);
        log(message);
    }

    @Override
    public void log(String message) {
        if (message == null) {
            // @todo report?
            return;
        }
        logMessages.add(message);
        while (logMessages.size() > Config.processorMaxloglines.get()) {
            logMessages.remove();
        }
    }

    private List<String> getDebugLog() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(5, cpuCores.size()); i++) {
            result.add(ChatFormatting.BLUE + "Core " + i + " " + ChatFormatting.WHITE + getStatus(i));
        }

        showWithWarn("Event queue: ", eventQueue.size(), 20, result);
        showWithWarn("Waiting items: ", waitingForItems.size(), 20, result);
        showWithWarn("Locks: ", locks.size(), 10, result);

        ProcessorCoreData coreData = getCoreData();
        String lastException = coreData.lastException();
        if (!lastException.isEmpty()) {
            long dt = System.currentTimeMillis() - coreData.lastExceptionTime();
            result.add(ChatFormatting.RED + lastException);
            if (dt > 60000 * 60) {
                result.add("(" + (dt / (60000 / 60)) + "hours ago)");
            } else if (dt > 60000) {
                result.add("(" + (dt / 60000) + "min ago)");
            } else if (dt > 1000) {
                result.add("(" + (dt / 1000) + "sec ago)");
            } else {
                result.add("(" + dt + "ms ago)");
            }
        }

        return result;
    }

    private void showWithWarn(String label, int size, int max, List<String> result) {
        if (size >= max) {
            result.add(label + ChatFormatting.RED + size);
        } else {
            result.add(label + ChatFormatting.GREEN + size);
        }
    }

    private List<String> getLog() {
        return logMessages.stream().collect(Collectors.toList());
    }

    public List<String> getClientLog() {
        return clientLog;
    }

    public List<String> getClientDebugLog() {
        return clientDebugLog;
    }

    public List<String> getLastMessages(int n) {
        List<String> rc = new ArrayList<>();
        int i = 0;
        for (String s : logMessages) {
            if (i >= logMessages.size() - n) {
                rc.add(s);
            }
            i++;
        }
        return rc;
    }

    public int getFluidSlotsAvailable() {
        if (fluidSlotsAvailable == -1) {
            updateFluidSlotsAvailability();
        }
        return fluidSlotsAvailable;
    }

    public Parameter[] getVariableArray() {
        return variables;
    }

    public List<Parameter> getVariables() {
        List<Parameter> pars = new ArrayList<>();
        Collections.addAll(pars, variables);
        return pars;
    }

    public WatchInfo[] getWatchInfos() {
        return watchInfos;
    }

    public void setWatch(int varIndex, boolean br) {
        watchInfos[varIndex] = new WatchInfo(br);
        markDirtyQuick();
    }

    public void clearWatch(int varIndex) {
        watchInfos[varIndex] = null;
        markDirtyQuick();
    }

    public List<PacketGetFluids.FluidEntry> getFluids() {
        List<PacketGetFluids.FluidEntry> pars = new ArrayList<>();
        for (int i = 0; i < MAXFLUIDVARS; i++) {
            if (isFluidSlotAvailable(i)) {
                Direction side = Direction.values()[i / TANKS];
                BlockEntity te = level.getBlockEntity(getBlockPos().relative(side));
                if (te instanceof MultiTankTileEntity) {
                    MultiTankTileEntity mtank = (MultiTankTileEntity) te;
                    MultiTankFluidProperties[] propertyList = mtank.getProperties();
                    MultiTankFluidProperties properties = propertyList[i % TANKS];
                    FluidStack fluidStack = properties == null ? null : properties.getContents();
                    pars.add(new PacketGetFluids.FluidEntry(fluidStack, true));
                } else {
                    pars.add(new PacketGetFluids.FluidEntry(null, true));
                }
            } else {
                pars.add(new PacketGetFluids.FluidEntry(null, false));
            }
        }
        return pars;
    }

    public List<CpuCore> getCpuCores() {
        return cpuCores;
    }

    private CpuCore findAvailableCore(int cardIndex) {
        if (isExclusive()) {
            if (cardIndex < cpuCores.size()) {
                CpuCore core = cpuCores.get(cardIndex);
                if (!core.hasProgram()) {
                    return core;
                }
            }
        } else {
            for (CpuCore core : cpuCores) {
                if (!core.hasProgram()) {
                    return core;
                }
            }
        }
        return null;
    }

    private void run() {
        long rf = energyStorage.getEnergy();

        for (CpuCore core : cpuCores) {
            if (core.hasProgram()) {
                int rft = Config.coreRFPerTick[core.getTier()].get();
                if (rft < rf) {
                    core.run(this);
                    energyStorage.consumeEnergy(rft);
                    rf -= rft;
                }
            }
        }
    }

    private void updateCores() {
        if (coresDirty) {
            coresDirty = false;
            // @todo, keep state of current running programs?
            cpuCores.clear();
            for (int i = SLOT_EXPANSION; i < SLOT_EXPANSION + EXPANSION_SLOTS; i++) {
                ItemStack expansionStack = items.getStackInSlot(i);
                if (!expansionStack.isEmpty() && expansionStack.getItem() instanceof CPUCoreItem) {
                    CPUCoreItem coreItem = (CPUCoreItem) expansionStack.getItem();
                    CpuCore core = new CpuCore();
                    core.setTier(coreItem.getTier());
                    cpuCores.add(core);
                }
            }
        }
    }

    private void compileCards(HolderLookup.Provider provider) {
        if (cardsDirty) {
            cardsDirty = false;
            for (int i = ProcessorContainer.SLOT_CARD; i < ProcessorContainer.SLOT_CARD + CARD_SLOTS; i++) {
                ItemStack cardStack = items.getStackInSlot(i);
                if (!cardStack.isEmpty()) {
                    int cardIndex = i - ProcessorContainer.SLOT_CARD;
                    if (cardInfo[cardIndex].getCompiledCard() == null) {
                        // @todo validation
                        CompiledCard compiled = CompiledCard.compile(cardStack.get(VariousModule.PROGRAM_CARD_DATA.get()));
                        cardInfo[cardIndex].setCompiledCard(compiled);
                    }
                }
            }
        }
    }

    public String getMachineInfo(Inventory side, int idx) {
        BlockEntity te = getTileEntityAt(side);
        IMachineInformation h = level.getCapability(CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY, te.getBlockPos(), null);
        if (h != null) {
            if (idx < 0 || idx >= h.getTagCount()) {
                throw new ProgException(EXCEPT_INVALIDMACHINE_INDEX);
            }
            return h.getData(idx, 0);
        } else {
            throw new ProgException(EXCEPT_INVALIDMACHINE);
        }
    }

    @Override
    public int getEnergy(Inventory side) {
        BlockEntity te = getTileEntityAt(side);
        if (te == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        IEnergyStorage storage = te.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, te.getBlockPos(), side.getIntSide());
        if (storage == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        return storage.getEnergyStored();
    }

    @Override
    public int getMaxEnergy(Inventory side) {
        BlockEntity te = getTileEntityAt(side);
        if (te == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        IEnergyStorage storage = te.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, te.getBlockPos(), side.getIntSide());
        if (storage == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        return storage.getMaxEnergyStored();
    }

    @Override
    public long getEnergyLong(Inventory side) {
        BlockEntity te = getTileEntityAt(side);
        EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevelMulti(te, null);  // @todo fix side
        if (level.maxEnergy() >= 0) {
            throw new ProgException(EXCEPT_NORF);
        }
        return level.energy();
    }

    @Override
    public long getMaxEnergyLong(Inventory side) {
        BlockEntity te = getTileEntityAt(side);
        EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevelMulti(te, null);  // @todo fix side
        if (level.maxEnergy() >= 0) {
            throw new ProgException(EXCEPT_NORF);
        }
        return level.maxEnergy();
    }

    @Override
    public int getLiquid(@Nonnull Inventory side) {
        IFluidHandler handler = getFluidHandlerAt(side);
        if (handler.getTanks() > 0) {
            FluidStack contents = handler.getFluidInTank(0);
            if (!contents.isEmpty()) {
                return contents.getAmount();
            }
        }
        return 0;
    }

    @Override
    public int getMaxLiquid(@Nonnull Inventory side) {
        IFluidHandler handler = getFluidHandlerAt(side);
        if (handler.getTanks() > 0) {
            return handler.getTankCapacity(0);
        }
        return 0;
    }

    private IStorageScanner getScannerForInv(@Nullable Inventory inv) {
        if (inv == null) {
            return getStorageScanner();
        } else {
            return null;
        }
    }

    @Nullable
    private IItemHandler getHandlerForInv(@Nullable Inventory inv) {
        if (inv == null) {
            return null;
        } else {
            return getItemHandlerAt(inv);
        }
    }

    public boolean compareNBTTag(@Nonnull ItemStack v1, @Nonnull ItemStack v2, @Nonnull ResourceLocation componentId) {
        DataComponentMap componentsV1 = v1.getComponents();
        DataComponentMap componentsV2 = v2.getComponents();

        // Find DataComponentType for the given componentId
        DataComponentType<?> componentType = level.registryAccess().registry(Registries.DATA_COMPONENT_TYPE)
                .get()
                .get(componentId);

        // If either item has no components
        if (!componentsV1.has(componentType) || !componentsV2.has(componentType)) {
            return componentsV1.has(componentType) == componentsV2.has(componentType);
        }

        Object component1 = componentsV1.get(componentType);
        Object component2 = componentsV2.get(componentType);

        if (component1 == component2) {
            return true;
        }
        if (component1 != null) {
            return component1.equals(component2);
        }
        return false;
    }

    private MultiTankFluidProperties getFluidPropertiesFromMultiTank(Direction side, int idx) {
        BlockEntity te = level.getBlockEntity(getBlockPos().relative(side));
        if (te instanceof MultiTankTileEntity) {
            MultiTankTileEntity mtank = (MultiTankTileEntity) te;
            return mtank.getProperties()[idx];
        }
        return null;
    }

    @Nonnull
    public FluidStack examineLiquid(@Nonnull Inventory inv, @Nullable Integer slot) {
        if (slot == null) {
            slot = 0;
        }
        Integer finalSlot = slot;
        IFluidHandler handler = getFluidHandlerAt(inv);
        if (finalSlot < handler.getTanks()) {
            return handler.getFluidInTank(finalSlot);
        }
        return FluidStack.EMPTY;
    }

    @Nullable
    public FluidStack examineLiquidInternal(IProgram program, int virtualSlot) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealFluidSlot(virtualSlot);
        Direction side = Direction.values()[realSlot / TANKS];
        int idx = realSlot % TANKS;
        MultiTankFluidProperties properties = getFluidPropertiesFromMultiTank(side, idx);
        if (properties == null) {
            return null;
        }
        return properties.getContents();
    }

    public int pushLiquid(IProgram program, @Nonnull Inventory inv, int amount, int virtualSlot) {
        IFluidHandler handler = getFluidHandlerAt(inv);
        if (handler != null) {
            CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
            int realSlot = info.getRealFluidSlot(virtualSlot);
            Direction side = Direction.values()[realSlot / TANKS];
            int idx = realSlot % TANKS;
            MultiTankFluidProperties properties = getFluidPropertiesFromMultiTank(side, idx);
            if (properties == null) {
                return 0;
            }
            if (!properties.hasContents()) {
                return 0;
            }

            int newAmount = Math.min(amount, properties.getContentsInternal().getAmount());
            FluidStack topush = properties.getContents();   // getContents() already does a copy()
            topush.setAmount(newAmount);
            int filled = handler.fill(topush, IFluidHandler.FluidAction.EXECUTE);
            properties.drain(filled);
            return filled;
        } else {
            return 0;
        }
    }

    public int fetchLiquid(IProgram program, @Nonnull Inventory inv, final int amount, @Nullable FluidStack fluidStack, int virtualSlot) {
        IFluidHandler handler = getFluidHandlerAt(inv);
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealFluidSlot(virtualSlot);
        Direction side = Direction.values()[realSlot / TANKS];
        int idx = realSlot % TANKS;
        MultiTankFluidProperties properties = getFluidPropertiesFromMultiTank(side, idx);
        if (properties == null) {
            return 0;
        }

        int internalAmount = 0;
        if (properties.hasContents()) {
            // There is already some fluid in the slot
            if (fluidStack != null) {
                // This has to match
                if (!FluidStack.isSameFluidSameComponents(fluidStack, properties.getContentsInternal())) {
                    return 0;
                }
            }
            internalAmount = properties.getContentsInternal().getAmount();
        }

        // Make sure we only drain what can fit in the internal slot
        int newAmount = amount;
        if (internalAmount + newAmount > MAXCAPACITY) {
            newAmount = MAXCAPACITY - internalAmount;
        }
        if (newAmount <= 0) {
            return 0;
        }

        if (fluidStack == null) {
            // Just drain any fluid
            FluidStack drained = handler.drain(newAmount, IFluidHandler.FluidAction.SIMULATE);
            if (!drained.isEmpty()) {
                // Check if the fluid matches
                if ((!properties.hasContents()) || FluidStack.isSameFluidSameComponents(properties.getContentsInternal(), drained)) {
                    drained = handler.drain(newAmount, IFluidHandler.FluidAction.EXECUTE);
                    properties.fill(drained);
                    return drained.getAmount();
                }
                return 0;
            }
        } else {
            // Drain only that fluid
            FluidStack todrain = fluidStack.copy();
            todrain.setAmount(newAmount);
            FluidStack drained = handler.drain(todrain, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                int drainedAmount = drained.getAmount();
                if (properties.hasContents()) {
                    drained.setAmount(drained.getAmount() + properties.getContentsInternal().getAmount());
                }
                properties.set(drained);
                return drainedAmount;
            }
        }

        return 0;
    }


    public int fetchItemsFilter(IProgram program, Inventory inv, Integer amount, int virtualSlot, int filterIndex) {
        if (amount != null && amount == 0) {
            throw new ProgException(EXCEPT_BADPARAMETERS);
        }
        Predicate<ItemStack> cache = getFilterCache(filterIndex);
        if (cache == null) {
            throw new ProgException(EXCEPT_UNKNOWN_FILTER);
        }

        IItemHandler handler = getHandlerForInv(inv);
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        ItemStack stack = LogicInventoryTools.tryExtractItem(handler, amount, cache);
        if (stack.isEmpty()) {
            // Nothing to do
            return 0;
        }
        IItemHandler capability = items;
        if (!capability.insertItem(realSlot, stack, true).isEmpty()) {
            // Not enough room. Do nothing
            return 0;
        }
        // All seems ok. Do the real thing now.
        stack = LogicInventoryTools.extractItem(handler, amount, cache);
        capability.insertItem(realSlot, stack, false);
        return stack.getCount();
    }

    public int fetchItems(IProgram program, Inventory inv, Integer slot, Ingredient itemMatcher, boolean routable, @Nullable Integer amount, int virtualSlot) {
        if (amount != null && amount == 0) {
            throw new ProgException(EXCEPT_BADPARAMETERS);
        }

        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);

        ItemStack stack = LogicInventoryTools.tryExtractItem(handler, scanner, amount, routable, itemMatcher, slot);
        if (stack.isEmpty()) {
            // Nothing to do
            return 0;
        }
        IItemHandler capability = items;
        if (!capability.insertItem(realSlot, stack, true).isEmpty()) {
            // Not enough room. Do nothing
            return 0;
        }
        // All seems ok. Do the real thing now.
        stack = LogicInventoryTools.extractItem(handler, scanner, amount, routable, itemMatcher, slot);
        capability.insertItem(realSlot, stack, false);
        return stack.getCount();
    }

    @Override
    @Nullable
    public ItemStack getItemInternal(IProgram program, int virtualSlot) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        return items.getStackInSlot(realSlot);
    }

    public int pushItems(IProgram program, Inventory inv, Integer slot, @Nullable Integer amount, int virtualSlot) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        IItemHandler itemHandler = items;
        ItemStack extracted = itemHandler.extractItem(realSlot, amount == null ? 64 : amount, false);
        if (extracted.isEmpty()) {
            // Nothing to do
            return 0;
        }
        ItemStack remaining = LogicInventoryTools.insertItem(handler, scanner, extracted, slot);
        if (!remaining.isEmpty()) {
            itemHandler.insertItem(realSlot, remaining, false);
            return extracted.getCount() - remaining.getCount();
        }
        return extracted.getCount();
    }

    @Override
    public void sendMessage(IProgram program, int idSlot, String messageName, @Nullable Integer variableSlot) {
        if (!hasNetworkCard()) {
            throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
        }
        if (hasNetworkCard != NetworkCardItem.TIER_ADVANCED) {
            throw new ProgException(EXCEPT_NEEDSADVANCEDNETWORK);
        }

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realIdSlot = info.getRealSlot(idSlot);

        Integer realVariable = info.getRealVar(variableSlot);

        ItemStack idCard = items.getStackInSlot(realIdSlot);
        if (idCard.isEmpty() || !(idCard.getItem() instanceof NetworkIdentifierItem)) {
            throw new ProgException(EXCEPT_NOTANIDENTIFIER);
        }
        if (!ModuleTools.hasModuleTarget(idCard)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        var dim = ModuleTools.getDimensionFromModule(idCard);
        BlockPos dest = ModuleTools.getPositionFromModule(idCard);
        Level world = LevelTools.getLevel(level, dim);
        if (world == null || !LevelTools.isLoaded(world, dest)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        BlockEntity te = world.getBlockEntity(dest);
        if (!(te instanceof ProcessorTileEntity destTE)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        destTE.receiveMessage(messageName, realVariable == null ? null : getVariableArray()[realVariable]);
    }

    private void setOp(String id, GfxOp op) {
        if (!hasGraphicsCard()) {
            throw new ProgException(EXCEPT_MISSINGGRAPHICSCARD);
        }
        ProcessorGraphicsOperationsData graphicsData = getGraphicsOperationsData();
        Map<String, GfxOp> currentOps = graphicsData.operations();
        boolean alreadyPresent = currentOps.containsKey(id);
        if (!alreadyPresent) {
            if (currentOps.size() >= Config.maxGraphicsOpcodes.get()) {
                throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
            }
        }
        Map<String, GfxOp> updatedOps = new LinkedHashMap<>(currentOps);
        updatedOps.put(id, op);
        setData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get(), graphicsData.withOperations(updatedOps));
        orderedOps = null;
        setChanged();
    }

    private void sortOps() {
        orderedOps = new ArrayList<>(getGraphicsOperationsData().operations().keySet());
        orderedOps.sort(String::compareTo);
    }

    @Override
    public void gfxDrawBox(IProgram program, String id, int x, int y, int w, int h, int color) {
        setOp(id, new GfxOpBox(x, y, w, h, color));
    }

    @Override
    public void gfxDrawLine(IProgram program, String id, int x1, int y1, int x2, int y2, int color) {
        setOp(id, new GfxOpLine(x1, y1, x2, y2, color));
    }

    @Override
    public void gfxDrawText(IProgram program, String id, int x, int y, String text, int color) {
        setOp(id, new GfxOpText(x, y, text, color));
    }

    @Override
    public void gfxDrawBox(IProgram program, String id, @Nonnull Tuple loc, @Nonnull Tuple size, int color) {
        setOp(id, new GfxOpBox(loc.getX(), loc.getY(), size.getX(), size.getY(), color));
    }

    @Override
    public void gfxDrawLine(IProgram program, String id, @Nonnull Tuple pos1, @Nonnull Tuple pos2, int color) {
        setOp(id, new GfxOpLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY(), color));
    }

    @Override
    public void gfxDrawText(IProgram program, String id, @Nonnull Tuple pos, String text, int color) {
        setOp(id, new GfxOpText(pos.getX(), pos.getY(), text, color));
    }

    @Override
    public void gfxClear(IProgram program, @Nullable String id) {
        ProcessorGraphicsOperationsData graphicsData = getGraphicsOperationsData();
        Map<String, GfxOp> currentOps = graphicsData.operations();
        boolean changed = false;
        if (id == null || id.isEmpty()) {
            if (!currentOps.isEmpty()) {
                setData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get(), graphicsData.withOperations(Collections.emptyMap()));
                changed = true;
            }
        } else if (currentOps.containsKey(id)) {
            Map<String, GfxOp> updated = new LinkedHashMap<>(currentOps);
            updated.remove(id);
            setData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get(), graphicsData.withOperations(updated));
            changed = true;
        }
        if (changed) {
            orderedOps = null;
            setChanged();
        }
    }

    public Map<String, GfxOp> getGfxOps() {
        return getGraphicsOperationsData().operations();
    }

    public List<String> getOrderedOps() {
        if (orderedOps == null) {
            sortOps();
        }
        return orderedOps;
    }

    public void setClientOrderedGfx(Map<String, GfxOp> gfxOps, List<String> orderedOps) {
        clientGfxOps.clear();
        for (String key : orderedOps) {
            clientGfxOps.add(gfxOps.get(key));
        }
    }

    public List<GfxOp> getClientGfxOps() {
        return clientGfxOps;
    }


    public boolean testWithFilter(ItemStack item, int idx) {
        Predicate<ItemStack> filterCache = getFilterCache(idx);
        if (filterCache == null) {
            throw new ProgException(EXCEPT_UNKNOWN_FILTER);
        }
        return filterCache.test(item);
    }

    private List<Predicate<ItemStack>> getFilterCaches() {
        List<Predicate<ItemStack>> caches = new ArrayList<>();
        for (int i = SLOT_EXPANSION; i < SLOT_EXPANSION + EXPANSION_SLOTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof FilterModuleItem) {
                caches.add(FilterModuleItem.getCache(stack));
            }
        }
        return caches;
    }

    @Nullable
    private Predicate<ItemStack> getFilterCache(int index) {
        if (index < filterCaches.get().size()) {
            return filterCaches.get().get(index);
        } else {
            return null;
        }
    }

    public int getMaxvars() {
        if (maxVars == -1) {
            maxVars = 0;
            hasNetworkCard = -1;
            hasGraphicsCard = false;
            storageCard = -1;
            Item storageCardItem = RFToolsStuff.STORAGE_CONTROL_MODULE.get();
            for (int i = SLOT_EXPANSION; i < SLOT_EXPANSION + EXPANSION_SLOTS; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof NetworkCardItem) {
                        hasNetworkCard = ((NetworkCardItem) stack.getItem()).getTier();
                    } else if (stack.getItem() instanceof RAMChipItem) {
                        maxVars += 8;
                    } else if (stack.getItem() instanceof GraphicsCardItem) {
                        hasGraphicsCard = true;
                    } else if (stack.getItem() == storageCardItem) {
                        storageCard = i;
                    }
                }
            }
            if (maxVars >= MAXVARS) {
                maxVars = MAXVARS;
            }

            updateFluidSlotsAvailability();

        }
        return maxVars;
    }

    public void markFluidSlotsDirty() {
        fluidSlotsAvailable = -1;
    }

    private void updateFluidSlotsAvailability() {
        fluidSlotsAvailable = 0;
        for (Direction facing : Direction.values()) {
            BlockEntity te = level.getBlockEntity(getBlockPos().relative(facing));
            if (te instanceof MultiTankTileEntity) {
                fluidSlotsAvailable |= 1 << facing.ordinal();
            }
        }
        fixCardInfoForSlotAvailability();
        setChanged();
    }

    private void fixCardInfoForSlotAvailability() {
        for (CardInfo info : cardInfo) {
            int alloc = info.getFluidAllocation();
            for (int i = 0; i < MultiTankTileEntity.TANKS * 6; i++) {
                if ((fluidSlotsAvailable & (1 << (i / TANKS))) == 0) {
                    alloc &= ~(1 << i);
                }
            }
            info.setFluidAllocation(alloc);
        }
    }

    public boolean hasGraphicsCard() {
        if (maxVars == -1) {
            getMaxvars();       // Update
        }
        return hasGraphicsCard;
    }

    public boolean hasNetworkCard() {
        if (maxVars == -1) {
            getMaxvars();       // Update
        }
        return hasNetworkCard != -1;
    }

    public int getStorageCard() {
        if (storageCard == -2) {
            getMaxvars();   // Update
        }
        return storageCard;
    }

    public String getChannelName() {
        return channel;
    }

    public int getNodeCount() {
        return networkNodes.size();
    }

    public void stopOrResume(IProgram program) {
        ((RunningProgram) program).popLoopStack(this);
    }

    public boolean testGreater(IProgram program, int var) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realVar = getRealVarSafe(var, info);

        Parameter lastValue = (Parameter) program.getLastValue();
        Parameter varValue = variables[realVar];

        if (lastValue == null) {
            return varValue == null;
        }
        if (varValue == null) {
            return false;
        }
        if (lastValue.getParameterType() != varValue.getParameterType()) {
            return false;
        }
        return ParameterTools.compare(lastValue, varValue) > 0;
    }

    public boolean testEquality(IProgram program, int var) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realVar = getRealVarSafe(var, info);

        Parameter lastValue = (Parameter) program.getLastValue();
        Parameter varValue = variables[realVar];

        if (lastValue == null) {
            return varValue == null;
        }
        if (varValue == null) {
            return false;
        }
        if (lastValue.getParameterType() != varValue.getParameterType()) {
            return false;
        }
        Object v1 = lastValue.getParameterValue().getValue();
        Object v2 = varValue.getParameterValue().getValue();
        if (v1 == null) {
            return v2 == null;
        }
        if (v2 == null) {
            return false;
        }

        if (varValue.getParameterType() == ParameterType.PAR_ITEM) {
            return ItemStack.isSameItem((ItemStack) v1, (ItemStack) v2);
        } else if (varValue.getParameterType() == ParameterType.PAR_FLUID) {
            return FluidStack.isSameFluidSameComponents((FluidStack) v1, (FluidStack) v2);
        } else if (varValue.getParameterType() == ParameterType.PAR_VECTOR) {
            return ParameterTools.compare(lastValue, varValue) == 0;
        } else {
            return v1.equals(v2);
        }
    }

    private int getRealVarSafe(int var, CardInfo info) {
        int realVar = info.getRealVar(var);
        if (realVar == -1) {
            throw new ProgException(EXCEPT_MISSINGVARIABLE);
        }
        if (realVar >= getMaxvars()) {
            throw new ProgException(EXCEPT_NOTENOUGHVARIABLES);
        }
        return realVar;
    }

    public void handleCall(IProgram program, String signal) {
        RunningProgram p = (RunningProgram) program;
        CardInfo info = this.cardInfo[p.getCardIndex()];
        CompiledCard compiledCard = info.getCompiledCard();
        if (compiledCard != null) {
            for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_SIGNAL)) {
                int index = event.index();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                String sig = evaluateStringParameter(compiledOpcode, null, 0);
                if (signal.equals(sig)) {
                    p.pushCall(p.getCurrentOpcode(this).getPrimaryIndex());
                    p.setCurrent(event.index());
                    return;
                }
            }
        }
        throw new ProgException(EXCEPT_MISSINGSIGNAL);
    }


    //    public IOpcodeRunnable.OpcodeResult handleLoop(IProgram program, List<Parameter> vector, int varIdx) {
//        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
//        int realVar = getRealVarSafe(varIdx, info);
//        return IOpcodeRunnable.OpcodeResult.NEGATIVE;
//    }
//
    public IOpcodeRunnable.OpcodeResult handleLoop(IProgram program, int varIdx, int end) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realVar = getRealVarSafe(varIdx, info);

        Parameter parameter = getVariableArray()[realVar];
        int i = TypeConverters.convertToInt(parameter);
        if (i > end) {
            return IOpcodeRunnable.OpcodeResult.NEGATIVE;
        } else {
            ((RunningProgram) program).pushLoopStack(realVar);
            return IOpcodeRunnable.OpcodeResult.POSITIVE;
        }
    }

    public void setValueInToken(IProgram program, int slot) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(slot);
        ItemStack stack = ((IItemHandler) items).getStackInSlot(realSlot);
        if (stack.isEmpty() || !(stack.getItem() instanceof TokenItem)) {
            throw new ProgException(EXCEPT_NOTATOKEN);
        }
        Parameter lastValue = (Parameter) program.getLastValue();
        if (lastValue == null) {
            stack.remove(mcjty.rftoolscontrol.modules.various.VariousModule.TOKEN_DATA);
        } else {
            stack.set(mcjty.rftoolscontrol.modules.various.VariousModule.TOKEN_DATA, new mcjty.rftoolscontrol.modules.various.data.TokenData(lastValue));
        }
    }

    @Nullable
    public Parameter getParameterFromToken(IProgram program, int slot) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(slot);
        ItemStack stack = ((IItemHandler) items).getStackInSlot(realSlot);
        if (stack.isEmpty() || !(stack.getItem() instanceof TokenItem)) {
            throw new ProgException(EXCEPT_NOTATOKEN);
        }
        mcjty.rftoolscontrol.modules.various.data.TokenData data = stack.get(mcjty.rftoolscontrol.modules.various.VariousModule.TOKEN_DATA);
        return data != null ? data.parameter() : null;
    }


    @Override
    public void setVariable(IProgram program, int var) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realVar = getRealVarSafe(var, info);
        setVariableInternal(program, realVar, (Parameter) program.getLastValue());
    }

    public void setVariableInternal(IProgram program, int realVar, Parameter value) {
        if (watchInfos[realVar] != null) {
            Parameter oldValue = variables[realVar];
            if (isWatchTriggered(oldValue, value)) {
                log(ChatFormatting.BLUE + "W" + realVar + ": " + TypeConverters.convertToString(value));
                if (watchInfos[realVar].isBreakOnChange()) {
                    CpuCore core = ((RunningProgram) program).getCore();    // @todo ugly cast
                    core.setDebug(true);
                }
            }
        }
        variables[realVar] = value;
    }

    private boolean isWatchTriggered(Parameter old, Parameter value) {
        if (old == value) {
            return false;
        } else if (old == null) {
            return true;
        } else if (value == null) {
            return true;
        } else {
            return ParameterTools.compare(value, old) != 0;
        }
    }

    @Override
    public IParameter getVariable(IProgram program, int var) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realVar = getRealVarSafe(var, info);
        return variables[realVar];
    }

    @Nullable
    public <T> T evaluateGenericParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex,
                                          BiFunction<ParameterType, Object, T> convertor) {
        List<IParameter> parameters = compiledOpcode.getParameters();
        if (parIndex >= parameters.size()) {
            return null;
        }
        IParameter parameter = parameters.get(parIndex);
        ParameterValue value = parameter.getParameterValue();
        if (value.isConstant()) {
            return convertor.apply(parameter.getParameterType(), value.getValue());
        } else if (value.isFunction()) {
            mcjty.rftoolsbase.api.control.code.Function function = value.getFunction();
            Object v = function.getFunctionRunnable().run(this, program);
            return convertor.apply(function.getReturnType(), v);
        } else {
            CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
            int realVar = getRealVarSafe(value.getVariableIndex(), info);
            Parameter par = variables[realVar];
            if (par == null || par.getParameterValue() == null) {
                return null;
            }
            return convertor.apply(par.getParameterType(), par.getParameterValue().getValue());
        }
    }

    @Nonnull
    public <T> T evaluateGenericParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex,
                                                 BiFunction<ParameterType, Object, T> convertor) {
        T rc = evaluateGenericParameter(compiledOpcode, program, parIndex, convertor);
        if (rc == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return rc;
    }

    @Nonnull
    @Override
    public <T> T evaluateParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, (type, value) -> (T) value);
    }

    @Override
    @Nullable
    public <T> T evaluateParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, (type, value) -> (T) value);
    }

    @Nullable
    @Override
    public Tuple evaluateTupleParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_TUPLE);
    }

    @Nonnull
    @Override
    public Tuple evaluateTupleParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_TUPLE);
    }

    @Nullable
    @Override
    public List<IParameter> evaluateVectorParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        List<Parameter> parameters = evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_VECTOR);
        if (parameters == null) {
            return null;
        }
        // @todo is there a more optimal way?
        return parameters.stream().map(p -> p).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<IParameter> evaluateVectorParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        List<Parameter> parameters = evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_VECTOR);
        // @todo is there a more optimal way?
        return parameters.stream().map(p -> p).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ItemStack evaluateItemParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        ItemStack stack = evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_ITEM);
        // This can return null!
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack evaluateItemParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        ItemStack stack = evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_ITEM);
        if (stack.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return stack;
    }

    @Nullable
    @Override
    public FluidStack evaluateFluidParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_FLUID);
    }

    @Nonnull
    @Override
    public FluidStack evaluateFluidParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_FLUID);
    }

    @Nullable
    @Override
    public BlockSide evaluateSideParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_SIDE);
    }

    @Nonnull
    @Override
    public BlockSide evaluateSideParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_SIDE);
    }

    @Nullable
    @Override
    public Inventory evaluateInventoryParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_INVENTORY);
    }

    @Nonnull
    @Override
    public Inventory evaluateInventoryParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_INVENTORY);
    }

    @Override
    public int evaluateIntParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Integer value = evaluateIntegerParameter(compiledOpcode, program, parIndex);
        if (value == null) {
            return 0;
        }
        return value;
    }

    @Override
    public long evaluateLngParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Long value = evaluateLongParameter(compiledOpcode, program, parIndex);
        if (value == null) {
            return 0;
        }
        return value;
    }

    @Override
    @Nullable
    public Integer evaluateIntegerParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_INTEGER);
    }

    @Override
    @Nullable
    public Long evaluateLongParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_LONG);
    }

    @Override
    @Nullable
    public Number evaluateNumberParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_NUMBER);
    }

    @Override
    @Nullable
    public String evaluateStringParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_STRING);
    }

    @Nonnull
    @Override
    public String evaluateStringParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        return evaluateGenericParameterNonNull(compiledOpcode, program, parIndex, CONVERTOR_STRING);
    }

    @Override
    public boolean evaluateBoolParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Boolean rc = evaluateGenericParameter(compiledOpcode, program, parIndex, CONVERTOR_BOOL);
        if (rc == null) {
            return false;
        }
        return rc;
    }

    public int countItemStorage(ItemStack stack, boolean routable) {
        IStorageScanner scanner = getStorageScanner();
        return scanner.countItems(stack, routable);
    }

    private IStorageScanner getStorageScanner() {
        int card = getStorageCard();
        if (card == -1) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        ItemStack storageStack = items.getStackInSlot(card);
        BlockPos c = ModuleTools.getPositionFromModule(storageStack);
        ResourceKey<Level> dim = ModuleTools.getDimensionFromModule(storageStack);
        if (dim == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        Level world = LevelTools.getLevel(dim);
        if (world == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!LevelTools.isLoaded(world, c)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        BlockEntity te = world.getBlockEntity(c);
        if (te == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!(te instanceof IStorageScanner)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }
        return (IStorageScanner) te;
    }

    public int countSlots(Inventory inv, IProgram program) {
        return getItemHandlerAt(inv).getSlots();
    }

    public int countItem(Inventory inv, Integer slot, ItemStack itemMatcher, boolean routable, IProgram program) {
        if (inv == null) {
            return countItemStorage(itemMatcher, routable);
        }
        // @todo support oredict here?
        IItemHandler handler = getItemHandlerAt(inv);
        if (slot != null) {
            ItemStack stackInSlot = handler.getStackInSlot(slot);
            if (stackInSlot.isEmpty()) {
                return 0;
            } else {
                if (!itemMatcher.isEmpty()) {
                    if (!ItemStack.isSameItem(stackInSlot, itemMatcher)) {
                        return 0;
                    }
                }
                return stackInSlot.getCount();
            }
        } else if (!itemMatcher.isEmpty()) {
            return countItemInHandler(itemMatcher, handler);
        } else {
            // Just count all items
            int cnt = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    cnt += stack.getCount();
                }
            }
            return cnt;
        }
    }

    private int countItemInHandler(ItemStack itemMatcher, IItemHandler handler) {
        int cnt = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, itemMatcher)) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }

    @Override
    @Nullable
    public BlockEntity getTileEntityAt(@Nullable BlockSide inv) {
        BlockPos np = getPositionAt(inv);
        if (np == null) {
            return null;
        }
        return level.getBlockEntity(np);
    }

    @Override
    @Nullable
    public BlockPos getPositionAt(@Nullable BlockSide inv) {
        if (inv == null) {
            return null;
        }
        BlockPos p = worldPosition;
        if (inv.hasNodeName()) {
            if (!hasNetworkCard()) {
                throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
            }
            p = networkNodes.get(inv.getNodeName());
            if (p == null) {
                throw new ProgException(EXCEPT_MISSINGNODE);
            }
        }
        if (inv.getSide() == null) {
            return p;
        } else {
            return p.relative(inv.getSide());
        }
    }

    @Override
    @Nonnull
    public IFluidHandler getFluidHandlerAt(@Nonnull Inventory inv) {
        BlockEntity te = getTileEntityAt(inv);
        if (te == null) {
            throw new ProgException(EXCEPT_NOLIQUID);
        }
        IFluidHandler capability = te.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, te.getBlockPos(), inv.getIntSide());
        if (capability == null) {
            throw new ProgException(EXCEPT_NOLIQUID);
        }
        return capability;
    }

    @Override
    @Nullable
    public IItemHandler getItemHandlerAt(@Nonnull Inventory inv) {
        Direction intSide = inv.getIntSide();
        BlockEntity te = getTileEntityAt(inv);
        if (te == null) {
            return null;
        }
        return getItemHandlerAt(te, intSide);
    }

    private IItemHandler getItemHandlerAt(@Nonnull BlockEntity te, Direction intSide) {
        IItemHandler capability = te.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, te.getBlockPos(), intSide);
        if (capability == null) {
            throw new ProgException(EXCEPT_INVALIDINVENTORY);
        }
        return capability;
    }

    private boolean isExpansionSlot(int index) {
        return index >= SLOT_EXPANSION && index < SLOT_EXPANSION + EXPANSION_SLOTS;
    }

    private boolean isCardSlot(int index) {
        return index >= ProcessorContainer.SLOT_CARD && index < ProcessorContainer.SLOT_CARD + CARD_SLOTS;
    }

    private void removeCard(int index) {
        cardInfo[index].setCompiledCard(null);
        stopPrograms(index);

        Queue<QueuedEvent> newQueue = new ArrayDeque<>();
        for (QueuedEvent event : eventQueue) {
            if (event.cardIndex() != index) {
                newQueue.add(event);
            }
        }
        eventQueue = newQueue;
    }

    private void stopPrograms(int cardIndex) {
        for (CpuCore core : cpuCores) {
            if (core.hasProgram() && core.getProgram().getCardIndex() == cardIndex) {
                core.stopProgram();
            }
        }

        Set<Pair<Integer, Integer>> newRunningEvents = new HashSet<>();
        for (Pair<Integer, Integer> pair : runningEvents) {
            if (pair.getLeft() != cardIndex) {
                newRunningEvents.add(pair);
            }
        }
        runningEvents = newRunningEvents;
    }

    private void clearExpansions() {
        coresDirty = true;
        maxVars = -1;
        storageCard = -2;
        hasNetworkCard = -1;
        filterCaches.clear();
    }

    public int getShowHud() {
        return getSettingsData().showHud();
    }

    public void setShowHud(int showHud) {
        ProcessorSettingsData data = getSettingsData();
        if (data.showHud() != showHud) {
            setData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get(), data.withShowHud(showHud));
            markDirtyClient();
        }
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        CompoundTag info = tagCompound.getCompound("Info");
        if (info != null) {
            boolean ex = info.getBoolean("exclusive");
            int hud = info.getByte("hud");
            setData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get(), new ProcessorSettingsData(ex, hud));
            // @todo 1.21
//            readCardInfo(info);
        }
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        // @todo 1.21 data
//        CompoundTag info = getOrCreateInfo(tagCompound);
//        info.putBoolean("exclusive", exclusive);
//        info.putByte("hud", (byte) showHud);
//        writeCardInfo(info);
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0; i < 6; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0; i < 6; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        var core = input.get(ProcessorModule.ITEM_PROCESSOR_CORE_DATA.get());
        if (core != null) {
            setData(ProcessorModule.PROCESSOR_CORE_DATA.get(), core);
        }
        var cardInfo = input.get(ProcessorModule.ITEM_PROCESSOR_CARD_INFO_DATA.get());
        if (cardInfo != null) {
            setData(ProcessorModule.PROCESSOR_CARD_INFO_DATA.get(), cardInfo);
        }
        var graphics = input.get(ProcessorModule.ITEM_PROCESSOR_GRAPHICS_DATA.get());
        if (graphics != null) {
            setData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get(), graphics);
        }
        var events = input.get(ProcessorModule.ITEM_PROCESSOR_EVENTS_DATA.get());
        if (events != null) {
            setData(ProcessorModule.PROCESSOR_EVENTS_DATA.get(), events);
        }
        var crafting = input.get(ProcessorModule.ITEM_PROCESSOR_CRAFTING_DATA.get());
        if (crafting != null) {
            setData(ProcessorModule.PROCESSOR_CRAFTING_DATA.get(), crafting);
        }
        var extra = input.get(ProcessorModule.ITEM_PROCESSOR_EXTRA_DATA.get());
        if (extra != null) {
            setData(ProcessorModule.PROCESSOR_EXTRA_DATA.get(), extra);
        }
        var settings = input.get(ProcessorModule.ITEM_PROCESSOR_SETTINGS_DATA.get());
        if (settings != null) {
            setData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get(), settings);
        }
        // Energy and items
        energyStorage.applyImplicitComponents(input.get(Registration.ITEM_ENERGY));
        items.applyImplicitComponents(input.get(Registration.ITEM_INVENTORY));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(ProcessorModule.ITEM_PROCESSOR_CORE_DATA.get(), getData(ProcessorModule.PROCESSOR_CORE_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_CARD_INFO_DATA.get(), getData(ProcessorModule.PROCESSOR_CARD_INFO_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_GRAPHICS_DATA.get(), getData(ProcessorModule.PROCESSOR_GRAPHICS_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_EVENTS_DATA.get(), getData(ProcessorModule.PROCESSOR_EVENTS_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_CRAFTING_DATA.get(), getData(ProcessorModule.PROCESSOR_CRAFTING_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_EXTRA_DATA.get(), getData(ProcessorModule.PROCESSOR_EXTRA_DATA.get()));
        builder.set(ProcessorModule.ITEM_PROCESSOR_SETTINGS_DATA.get(), getData(ProcessorModule.PROCESSOR_SETTINGS_DATA.get()));
        // Energy and items
        energyStorage.collectImplicitComponents(builder);
        items.collectImplicitComponents(builder);
    }

    public boolean isFluidAllocated(int cardIndex, int fluidIndex) {
        if (cardIndex == -1) {
            for (CardInfo info : cardInfo) {
                int fluidAlloc = info.getFluidAllocation();
                if (((fluidAlloc >> fluidIndex) & 1) != 0) {
                    return true;
                }
            }
            return false;
        } else {
            CardInfo info = getCardInfo(cardIndex);
            int fluidA = info.getFluidAllocation();
            return ((fluidA >> fluidIndex) & 1) != 0;
        }
    }

    public boolean isVarAllocated(int cardIndex, int varIndex) {
        if (cardIndex == -1) {
            for (CardInfo info : cardInfo) {
                int varAlloc = info.getVarAllocation();
                if (((varAlloc >> varIndex) & 1) != 0) {
                    return true;
                }
            }
            return false;
        } else {
            CardInfo info = getCardInfo(cardIndex);
            int varAlloc = info.getVarAllocation();
            return ((varAlloc >> varIndex) & 1) != 0;
        }
    }

    public boolean isItemAllocated(int cardIndex, int itemIndex) {
        if (cardIndex == -1) {
            for (CardInfo info : cardInfo) {
                int itemAlloc = info.getItemAllocation();
                if (((itemAlloc >> itemIndex) & 1) != 0) {
                    return true;
                }
            }
            return false;
        } else {
            CardInfo info = getCardInfo(cardIndex);
            int itemAlloc = info.getItemAllocation();
            return ((itemAlloc >> itemIndex) & 1) != 0;
        }
    }

    public CardInfo getCardInfo(int index) {
        return cardInfo[index];
    }

    public CompiledCard getCompiledCard(int index) {
        CardInfo info = getCardInfo(index);
        CompiledCard card = info.getCompiledCard();
        ItemStack cardStack = items.getStackInSlot(index + ProcessorContainer.SLOT_CARD);
        if (card == null && !cardStack.isEmpty()) {
            card = CompiledCard.compile(cardStack.get(VariousModule.PROGRAM_CARD_DATA.get()));
            cardInfo[index].setCompiledCard(card);
        }
        return card;
    }

    private void allocate(int card, int itemAlloc, int varAlloc, int fluidAlloc) {
        cardInfo[card].setItemAllocation(itemAlloc);
        cardInfo[card].setVarAllocation(varAlloc);
        cardInfo[card].setFluidAllocation(fluidAlloc);
        setChanged();
    }

    public void showNetworkInfo() {
        log("Channel: " + channel);
        log("Nodes: " + networkNodes.size());
    }

    public void listNodes() {
        if (networkNodes.isEmpty() && craftingStations.isEmpty()) {
            log("No nodes or crafting stations!");
        } else {
            for (Map.Entry<String, BlockPos> entry : networkNodes.entrySet()) {
                log(ChatFormatting.GREEN + "Node " + ChatFormatting.YELLOW + entry.getKey() + ChatFormatting.GREEN + " at " + ChatFormatting.YELLOW + BlockPosTools.toString(entry.getValue()));
            }
            for (BlockPos station : craftingStations) {
                log(ChatFormatting.GREEN + "Crafting station at " + ChatFormatting.YELLOW + BlockPosTools.toString(station));
            }
        }
    }

    public void setupNetwork(String name) {
        channel = name;
        setChanged();
    }

    public void redstoneNodeChange(int previousMask, int newMask, String node) {
        for (int i = 0; i < cardInfo.length; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                handleEventsRedstoneOn(i, compiledCard, node, previousMask, newMask);
                handleEventsRedstoneOff(i, compiledCard, node, previousMask, newMask);
            }
        }
    }

    public void scanNodes() {
        if (!hasNetworkCard()) {
            log(ChatFormatting.RED + "No network card!");
            return;
        }
        if (channel == null || channel.isEmpty()) {
            log(ChatFormatting.RED + "Setup a channel first!");
            return;
        }
        networkNodes.clear();
        craftingStations.clear();
        int range = hasNetworkCard == NetworkCardItem.TIER_NORMAL ? 8 : 16;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos n = new BlockPos(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
                    BlockEntity te = level.getBlockEntity(n);
                    if (te instanceof NodeTileEntity) {
                        NodeTileEntity node = (NodeTileEntity) te;
                        if (channel.equals(node.getChannelName())) {
                            if (node.getNodeName() == null || node.getNodeName().isEmpty()) {
                                log("Node is missing a name!");
                            } else {
                                networkNodes.put(node.getNodeName(), n);
                                node.setProcessor(getBlockPos());
                            }
                        }
                    } else if (te instanceof CraftingStationTileEntity) {
                        CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                        craftingStation.registerProcessor(worldPosition);
                        craftingStations.add(n);
                    }
                }
            }
        }
        log("Found " + networkNodes.size() + " node(s)");
        log("Found " + craftingStations.size() + " crafting station(s)");
        setChanged();
    }

    private boolean isValidExpansionItem(Item item) {
        Item storageCardItem = RFToolsStuff.STORAGE_CONTROL_MODULE.get();
        return item == ProcessorModule.GRAPHICS_CARD.get() || item == ProcessorModule.NETWORK_CARD.get() ||
                item == ProcessorModule.ADVANCED_NETWORK_CARD.get() || item == ProcessorModule.CPU_CORE_500.get() ||
                item == ProcessorModule.CPU_CORE_1000.get() || item == ProcessorModule.CPU_CORE_2000.get() ||
                item == ProcessorModule.RAM_CHIP.get() || item == storageCardItem || item instanceof FilterModuleItem;
    }

    private void onUpdateCard(int index) {
        if (isCardSlot(index)) {
            removeCard(index - ProcessorContainer.SLOT_CARD);
            cardsDirty = true;
        } else if (isExpansionSlot(index)) {
            clearExpansions();
        }
    }


    public static final Key<Integer> PARAM_CARD = new Key<>("card", Type.INTEGER);
    public static final Key<Integer> PARAM_ITEMS = new Key<>("items", Type.INTEGER);
    public static final Key<Integer> PARAM_VARS = new Key<>("vars", Type.INTEGER);
    public static final Key<Integer> PARAM_FLUID = new Key<>("fluids", Type.INTEGER);
    public static final Key<String> PARAM_CMD = new Key<>("cmd", Type.STRING);
    public static final Key<Boolean> PARAM_EXCLUSIVE = new Key<>("exclusive", Type.BOOLEAN);
    public static final Key<Integer> PARAM_HUDMODE = new Key<>("hudmode", Type.INTEGER);

    @ServerCommand
    public static final Command<?> CMD_ALLOCATE = Command.<ProcessorTileEntity>create("allocate",
            (te, player, params) -> {
                int card = params.get(PARAM_CARD);
                int itemAlloc = params.get(PARAM_ITEMS);
                int varAlloc = params.get(PARAM_VARS);
                int fluidAlloc = params.get(PARAM_FLUID);
                te.allocate(card, itemAlloc, varAlloc, fluidAlloc);
            });
    @ServerCommand
    public static final Command<?> CMD_EXECUTE = Command.<ProcessorTileEntity>create("execute",
            (te, player, params) -> Commands.executeCommand(te, params.get(PARAM_CMD)));
    @ServerCommand
    public static final Command<?> CMD_SETEXCLUSIVE = Command.<ProcessorTileEntity>create("setExclusive",
            (te, player, params) -> te.setExclusive(params.get(PARAM_EXCLUSIVE)));
    @ServerCommand
    public static final Command<?> CMD_SETHUDMODE = Command.<ProcessorTileEntity>create("setHudMode",
            (te, player, params) -> te.setShowHud(params.get(PARAM_HUDMODE)));

    @ServerCommand(type = String.class)
    public static final ListCommand<?, ?> CMD_GETDEBUGLOG = ListCommand.<ProcessorTileEntity, String>create("rftoolscontrol.processor.getDebugLog",
            (te, player, params) -> te.getDebugLog(),
            (te, player, params, list) -> te.clientDebugLog = list);

    @ServerCommand(type = String.class)
    public static final ListCommand<?, ?> CMD_GETLOG = ListCommand.<ProcessorTileEntity, String>create("rftoolscontrol.processor.getLog",
            (te, player, params) -> te.getLog(),
            (te, player, params, list) -> te.clientLog = list);

    @ServerCommand(type = Parameter.class, serializer = ParameterSerializer.class)
    public static final ListCommand<?, ?> CMD_GETVARS = ListCommand.<ProcessorTileEntity, Parameter>create("rftoolscontrol.processor.getVars",
            (te, player, params) -> te.getVariables(),
            (te, player, params, list) -> GuiProcessor.storeVarsForClient(list));

    @ServerCommand(type = PacketGetFluids.FluidEntry.class, serializer = PacketGetFluids.FluidEntry.Serializer.class)
    public static final ListCommand<?, ?> CMD_GETFLUIDS = ListCommand.<ProcessorTileEntity, PacketGetFluids.FluidEntry>create("rftoolscontrol.processor.getFluids",
            (te, player, params) -> te.getFluids(),
            (te, player, params, list) -> GuiProcessor.storeFluidsForClient(list));

}

package mcjty.rftoolscontrol.modules.processor.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftoolsbase.api.control.code.Function;
import mcjty.rftoolsbase.api.control.code.ICompiledOpcode;
import mcjty.rftoolsbase.api.control.code.IOpcodeRunnable;
import mcjty.rftoolsbase.api.control.machines.IProcessor;
import mcjty.rftoolsbase.api.control.machines.IProgram;
import mcjty.rftoolsbase.api.control.parameters.*;
import mcjty.rftoolsbase.api.machineinfo.CapabilityMachineInformation;
import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolscontrol.compat.RFToolsStuff;
import mcjty.rftoolscontrol.modules.craftingstation.blocks.CraftingStationTileEntity;
import mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity;
import mcjty.rftoolscontrol.modules.multitank.util.MultiTankFluidProperties;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.items.*;
import mcjty.rftoolscontrol.modules.processor.logic.LogicInventoryTools;
import mcjty.rftoolscontrol.modules.processor.logic.Parameter;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.modules.processor.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.modules.processor.logic.registry.InventoryUtil;
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
import mcjty.rftoolscontrol.modules.various.items.TokenItem;
import mcjty.rftoolscontrol.setup.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.MAXCAPACITY;
import static mcjty.rftoolscontrol.modules.multitank.blocks.MultiTankTileEntity.TANKS;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer.CONTAINER_FACTORY;
import static mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer.SLOT_EXPANSION;
import static mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType.*;

public class ProcessorTileEntity extends GenericTileEntity implements ITickableTileEntity, IProcessor {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3 * 8;
    public static final int EXPANSION_SLOTS = 4 * 4;
    public static final int MAXVARS = 32;
    public static final int MAXFLUIDVARS = 4 * 6;

    public static final String CMD_ALLOCATE = "allocate";
    public static final String CMD_EXECUTE = "execute";
    public static final String CMD_GETLOG = "getLog";
    public static final String CMD_GETDEBUGLOG = "getDebugLog";
    public static final String CMD_SETEXCLUSIVE = "setExclusive";
    public static final String CMD_SETHUDMODE = "setHudMode";
    public static final String CLIENTCMD_GETLOG = "getLog";
    public static final String CLIENTCMD_GETDEBUGLOG = "getDebugLog";
    public static final String CMD_GETVARS = "getVars";
    public static final String CLIENTCMD_GETVARS = "getVars";
    public static final String CMD_GETFLUIDS = "getFluids";
    public static final String CLIENTCMD_GETFLUIDS = "getFluids";

    public static final Key<Integer> PARAM_CARD = new Key<>("card", Type.INTEGER);
    public static final Key<Integer> PARAM_ITEMS = new Key<>("items", Type.INTEGER);
    public static final Key<Integer> PARAM_VARS = new Key<>("vars", Type.INTEGER);
    public static final Key<Integer> PARAM_FLUID = new Key<>("fluids", Type.INTEGER);
    public static final Key<String> PARAM_CMD = new Key<>("cmd", Type.STRING);
    public static final Key<Boolean> PARAM_EXCLUSIVE = new Key<>("exclusive", Type.BOOLEAN);
    public static final Key<Integer> PARAM_HUDMODE = new Key<>("hudmode", Type.INTEGER);

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

    private final NoDirectionItemHander items = createItemHandler();
    private final LazyOptional<AutomationFilterItemHander> itemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, Config.processorMaxenergy.get(), Config.processorReceivepertick.get());
    private final LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<ProcessorContainer>("Processor")
            .containerSupplier((windowId, player) -> ProcessorContainer.create(windowId, getBlockPos(), ProcessorTileEntity.this))
            .itemHandler(() -> items)
            .energyHandler(() -> energyStorage));

    private final List<CpuCore> cpuCores = new ArrayList<>();

    public static final int HUD_OFF = 0;
    public static final int HUD_LOG = 1;
    public static final int HUD_DB = 2;
    public static final int HUD_GFX = 3;
    private int showHud = HUD_OFF;

    // If true some cards might need compiling
    private boolean cardsDirty = true;
    // If true some cpu cores need updating
    private boolean coresDirty = true;

    private int maxVars = -1;   // If -1 we need updating
    private int hasNetworkCard = -1;
    private int storageCard = -2;   // -2 is unknown
    private boolean hasGraphicsCard = false;
    private final Cached<List<Predicate<ItemStack>>> filterCaches = Cached.of(this::getFilterCaches);

    private final Map<String, GfxOp> gfxOps = new HashMap<>();
    private List<String> orderedOps = null;

    // Client-side only: for the HUD
    private final List<GfxOp> clientGfxOps = new ArrayList<>();

    private boolean exclusive = false;

    private String lastException = null;
    private long lastExceptionTime = 0;

    private String channel = "";
    private final Map<String, BlockPos> networkNodes = new HashMap<>();
    private final Set<BlockPos> craftingStations = new HashSet<>();

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[]{0, 0, 0, 0, 0, 0};

    private int tickCount = 0;

    private final Parameter[] variables = new Parameter[MAXVARS];
    private final WatchInfo[] watchInfos = new WatchInfo[MAXVARS];
    private int fluidSlotsAvailable = -1;    // Bitmask indexed by side (6 bits), -1 means unset

    private final CardInfo[] cardInfo = new CardInfo[CARD_SLOTS];

    private Queue<QueuedEvent> eventQueue = new ArrayDeque<>();        // Integer == card index

    private final List<WaitForItem> waitingForItems = new ArrayList<>();

    private final Queue<String> logMessages = new ArrayDeque<>();

    // Client side: log from server
    public long clientTime = 0;
    private List<String> clientLog = new ArrayList<>();
    private List<String> clientDebugLog = new ArrayList<>();

    // Card index, Opcode index
    private Set<Pair<Integer, Integer>> runningEvents = new HashSet<>();

    private final Set<String> locks = new HashSet<>();

    // If set this is a dummy tile entity
    private DimensionId dummyType = null;


    public ProcessorTileEntity() {
        super(ProcessorModule.PROCESSOR_TILE.get());
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
    public ProcessorTileEntity(DimensionId type) {
        this();
        dummyType = type;
    }


    // Return true if this is a dummy tile entity for the tablet
    public boolean isDummy() {
        return dummyType != null;
    }

    @Override
    public DimensionId getDimension() {
        if (dummyType != null) {
            return dummyType;
        }
        return super.getDimension();
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
        setChanged();
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
            TileEntity te = level.getBlockEntity(p);
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
    public void setPowerOut(@Nonnull BlockSide side, int level) {
        Direction facing = side.getSide();
        BlockPos p = getAdjacentPosition(side);
        if (p == null) {
            return;
        }

        if (level < 0) {
            level = 0;
        } else if (level > 15) {
            level = 15;
        }

        if (p.equals(worldPosition)) {
            powerOut[facing.ordinal()] = level;
            setChanged();
            level.neighborChanged(this.worldPosition.relative(facing), this.getBlockState().getBlock(), this.worldPosition);
        } else {
            NodeTileEntity te = (NodeTileEntity) level.getBlockEntity(p);
            te.setPowerOut(facing, level);
        }
    }

    public int getPowerOut(Direction side) {
        return powerOut[side.ordinal()];
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            process();
            prevIn = powerLevel;
        }
    }

    private void process() {
        tickCount++;

        setChanged();
        updateCores();
        compileCards();
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
            CompiledEvent compiledEvent = queuedEvent.getCompiledEvent();
            if (compiledEvent.isSingle() && runningEvents.contains(Pair.of(queuedEvent.getCardIndex(), compiledEvent.getIndex()))) {
                return;
            }
            CpuCore core = findAvailableCore(queuedEvent.getCardIndex());
            if (core != null) {
                eventQueue.remove();
                RunningProgram program = new RunningProgram(queuedEvent.getCardIndex());
                program.startFromEvent(compiledEvent);
                program.setCraftTicket(queuedEvent.getTicket());
                program.setLastValue(queuedEvent.getParameter());
                core.startProgram(program);
                if (compiledEvent.isSingle()) {
                    runningEvents.add(Pair.of(queuedEvent.getCardIndex(), compiledEvent.getIndex()));
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
                        int index = event.getIndex();
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
                            getItemHandlerAt(inv).ifPresent(handler -> {
                                for (int i = 0; i < handler.getSlots(); i++) {
                                    ItemStack s = handler.getStackInSlot(i);
                                    if (!s.isEmpty() && s.getItem() == RFToolsStuff.CRAFTING_CARD) {
                                        ItemStack result = CraftingCardItem.getResult(s);
                                        if (!result.isEmpty()) {
                                            stacks.add(result);
                                        }
                                    }
                                }
                            });
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
            TileEntity te = level.getBlockEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
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
            TileEntity te = level.getBlockEntity(p);
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

        TileEntity te = getTileEntityAt(workbench);
        if (!(te instanceof WorkbenchTileEntity)) {
            throw new ProgException(EXCEPT_NOTAWORKBENCH);
        }
        ItemStack finalItem = item;
        ItemStack card = getItemHandlerAt(te, Direction.EAST).map(handler -> findCraftingCard(handler, finalItem)).orElse(ItemStack.EMPTY);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }

        if (!CraftingCardItem.fitsGrid(card)) {
            throw new ProgException(EXCEPT_NOTAGRID);
        }

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        IItemHandler itemHandler = items;

        return getItemHandlerAt(te, Direction.UP).map(gridHandler -> {
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
        }).orElse(false);
    }

    public int pushItemsMulti(IProgram program, @Nullable Inventory inv, int slot1, int slot2, @Nullable Integer extSlot) {
        return getHandlerForInv(inv).map(handler -> {
            IStorageScanner scanner = getScannerForInv(inv);

            CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
            IItemHandler itemHandler = items;
            int e = 0;
            if (extSlot != null) {
                e = extSlot;
            }

            int failed = 0;
            for (int slot = slot1; slot <= slot2; slot++) {
                int realSlot = info.getRealSlot(slot);
                ItemStack stack = itemHandler.getStackInSlot(realSlot);
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
        }).orElse(0);
    }

    public int countCardIngredients(IProgram program, @Nullable Inventory inv, ItemStack card) {
        IStorageScanner scanner = getScannerForInv(inv);
        return getHandlerForInv(inv).map(handler -> {
            List<Ingredient> ingredients = CraftingCardItem.getIngredients(card);
            List<Ingredient> needed = combineIngredients(ingredients);
            return countPossibleCrafts(scanner, handler, needed);
        }).orElse(0);
    }

    public boolean checkIngredients(IProgram program, @Nonnull Inventory cardInv, ItemStack item, int slot1, int slot2) {
        if (item.isEmpty()) {
            item = getCraftResult(program);
        }
        if (item.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }
        ItemStack finalItem = item;
        ItemStack card = getItemHandlerAt(cardInv)
                .map(cardHandler -> findCraftingCard(cardHandler, finalItem))
                .orElse(ItemStack.EMPTY);
        if (card.isEmpty()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }

        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];

        IItemHandler itemHandler = items;
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
            ItemStack localStack = itemHandler.getStackInSlot(realSlot);
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
        IStorageScanner scanner = getScannerForInv(inv);
        return getHandlerForInv(inv).map(handler -> {
            ItemStack item = inputStack;
            if (item.isEmpty()) {
                item = getCraftResult(program);
            }
            if (item.isEmpty()) {
                throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
            }

            ItemStack finalItem = item;
            return getHandlerForInv(destInv).map(destHandler -> {
                ItemStack card = getItemHandlerAt(cardInv)
                        .map(cardHandler -> findCraftingCard(cardHandler, finalItem))
                        .orElse(ItemStack.EMPTY);
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
                IItemHandler itemHandler = items;
                int slot = slot1;

                for (Ingredient ingredient : ingredients) {
                    int realSlot = info.getRealSlot(slot);
                    if (ingredient != Ingredient.EMPTY) {
                        ItemStack stack = LogicInventoryTools.extractItem(handler, scanner, LogicInventoryTools.getCountFromIngredient(ingredient), true, ingredient, null);
                        if (!stack.isEmpty()) {
                            itemHandler.insertItem(realSlot, stack, false);
                        }
                    }
                    slot++;
                }
                return 0;
            }).orElseThrow(() -> new ProgException(EXCEPT_INVALIDINVENTORY));
        }).orElse(0);
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
        // @todo 1.15 can we solve this in a different way?
//        List<Ingredient> needed = new ArrayList<>();
//        for (Ingredient ingredient : ingredients) {
////            if (!ingredient.isEmpty()) {
//                boolean found = false;
//                for (Ingredient neededStack : needed) {
//                    if (ingredient.test(neededStack)) {
//                        neededStack.grow(ingredient.getCount());
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    needed.add(ingredient.copy());
//                }
////            }
//        }
//        return needed;
        return ingredients;
    }

    public int getIngredients(IProgram program, Inventory inv, Inventory cardInv, ItemStack inputStack, int slot1, int slot2) {
        IStorageScanner scanner = getScannerForInv(inv);
        return getHandlerForInv(inv).map(handler -> {
            ItemStack item = inputStack;
            if (item.isEmpty()) {
                item = getCraftResult(program);
            }
            if (item.isEmpty()) {
                throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
            }

            ItemStack finalItem = item;
            ItemStack card = getItemHandlerAt(cardInv)
                    .map(cardHandler -> findCraftingCard(cardHandler, finalItem))
                    .orElse(ItemStack.EMPTY);
            if (card.isEmpty()) {
                throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
            }
            CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];

            IItemHandler itemHandler = items;
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
                        ItemStack remainder = itemHandler.insertItem(realSlot, stack, false);
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
        }).orElse(0);
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
            TileEntity te = level.getBlockEntity(p);
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
            TileEntity te = level.getBlockEntity(p);
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
        if (itemStack.getItem() instanceof TokenItem && itemStack.hasTag()) {
            CompoundNBT tag = itemStack.getTag().getCompound("parameter");
            if (tag.isEmpty()) {
                return ItemStack.EMPTY;
            }
            Parameter parameter = ParameterTools.readFromNBT(tag);
            if (parameter == null || !parameter.isSet()) {
                return ItemStack.EMPTY;
            }
            return TypeConverters.convertToItem(parameter);
        }
        return ItemStack.EMPTY;
    }


    @Override
    public ItemStack getCraftResult(IProgram program) {
        if (!program.hasCraftTicket()) {
            return ItemStack.EMPTY;
        }
        for (BlockPos p : craftingStations) {
            TileEntity te = level.getBlockEntity(p);
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
        return getHandlerForInv(inventory)
                .map(handler -> findCraftingCard(handler, stack))
                .orElseThrow(() -> new ProgException(EXCEPT_INVALIDINVENTORY));
    }

    private ItemStack findCraftingCard(IItemHandler handler, ItemStack craftResult) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (!s.isEmpty() && s.getItem() == RFToolsStuff.CRAFTING_CARD) {
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
                    int index = event.getIndex();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    ItemStack stack = evaluateItemParameter(compiledOpcode, null, 0);
                    Inventory inv = evaluateInventoryParameter(compiledOpcode, null, 1);
                    if (!stack.isEmpty()) {
                        if (stack.sameItem(stackToCraft)) {
                            runOrQueueEvent(i, event, ticket, null);
                            return;
                        }
                    } else if (inv != null) {
                        ItemStack craftingCard = getItemHandlerAt(inv)
                                .map(handler -> findCraftingCard(handler, stackToCraft))
                                .orElse(ItemStack.EMPTY);
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
            int index = event.getIndex();
            CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
            int ticks = evaluateIntParameter(compiledOpcode, null, 0);
            if (ticks > 0 && tickCount % ticks == 0) {
                if (!waitingForItems.isEmpty()) {
                    WaitForItem found = null;
                    int foundIdx = -1;
                    for (int i = 0; i < waitingForItems.size(); i++) {
                        WaitForItem wfi = waitingForItems.get(i);
                        if (wfi.getInventory() == null || wfi.getItemStack().isEmpty()) {
                            foundIdx = i;
                            found = wfi;
                            break;
                        } else {
                            int cnt = getItemHandlerAt(wfi.getInventory())
                                    .map(handler -> countItemInHandler(wfi.getItemStack(), handler))
                                    .orElse(0);
                            if (cnt >= wfi.getItemStack().getCount()) {
                                foundIdx = i;
                                found = wfi;
                                break;
                            }
                        }
                    }
                    if (found != null) {
                        waitingForItems.remove(foundIdx);
                        runOrQueueEvent(cardIndex, event, found.getTicket(), null);
                    }
                }
            }
        }
    }

    private void handleEventsTimer(int i, CompiledCard compiledCard) {
        for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_TIMER)) {
            int index = event.getIndex();
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
                int index = event.getIndex();
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
                int index = event.getIndex();
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
                int index = event.getIndex();
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
                int index = event.getIndex();
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
        if (event.isSingle() && runningEvents.contains(Pair.of(cardIndex, event.getIndex()))) {
            // Already running and single
            return;
        }
        CpuCore core = findAvailableCore(cardIndex);
        if (core == null) {
            // No available core. First we check if this exact event is already
            // in the queue. If so we drop it. Otherwise we add it
            for (QueuedEvent q : eventQueue) {
                if (q.getCardIndex() == cardIndex) {
                    if (q.getCompiledEvent().equals(event)) {
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
            if (event.isSingle()) {
                runningEvents.add(Pair.of(cardIndex, event.getIndex()));
            }
        }
    }

    private void runOrQueueEvent(int cardIndex, CompiledEvent event, @Nullable String ticket, @Nullable Parameter parameter) {
        if (event.isSingle() && runningEvents.contains(Pair.of(cardIndex, event.getIndex()))) {
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
            if (event.isSingle()) {
                runningEvents.add(Pair.of(cardIndex, event.getIndex()));
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
                    int index = event.getIndex();
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
                    int index = event.getIndex();
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
                    int index = event.getIndex();
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

        if (lastException != null) {
            long dt = System.currentTimeMillis() - lastExceptionTime;
            log("Last: " + TextFormatting.RED + lastException);
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
            TileEntity te = level.getBlockEntity(np);
            if (te instanceof NodeTileEntity) {
                NodeTileEntity tileEntity = (NodeTileEntity) te;
                for (Direction facing : Direction.values()) {
                    tileEntity.setPowerOut(facing, 0);
                }
            }
        }
        gfxOps.clear();
        orderedOps.clear();
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
        lastException = null;
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
                        int index = event.getIndex();
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
                message = TextFormatting.RED + "INTERNAL: " + exception.getDescription();
            } else {
                CompiledOpcode opcode = program.getCurrentOpcode(this);
                int gridX = opcode.getGridX();
                int gridY = opcode.getGridY();


                message = TextFormatting.RED + "[" + gridX + "," + gridY + "] " + exception.getDescription() + " (" + program.getCardIndex() + ")";
            }
        } else {
            message = TextFormatting.RED + exception.getDescription();
        }
        lastException = message;
        lastExceptionTime = System.currentTimeMillis();
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
            result.add(TextFormatting.BLUE + "Core " + i + " " + TextFormatting.WHITE + getStatus(i));
        }

        showWithWarn("Event queue: ", eventQueue.size(), 20, result);
        showWithWarn("Waiting items: ", waitingForItems.size(), 20, result);
        showWithWarn("Locks: ", locks.size(), 10, result);

        if (lastException != null) {
            long dt = System.currentTimeMillis() - lastExceptionTime;
            result.add(TextFormatting.RED + lastException);
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
            result.add(label + TextFormatting.RED + size);
        } else {
            result.add(label + TextFormatting.GREEN + size);
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
                TileEntity te = level.getBlockEntity(getBlockPos().relative(side));
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
        if (exclusive) {
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

    private void compileCards() {
        if (cardsDirty) {
            cardsDirty = false;
            for (int i = ProcessorContainer.SLOT_CARD; i < ProcessorContainer.SLOT_CARD + CARD_SLOTS; i++) {
                ItemStack cardStack = items.getStackInSlot(i);
                if (!cardStack.isEmpty()) {
                    int cardIndex = i - ProcessorContainer.SLOT_CARD;
                    if (cardInfo[cardIndex].getCompiledCard() == null) {
                        // @todo validation
                        CompiledCard compiled = CompiledCard.compile(ProgramCardInstance.parseInstance(cardStack));
                        cardInfo[cardIndex].setCompiledCard(compiled);
                    }
                }
            }
        }
    }

    public String getMachineInfo(Inventory side, int idx) {
        TileEntity te = getTileEntityAt(side);
        return te.getCapability(CapabilityMachineInformation.MACHINE_INFORMATION_CAPABILITY).map(h -> {
            if (idx < 0 || idx >= h.getTagCount()) {
                throw new ProgException(EXCEPT_INVALIDMACHINE_INDEX);
            }
            return h.getData(idx, 0);
        }).orElseThrow(() -> new ProgException(EXCEPT_INVALIDMACHINE));
    }

    @Override
    public int getEnergy(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        return te.getCapability(CapabilityEnergy.ENERGY, side.getIntSide())
                .map(IEnergyStorage::getEnergyStored).orElseThrow(() -> new ProgException(EXCEPT_NORF));
    }

    @Override
    public int getMaxEnergy(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te == null) {
            throw new ProgException(EXCEPT_NORF);
        }
        return te.getCapability(CapabilityEnergy.ENERGY, side.getIntSide())
                .map(IEnergyStorage::getMaxEnergyStored).orElseThrow(() -> new ProgException(EXCEPT_NORF));
    }

    @Override
    public long getEnergyLong(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevelMulti(te, null);  // @todo fix side
        if (level.getMaxEnergy() >= 0) {
            throw new ProgException(EXCEPT_NORF);
        }
        return level.getEnergy();
    }

    @Override
    public long getMaxEnergyLong(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        EnergyTools.EnergyLevel level = EnergyTools.getEnergyLevelMulti(te, null);  // @todo fix side
        if (level.getMaxEnergy() >= 0) {
            throw new ProgException(EXCEPT_NORF);
        }
        return level.getMaxEnergy();
    }

    @Override
    public int getLiquid(@Nonnull Inventory side) {
        return getFluidHandlerAt(side).map(handler -> {
            if (handler.getTanks() > 0) {
                FluidStack contents = handler.getFluidInTank(0);
                if (!contents.isEmpty()) {
                    return contents.getAmount();
                }
            }
            return 0;
        }).orElse(0);
    }

    @Override
    public int getMaxLiquid(@Nonnull Inventory side) {
        return getFluidHandlerAt(side).map(handler -> {
            if (handler.getTanks() > 0) {
                return handler.getTankCapacity(0);
            }
            return 0;
        }).orElse(0);
    }

    private IStorageScanner getScannerForInv(@Nullable Inventory inv) {
        if (inv == null) {
            return getStorageScanner();
        } else {
            return null;
        }
    }

    private LazyOptional<IItemHandler> getHandlerForInv(@Nullable Inventory inv) {
        if (inv == null) {
            return LazyOptional.empty();
        } else {
            return getItemHandlerAt(inv);
        }
    }

    public boolean compareNBTTag(@Nonnull ItemStack v1, @Nonnull ItemStack v2, @Nonnull String tag) {
        if ((!v1.hasTag()) || (!v2.hasTag())) {
            return v1.hasTag() == v2.hasTag();
        }
        INBT tag1 = v1.getTag().get(tag);
        INBT tag2 = v2.getTag().get(tag);
        if (tag1 == tag2) {
            return true;
        }
        if (tag1 != null) {
            return tag1.equals(tag2);
        }
        return false;
    }

    private MultiTankFluidProperties getFluidPropertiesFromMultiTank(Direction side, int idx) {
        TileEntity te = level.getBlockEntity(getBlockPos().relative(side));
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
        return getFluidHandlerAt(inv).map(handler -> {
            if (finalSlot < handler.getTanks()) {
                return handler.getFluidInTank(finalSlot);
            }
            return FluidStack.EMPTY;
        }).orElse(FluidStack.EMPTY);
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
        return getFluidHandlerAt(inv).map(handler -> {
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
        }).orElse(0);
    }

    public int fetchLiquid(IProgram program, @Nonnull Inventory inv, final int amount, @Nullable FluidStack fluidStack, int virtualSlot) {
        return getFluidHandlerAt(inv).map(handler -> {
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
                    if (!fluidStack.isFluidEqual(properties.getContentsInternal())) {
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
                if (drained != null) {
                    // Check if the fluid matches
                    if ((!properties.hasContents()) || properties.getContentsInternal().isFluidEqual(drained)) {
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
                if (drained != null) {
                    int drainedAmount = drained.getAmount();
                    if (properties.hasContents()) {
                        drained.setAmount(drained.getAmount() + properties.getContentsInternal().getAmount());
                    }
                    properties.fill(drained);
                    return drainedAmount;
                }
            }

            return 0;
        }).orElse(0);
    }


    public int fetchItemsFilter(IProgram program, Inventory inv, Integer amount, int virtualSlot, int filterIndex) {
        if (amount != null && amount == 0) {
            throw new ProgException(EXCEPT_BADPARAMETERS);
        }
        Predicate<ItemStack> cache = getFilterCache(filterIndex);
        if (cache == null) {
            throw new ProgException(EXCEPT_UNKNOWN_FILTER);
        }

        return getHandlerForInv(inv).map(handler -> {
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

        }).orElse(0);
    }

    public int fetchItems(IProgram program, Inventory inv, Integer slot, Ingredient itemMatcher, boolean routable, @Nullable Integer amount, int virtualSlot) {
        if (amount != null && amount == 0) {
            throw new ProgException(EXCEPT_BADPARAMETERS);
        }

        IStorageScanner scanner = getScannerForInv(inv);
        return getHandlerForInv(inv).map(handler -> {
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
        }).orElse(0);
    }

    @Override
    @Nullable
    public ItemStack getItemInternal(IProgram program, int virtualSlot) {
        CardInfo info = this.cardInfo[((RunningProgram) program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        IItemHandler capability = items;
        return capability.getStackInSlot(realSlot);
    }

    public int pushItems(IProgram program, Inventory inv, Integer slot, @Nullable Integer amount, int virtualSlot) {
        IStorageScanner scanner = getScannerForInv(inv);
        return getHandlerForInv(inv).map(handler -> {
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
        }).orElse(0);
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

        IItemHandler handler = items;
        ItemStack idCard = handler.getStackInSlot(realIdSlot);
        if (idCard.isEmpty() || !(idCard.getItem() instanceof NetworkIdentifierItem)) {
            throw new ProgException(EXCEPT_NOTANIDENTIFIER);
        }
        CompoundNBT tagCompound = idCard.getTag();
        if (tagCompound == null || !tagCompound.contains("monitorx")) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        String monitordim = tagCompound.getString("monitordim");
        int monitorx = tagCompound.getInt("monitorx");
        int monitory = tagCompound.getInt("monitory");
        int monitorz = tagCompound.getInt("monitorz");
        ServerWorld world = WorldTools.getWorld(DimensionId.fromResourceLocation(new ResourceLocation(monitordim)));
        BlockPos dest = new BlockPos(monitorx, monitory, monitorz);
        if (!WorldTools.isLoaded(world, dest)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        TileEntity te = world.getBlockEntity(dest);
        if (!(te instanceof ProcessorTileEntity)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        ProcessorTileEntity destTE = (ProcessorTileEntity) te;
        destTE.receiveMessage(messageName, realVariable == null ? null : getVariableArray()[realVariable]);
    }

    private void setOp(String id, GfxOp op) {
        if (!hasGraphicsCard()) {
            throw new ProgException(EXCEPT_MISSINGGRAPHICSCARD);
        }
        if (!gfxOps.containsKey(id)) {
            if (gfxOps.size() >= Config.maxGraphicsOpcodes.get()) {
                throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
            }
            orderedOps = null;
        }
        gfxOps.put(id, op);
        setChanged();
    }

    private void sortOps() {
        orderedOps = new ArrayList<>(gfxOps.keySet());
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
        if (id == null || id.isEmpty()) {
            gfxOps.clear();
            orderedOps = null;
        } else {
            gfxOps.remove(id);
            orderedOps = null;
        }
        setChanged();
    }

    public Map<String, GfxOp> getGfxOps() {
        return gfxOps;
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
            Item storageCardItem = RFToolsStuff.STORAGE_CONTROL_MODULE;
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
            TileEntity te = level.getBlockEntity(getBlockPos().relative(facing));
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
            return ((ItemStack) v1).sameItem((ItemStack) v2);
        } else if (varValue.getParameterType() == ParameterType.PAR_FLUID) {
            return ((FluidStack) v1).isFluidEqual((FluidStack) v2);
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
                int index = event.getIndex();
                CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                String sig = evaluateStringParameter(compiledOpcode, null, 0);
                if (signal.equals(sig)) {
                    p.pushCall(p.getCurrentOpcode(this).getPrimaryIndex());
                    p.setCurrent(event.getIndex());
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
        if (!stack.hasTag()) {
            stack.setTag(new CompoundNBT());
        }
        Parameter lastValue = (Parameter) program.getLastValue();
        if (lastValue == null) {
            stack.getTag().remove("parameter");
        } else {
            CompoundNBT tag = ParameterTools.writeToNBT(lastValue);
            stack.getTag().put("parameter", tag);
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
        if (!stack.hasTag()) {
            return null;
        }
        CompoundNBT tag = stack.getTag().getCompound("parameter");
        if (tag.isEmpty()) {
            return null;
        }
        return ParameterTools.readFromNBT(tag);
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
                log(TextFormatting.BLUE + "W" + realVar + ": " + TypeConverters.convertToString(value));
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
            return value.compareTo(old) != 0;
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
            Function function = value.getFunction();
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
        if (scanner == null) {
            return 0;
        }
        return scanner.countItems(stack, routable);
    }

    private IStorageScanner getStorageScanner() {
        int card = getStorageCard();
        if (card == -1) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        ItemStack storageStack = items.getStackInSlot(card);
        if (!storageStack.hasTag()) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        CompoundNBT tagCompound = storageStack.getTag();
        BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
        String dim = tagCompound.getString("monitordim");
        World world = WorldTools.getWorld(DimensionId.fromResourceLocation(new ResourceLocation(dim)));
        if (world == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!WorldTools.isLoaded(world, c)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        TileEntity te = world.getBlockEntity(c);
        if (te == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!(te instanceof IStorageScanner)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }
        return (IStorageScanner) te;
    }

    public int countSlots(Inventory inv, IProgram program) {
        return getItemHandlerAt(inv).map(IItemHandler::getSlots).orElse(0);
    }

    public int countItem(Inventory inv, Integer slot, ItemStack itemMatcher, boolean routable, IProgram program) {
        if (inv == null) {
            return countItemStorage(itemMatcher, routable);
        }
        // @todo support oredict here?
        return getItemHandlerAt(inv).map(handler -> {
            if (slot != null) {
                ItemStack stackInSlot = handler.getStackInSlot(slot);
                if (stackInSlot.isEmpty()) {
                    return 0;
                } else {
                    if (!itemMatcher.isEmpty()) {
                        if (!ItemStack.isSame(stackInSlot, itemMatcher)) {
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
        }).orElse(0);
    }

    private int countItemInHandler(ItemStack itemMatcher, IItemHandler handler) {
        int cnt = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSame(stack, itemMatcher)) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }

    @Override
    @Nullable
    public TileEntity getTileEntityAt(@Nullable BlockSide inv) {
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
    public LazyOptional<IFluidHandler> getFluidHandlerAt(@Nonnull Inventory inv) {
        TileEntity te = getTileEntityAt(inv);
        if (te == null) {
            throw new ProgException(EXCEPT_NOLIQUID);
        }
        LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inv.getIntSide());
        if (!capability.isPresent()) {
            throw new ProgException(EXCEPT_NOLIQUID);
        }
        return capability;
    }

    @Override
    @Nonnull
    public LazyOptional<IItemHandler> getItemHandlerAt(@Nonnull Inventory inv) {
        Direction intSide = inv.getIntSide();
        TileEntity te = getTileEntityAt(inv);
        return getItemHandlerAt(te, intSide);
    }

    private LazyOptional<IItemHandler> getItemHandlerAt(@Nonnull TileEntity te, Direction intSide) {
        if (te == null) {
            throw new ProgException(EXCEPT_INVALIDINVENTORY);
        }
        LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, intSide);
        if (!capability.isPresent()) {
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
            if (event.getCardIndex() != index) {
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
        return showHud;
    }

    public void setShowHud(int showHud) {
        this.showHud = showHud;
        markDirtyClient();
    }

    @Override
    public void readClientDataFromNBT(CompoundNBT tagCompound) {
        exclusive = tagCompound.getBoolean("exclusive");
        showHud = tagCompound.getByte("hud");
        readCardInfo(tagCompound);
    }

    @Override
    public void writeClientDataToNBT(CompoundNBT tagCompound) {
        tagCompound.putBoolean("exclusive", exclusive);
        tagCompound.putByte("hud", (byte) showHud);
        writeCardInfo(tagCompound);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        for (int i = 0; i < 6; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        super.save(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        for (int i = 0; i < 6; i++) {
            tagCompound.putByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    protected void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        tickCount = info.getInt("tickCount");
        channel = info.getString("channel");
        exclusive = info.getBoolean("exclusive");
        showHud = info.getByte("hud");
        if (info.contains("lastExc")) {
            lastException = info.getString("lastExc");
            lastExceptionTime = info.getLong("lastExcT");
        } else {
            lastException = null;
            lastExceptionTime = 0;
        }

        readCardInfo(info);
        readCores(info);
        readEventQueue(info);
        readLog(info);
        readVariables(info);
        readNetworkNodes(info);
        readCraftingStations(info);
        readWaitingForItems(info);
        readLocks(info);
        readRunningEvents(info);
        readGraphicsOperations(info);
    }

    private void readGraphicsOperations(CompoundNBT tagCompound) {
        gfxOps.clear();
        CompoundNBT opTag = tagCompound.getCompound("gfxop");
        for (String key : opTag.getAllKeys()) {
            gfxOps.put(key, GfxOp.readFromNBT(opTag.getCompound(key)));
        }
        sortOps();
    }

    private void readRunningEvents(CompoundNBT tagCompound) {
        runningEvents.clear();
        ListNBT evList = tagCompound.getList("singev", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < evList.size(); i++) {
            CompoundNBT tag = evList.getCompound(i);
            int cardIndex = tag.getInt("card");
            int eventIndex = tag.getInt("event");
            runningEvents.add(Pair.of(cardIndex, eventIndex));
        }
    }

    private void readLocks(CompoundNBT tagCompound) {
        locks.clear();
        ListNBT lockList = tagCompound.getList("locks", Constants.NBT.TAG_STRING);
        for (int i = 0; i < lockList.size(); i++) {
            String name = lockList.getString(i);
            locks.add(name);
        }
    }

    private void readWaitingForItems(CompoundNBT tagCompound) {
        waitingForItems.clear();
        ListNBT waitingList = tagCompound.getList("waiting", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < waitingList.size(); i++) {
            CompoundNBT tag = waitingList.getCompound(i);
            String ticket = tag.getString("ticket");

            ItemStack stack;
            if (tag.contains("item")) {
                stack = ItemStack.of(tag.getCompound("item"));
            } else {
                stack = ItemStack.EMPTY;
            }

            Inventory inventory;
            if (tag.contains("inv")) {
                inventory = InventoryUtil.readFromNBT(tag.getCompound("inv"));
            } else {
                inventory = null;
            }

            WaitForItem waitForItem = new WaitForItem(ticket, stack, inventory);
            waitingForItems.add(waitForItem);
        }
    }


    private void readCraftingStations(CompoundNBT tagCompound) {
        craftingStations.clear();
        ListNBT stationList = tagCompound.getList("stations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < stationList.size(); i++) {
            CompoundNBT tag = stationList.getCompound(i);
            BlockPos nodePos = new BlockPos(tag.getInt("nodex"), tag.getInt("nodey"), tag.getInt("nodez"));
            craftingStations.add(nodePos);
        }
    }

    private void readNetworkNodes(CompoundNBT tagCompound) {
        networkNodes.clear();
        ListNBT networkList = tagCompound.getList("nodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < networkList.size(); i++) {
            CompoundNBT tag = networkList.getCompound(i);
            String name = tag.getString("name");
            BlockPos nodePos = new BlockPos(tag.getInt("nodex"), tag.getInt("nodey"), tag.getInt("nodez"));
            networkNodes.put(name, nodePos);
        }
    }

    private void readVariables(CompoundNBT tagCompound) {
        for (int i = 0; i < MAXVARS; i++) {
            variables[i] = null;
            watchInfos[i] = null;
        }
        ListNBT varList = tagCompound.getList("vars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < varList.size(); i++) {
            CompoundNBT var = varList.getCompound(i);
            int index = var.getInt("varidx");
            variables[index] = ParameterTools.readFromNBT(var);
            if (var.contains("watch")) {
                WatchInfo info = new WatchInfo(var.getBoolean("watch"));
                watchInfos[index] = info;
            }
        }
    }

    private void readLog(CompoundNBT tagCompound) {
        logMessages.clear();
        ListNBT logList = tagCompound.getList("log", Constants.NBT.TAG_STRING);
        for (int i = 0; i < logList.size(); i++) {
            logMessages.add(logList.getString(i));
        }
    }

    private void readCores(CompoundNBT tagCompound) {
        ListNBT coreList = tagCompound.getList("cores", Constants.NBT.TAG_COMPOUND);
        cpuCores.clear();
        coresDirty = false;
        for (int i = 0; i < coreList.size(); i++) {
            CpuCore core = new CpuCore();
            core.readFromNBT(coreList.getCompound(i));
            cpuCores.add(core);
        }
        if (cpuCores.isEmpty()) {
            coresDirty = true;
        }
    }

    private void readEventQueue(CompoundNBT tagCompound) {
        eventQueue.clear();
        ListNBT eventQueueList = tagCompound.getList("events", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < eventQueueList.size(); i++) {
            CompoundNBT tag = eventQueueList.getCompound(i);
            int card = tag.getInt("card");
            int index = tag.getInt("index");
            boolean single = tag.getBoolean("single");
            String ticket = tag.contains("ticket") ? tag.getString("ticket") : null;
            Parameter parameter = null;
            if (tag.contains("parameter")) {
                parameter = ParameterTools.readFromNBT(tag.getCompound("parameter"));
            }
            eventQueue.add(new QueuedEvent(card, new CompiledEvent(index, single), ticket, parameter));
        }
    }

    private void readCardInfo(CompoundNBT tagCompound) {
        ListNBT cardInfoList = tagCompound.getList("cardInfo", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < cardInfoList.size(); i++) {
            cardInfo[i] = CardInfo.readFromNBT(cardInfoList.getCompound(i));
        }
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("tickCount", tickCount);
        info.putString("channel", channel == null ? "" : channel);
        info.putBoolean("exclusive", exclusive);
        info.putByte("hud", (byte) showHud);
        if (lastException != null) {
            info.putString("lastExc", lastException);
            info.putLong("lastExcT", lastExceptionTime);
        }

        writeCardInfo(info);
        writeCores(info);
        writeEventQueue(info);
        writeLog(info);
        writeVariables(info);
        writeNetworkNodes(info);
        writeCraftingStations(info);
        writeWaitingForItems(info);
        writeLocks(info);
        writeRunningEvents(info);
        writeGraphicsOperation(info);
    }

    private void writeGraphicsOperation(CompoundNBT tagCompound) {
        CompoundNBT opTag = new CompoundNBT();
        for (Map.Entry<String, GfxOp> entry : gfxOps.entrySet()) {
            opTag.put(entry.getKey(), entry.getValue().writeToNBT());
        }
        tagCompound.put("gfxop", opTag);
    }

    private void writeRunningEvents(CompoundNBT tagCompound) {
        ListNBT evList = new ListNBT();
        for (Pair<Integer, Integer> pair : runningEvents) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("card", pair.getLeft());
            tag.putInt("event", pair.getRight());
            evList.add(tag);
        }
        tagCompound.put("singev", evList);
    }

    private void writeLocks(CompoundNBT tagCompound) {
        ListNBT lockList = new ListNBT();
        for (String name : locks) {
            lockList.add(StringNBT.valueOf(name));
        }
        tagCompound.put("locks", lockList);
    }

    private void writeWaitingForItems(CompoundNBT tagCompound) {
        ListNBT waitingList = new ListNBT();
        for (WaitForItem waitingForItem : waitingForItems) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("ticket", waitingForItem.getTicket());
            if (waitingForItem.getInventory() != null) {
                tag.put("inv", InventoryUtil.writeToNBT(waitingForItem.getInventory()));
            }
            if (!waitingForItem.getItemStack().isEmpty()) {
                tag.put("item", waitingForItem.getItemStack().serializeNBT());
            }
            waitingList.add(tag);
        }
        tagCompound.put("waiting", waitingList);
    }


    private void writeCraftingStations(CompoundNBT tagCompound) {
        ListNBT stationList = new ListNBT();
        for (BlockPos pos : craftingStations) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("nodex", pos.getX());
            tag.putInt("nodey", pos.getY());
            tag.putInt("nodez", pos.getZ());
            stationList.add(tag);
        }
        tagCompound.put("stations", stationList);
    }

    private void writeNetworkNodes(CompoundNBT tagCompound) {
        ListNBT networkList = new ListNBT();
        for (Map.Entry<String, BlockPos> entry : networkNodes.entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("name", entry.getKey());
            tag.putInt("nodex", entry.getValue().getX());
            tag.putInt("nodey", entry.getValue().getY());
            tag.putInt("nodez", entry.getValue().getZ());
            networkList.add(tag);
        }
        tagCompound.put("nodes", networkList);
    }

    private void writeVariables(CompoundNBT tagCompound) {
        ListNBT varList = new ListNBT();
        for (int i = 0; i < MAXVARS; i++) {
            if (variables[i] != null) {
                CompoundNBT var = ParameterTools.writeToNBT(variables[i]);
                var.putInt("varidx", i);
                if (watchInfos[i] != null) {
                    var.putBoolean("watch", watchInfos[i].isBreakOnChange());
                }
                varList.add(var);
            }
        }
        tagCompound.put("vars", varList);
    }

    private void writeLog(CompoundNBT tagCompound) {
        ListNBT logList = new ListNBT();
        for (String message : logMessages) {
            logList.add(StringNBT.valueOf(message));
        }
        tagCompound.put("log", logList);
    }

    private void writeCores(CompoundNBT tagCompound) {
        ListNBT coreList = new ListNBT();
        for (CpuCore core : cpuCores) {
            coreList.add(core.writeToNBT());
        }
        tagCompound.put("cores", coreList);
    }

    private void writeEventQueue(CompoundNBT tagCompound) {
        ListNBT eventQueueList = new ListNBT();
        for (QueuedEvent queuedEvent : eventQueue) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("card", queuedEvent.getCardIndex());
            tag.putInt("index", queuedEvent.getCompiledEvent().getIndex());
            tag.putBoolean("single", queuedEvent.getCompiledEvent().isSingle());
            if (queuedEvent.getTicket() != null) {
                tag.putString("ticket", queuedEvent.getTicket());
            }
            if (queuedEvent.getParameter() != null) {
                CompoundNBT parTag = ParameterTools.writeToNBT(queuedEvent.getParameter());
                tag.put("parameter", parTag);
            }
            eventQueueList.add(tag);
        }
        tagCompound.put("events", eventQueueList);
    }

    private void writeCardInfo(CompoundNBT tagCompound) {
        ListNBT cardInfoList = new ListNBT();
        for (CardInfo info : cardInfo) {
            cardInfoList.add(info.writeToNBT());
        }
        tagCompound.put("cardInfo", cardInfoList);
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
            card = CompiledCard.compile(ProgramCardInstance.parseInstance(cardStack));
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
                log(TextFormatting.GREEN + "Node " + TextFormatting.YELLOW + entry.getKey() + TextFormatting.GREEN + " at " + TextFormatting.YELLOW + BlockPosTools.toString(entry.getValue()));
            }
            for (BlockPos station : craftingStations) {
                log(TextFormatting.GREEN + "Crafting station at " + TextFormatting.YELLOW + BlockPosTools.toString(station));
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
            log(TextFormatting.RED + "No network card!");
            return;
        }
        if (channel == null || channel.isEmpty()) {
            log(TextFormatting.RED + "Setup a channel first!");
            return;
        }
        networkNodes.clear();
        craftingStations.clear();
        int range = hasNetworkCard == NetworkCardItem.TIER_NORMAL ? 8 : 16;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos n = new BlockPos(worldPosition.getX() + x, worldPosition.getY() + y, worldPosition.getZ() + z);
                    TileEntity te = level.getBlockEntity(n);
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

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_ALLOCATE.equals(command)) {
            int card = args.get(PARAM_CARD);
            int itemAlloc = args.get(PARAM_ITEMS);
            int varAlloc = args.get(PARAM_VARS);
            int fluidAlloc = args.get(PARAM_FLUID);
            allocate(card, itemAlloc, varAlloc, fluidAlloc);
            return true;
        } else if (CMD_EXECUTE.equals(command)) {
            Commands.executeCommand(this, args.get(PARAM_CMD));
            return true;
        } else if (CMD_SETEXCLUSIVE.equals(command)) {
            boolean v = args.get(PARAM_EXCLUSIVE);
            setExclusive(v);
            return true;
        } else if (CMD_SETHUDMODE.equals(command)) {
            int v = args.get(PARAM_HUDMODE);
            setShowHud(v);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETLOG.equals(command)) {
            return type.convert(getLog());
        } else if (CMD_GETDEBUGLOG.equals(command)) {
            return type.convert(getDebugLog());
        } else if (CMD_GETVARS.equals(command)) {
            return type.convert(getVariables());
        } else if (CMD_GETFLUIDS.equals(command)) {
            return type.convert(getFluids());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETLOG.equals(command)) {
            clientLog = Type.STRING.convert(list);
            return true;
        } else if (CLIENTCMD_GETDEBUGLOG.equals(command)) {
            clientDebugLog = Type.STRING.convert(list);
            return true;
        } else if (CLIENTCMD_GETVARS.equals(command)) {
            GuiProcessor.storeVarsForClient(Type.create(Parameter.class).convert(list));
            return true;
        } else if (CLIENTCMD_GETFLUIDS.equals(command)) {
            GuiProcessor.storeFluidsForClient(Type.create(PacketGetFluids.FluidEntry.class).convert(list));
            return true;
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int xCoord = getBlockPos().getX();
        int yCoord = getBlockPos().getY();
        int zCoord = getBlockPos().getZ();
        return new AxisAlignedBB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 21, zCoord + 1);
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ProcessorTileEntity.this, CONTAINER_FACTORY.get()) {

            @Override
            protected void onUpdate(int index) {
                if (isCardSlot(index)) {
                    removeCard(index - ProcessorContainer.SLOT_CARD);
                    cardsDirty = true;
                } else if (isExpansionSlot(index)) {
                    clearExpansions();
                }
            }

            @Override
            public boolean isItemValid(int index, ItemStack stack) {
                if (stack.isEmpty()) {
                    return true;
                }
                Item item = stack.getItem();
                if (isExpansionSlot(index)) {
                    Item storageCardItem = RFToolsStuff.STORAGE_CONTROL_MODULE;
                    return item == ProcessorModule.GRAPHICS_CARD.get() || item == ProcessorModule.NETWORK_CARD.get() ||
                            item == ProcessorModule.ADVANCED_NETWORK_CARD.get() || item == ProcessorModule.CPU_CORE_500.get() ||
                            item == ProcessorModule.CPU_CORE_1000.get() || item == ProcessorModule.CPU_CORE_2000.get() ||
                            item == ProcessorModule.RAM_CHIP.get() || item == storageCardItem || item instanceof FilterModuleItem;
                } else if (isCardSlot(index)) {
                    return item == VariousModule.PROGRAM_CARD.get();
                }
                return true;
            }

        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
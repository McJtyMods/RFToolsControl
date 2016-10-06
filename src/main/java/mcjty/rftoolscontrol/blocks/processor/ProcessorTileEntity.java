package mcjty.rftoolscontrol.blocks.processor;

import cofh.api.energy.IEnergyHandler;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.storage.IStorageScanner;
import mcjty.rftoolscontrol.api.code.ICompiledOpcode;
import mcjty.rftoolscontrol.api.code.IOpcodeRunnable;
import mcjty.rftoolscontrol.api.machines.IProcessor;
import mcjty.rftoolscontrol.api.machines.IProgram;
import mcjty.rftoolscontrol.api.parameters.BlockSide;
import mcjty.rftoolscontrol.api.parameters.Inventory;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.craftingstation.CraftingStationTileEntity;
import mcjty.rftoolscontrol.blocks.node.NodeTileEntity;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOp;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOpBox;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOpLine;
import mcjty.rftoolscontrol.blocks.vectorart.GfxOpText;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import mcjty.rftoolscontrol.items.*;
import mcjty.rftoolscontrol.items.craftingcard.CraftingCardItem;
import mcjty.rftoolscontrol.logic.InventoryTools;
import mcjty.rftoolscontrol.logic.ParameterTools;
import mcjty.rftoolscontrol.logic.TypeConverters;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.InventoryUtil;
import mcjty.rftoolscontrol.logic.registry.Opcodes;
import mcjty.rftoolscontrol.logic.running.CpuCore;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.running.ProgException;
import mcjty.rftoolscontrol.logic.running.RunningProgram;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static mcjty.rftoolscontrol.logic.running.ExceptionType.*;

public class ProcessorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable, IProcessor {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3*8;
    public static final int EXPANSION_SLOTS = 4*4;
    public static final int MAXVARS = 32;

    public static final String CMD_ALLOCATE = "allocate";
    public static final String CMD_CLEARLOG = "clearLog";
    public static final String CMD_GETLOG = "getLog";
    public static final String CMD_GETDEBUGLOG = "getDebugLog";
    public static final String CMD_SETEXCLUSIVE = "setExclusive";
    public static final String CMD_SETHUDMODE = "setHudMode";
    public static final String CLIENTCMD_GETLOG = "getLog";
    public static final String CLIENTCMD_GETDEBUGLOG = "getDebugLog";
    public static final String CMD_GETVARS = "getVars";
    public static final String CLIENTCMD_GETVARS = "getVars";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProcessorContainer.factory, ProcessorContainer.SLOTS);
    private List<CpuCore> cpuCores = new ArrayList<>();

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

    private Map<String, GfxOp> gfxOps = new HashMap<>();
    private List<String> orderedOps = null;

    // Client-side only: for the HUD
    private List<GfxOp> clientGfxOps = new ArrayList<>();

    private boolean exclusive = false;

    private String lastException = null;
    private long lastExceptionTime = 0;

    private String channel = "";
    private Map<String, BlockPos> networkNodes = new HashMap<>();
    private Set<BlockPos> craftingStations = new HashSet<>();

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private int tickCount = 0;

    private Parameter[] variables = new Parameter[MAXVARS];

    private CardInfo[] cardInfo = new CardInfo[CARD_SLOTS];

    private Queue<QueuedEvent> eventQueue = new ArrayDeque<>();        // Integer == card index

    private List<WaitForItem> waitingForItems = new ArrayList<>();

    private Queue<String> logMessages = new ArrayDeque<>();

    // Client side: log from server
    public long clientTime = 0;
    private List<String> clientLog = new ArrayList<>();
    private List<String> clientDebugLog = new ArrayList<>();

    // Card index, Opcode index
    private Set<Pair<Integer,Integer>> runningEvents = new HashSet<>();

    private Set<String> locks = new HashSet<>();

    public ProcessorTileEntity() {
        super(GeneralConfiguration.processorMaxenergy, GeneralConfiguration.processorReceivepertick);
        for (int i = 0 ; i < cardInfo.length ; i++) {
            cardInfo[i] = new CardInfo();
        }
        for (int i = 0 ; i < MAXVARS ; i++) {
            variables[i] = null;
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
        markDirty();
    }

    public Parameter getParameter(int idx) {
        return variables[idx];
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private BlockPos getAdjacentPosition(@Nonnull BlockSide side) {
        BlockPos p;
        if (side.getNodeName() != null && !side.getNodeName().isEmpty()) {
            p = networkNodes.get(side.getNodeName());
            if (p == null) {
                throw new ProgException(EXCEPT_MISSINGNODE);
            }
            TileEntity te = worldObj.getTileEntity(p);
            if (!(te instanceof NodeTileEntity)) {
                throw new ProgException(EXCEPT_MISSINGNODE);
            }
        } else {
            p = pos;
        }
        return p;
    }

    @Override
    public int readRedstoneIn(@Nonnull BlockSide side) {
        EnumFacing facing = side.getSide();
        BlockPos p = getAdjacentPosition(side);
        if (p == null) {
            return 0;
        }
        return worldObj.getRedstonePower(p.offset(facing), facing);
    }

    @Override
    public void setPowerOut(@Nonnull BlockSide side, int level) {
        EnumFacing facing = side.getSide();
        BlockPos p = getAdjacentPosition(side);
        if (p == null) {
            return;
        }

        if (p.equals(pos)) {
            powerOut[facing.ordinal()] = level;
            markDirty();
            worldObj.notifyBlockOfStateChange(this.pos.offset(facing), this.getBlockType());
        } else {
            NodeTileEntity te = (NodeTileEntity) worldObj.getTileEntity(p);
            te.setPowerOut(facing, level);
}
    }

    public int getPowerOut(EnumFacing side) {
        return powerOut[side.ordinal()];
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            process();
            prevIn = powerLevel;
        }
    }

    private void process() {
        tickCount++;

        markDirty();
        updateCores();
        compileCards();
        processEventQueue();
        handleEvents();
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
                core.startProgram(program);
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
                        if (stack != null && inv != null) {
                            throw new ProgException(EXCEPT_BADPARAMETERS);
                        }
                        if (stack == null && inv == null) {
                            throw new ProgException(EXCEPT_BADPARAMETERS);
                        }
                        if (stack != null) {
                            stacks.add(stack);
                        } else {
                            // Find all crafting cards in the inventory
                            IItemHandler handler = getItemHandlerAt(inv);
                            for (int i = 0 ; i < handler.getSlots() ; i++) {
                                ItemStack s = handler.getStackInSlot(i);
                                if (s != null && s.getItem() == ModItems.craftingCardItem) {
                                    ItemStack result = CraftingCardItem.getResult(s);
                                    if (result != null) {
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

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        Integer realSlot = info.getRealSlot(slot);
        ItemStack craftedItem = null;
        if (realSlot != null) {
            craftedItem = getItemHandler().getStackInSlot(realSlot);
        }

        for (BlockPos p : craftingStations) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                craftedItem = craftingStation.craftOk(this, ticket, craftedItem);
            }
        }

        if (realSlot != null) {
            // Put back what could not be accepted
            getInventoryHelper().setStackInSlot(realSlot, craftedItem);
        }
    }

    public void craftFail(IProgram program) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        String ticket = program.getCraftTicket();

        for (BlockPos p : craftingStations) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                craftingStation.craftFail(ticket);
            }
        }
    }

    public int pushItemsMulti(IProgram program, Inventory inv, int slot1, int slot2, @Nullable Integer extSlot) {
        IItemHandler handler = getHandlerForInv(inv);
        IStorageScanner scanner = getScannerForInv(inv);

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        IItemHandler itemHandler = getItemHandler();
        int e = 0;
        if (extSlot != null) {
            e = extSlot;
        }

        int failed = 0;
        for (int slot = slot1 ; slot <= slot2 ; slot++) {
            int realSlot = info.getRealSlot(slot);
            ItemStack stack = itemHandler.getStackInSlot(realSlot);
            if (stack != null) {
                ItemStack remaining = InventoryTools.insertItem(handler, scanner, stack, extSlot == null ? null : e);
                if (remaining != null) {
                    failed++;
                }
                inventoryHelper.setStackInSlot(realSlot, remaining);
            }
            e++;
        }
        return failed;
    }

    public boolean checkIngredients(IProgram program, @Nonnull Inventory cardInv, @Nullable ItemStack item, int slot1, int slot2) {
        if (item == null) {
            item = getCraftResult(program);
        }
        if (item == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }
        IItemHandler cardHandler = getItemHandlerAt(cardInv);
        ItemStack card = findCraftingCard(cardHandler, item);
        if (card == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];

        IItemHandler itemHandler = getItemHandler();
        int slot = slot1;

        List<ItemStack> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2-slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        int failed = 0;
        for (ItemStack ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            ItemStack localStack = itemHandler.getStackInSlot(realSlot);
            if (ingredient != null) {
                if (!ingredient.isItemEqual(localStack)) {
                    return false;
                }
                if (ingredient.stackSize != localStack.stackSize) {
                    return false;
                }
            } else {
                if (localStack != null) {
                    return false;
                }
            }
            slot++;
        }
        return true;
    }

    public int getIngredientsSmart(IProgram program, Inventory inv, @Nonnull Inventory cardInv,
                                   @Nullable ItemStack item, int slot1, int slot2, @Nonnull Inventory destInv) {
        IStorageScanner scanner = getScannerForInv(inv);
        IItemHandler handler = getHandlerForInv(inv);

        if (item == null) {
            item = getCraftResult(program);
        }
        if (item == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }

        IItemHandler destHandler = getHandlerForInv(destInv);
        if (destHandler == null) {
            throw new ProgException(EXCEPT_INVALIDINVENTORY);
        }

        IItemHandler cardHandler = getItemHandlerAt(cardInv);
        ItemStack card = findCraftingCard(cardHandler, item);
        if (card == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];

        List<ItemStack> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2 - slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        List<ItemStack> needed = combineIngredients(ingredients);
        int requested = checkAvailableItemsAndRequestMissing(destInv, scanner, handler, needed);
        if (requested != 0) {
            return requested;
        }
        // We got everything;
        IItemHandler itemHandler = getItemHandler();
        int slot = slot1;

        for (ItemStack ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            if (ingredient != null) {
                ItemStack stack = InventoryTools.extractItem(handler, scanner, ingredient.stackSize, true, false, ingredient, null);
                if (stack != null) {
                    itemHandler.insertItem(realSlot, stack, false);
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
    private int checkAvailableItemsAndRequestMissing(Inventory destInv, IStorageScanner scanner, IItemHandler handler, List<ItemStack> needed) {
        int requested = 0;
        for (ItemStack ingredient : needed) {
            if (ingredient != null) {
                int cnt = InventoryTools.countItem(handler, scanner, ingredient, false, ingredient.stackSize);
                if (cnt < ingredient.stackSize) {
                    requested++;
                    ItemStack requestedItem = ingredient.copy();
                    requestedItem.stackSize = ingredient.stackSize - cnt;
                    if (!isRequested(requestedItem)) {
                        if (!requestCraft(requestedItem, destInv)) {
                            // It can't be requested, total failure
                            return -1;
                        }
                    }
                }
            }
        }
        return requested;
    }

    // Given a list of ingredients make a combined list where all identical
    // items are grouped
    private List<ItemStack> combineIngredients(List<ItemStack> ingredients) {
        List<ItemStack> needed = new ArrayList<>();
        for (ItemStack ingredient : ingredients) {
            if (ingredient != null) {
                boolean found = false;
                for (ItemStack neededStack : needed) {
                    if (neededStack.isItemEqual(ingredient)) {
                        neededStack.stackSize += ingredient.stackSize;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    needed.add(ingredient.copy());
                }
            }
        }
        return needed;
    }

    public int getIngredients(IProgram program, Inventory inv, Inventory cardInv, @Nullable ItemStack item, int slot1, int slot2) {
        IStorageScanner scanner = getScannerForInv(inv);
        IItemHandler handler = getHandlerForInv(inv);

        if (item == null) {
            item = getCraftResult(program);
        }
        if (item == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
        }

        IItemHandler cardHandler = getItemHandlerAt(cardInv);
        ItemStack card = findCraftingCard(cardHandler, item);
        if (card == null) {
            throw new ProgException(EXCEPT_MISSINGCRAFTINGCARD);
        }
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];

        IItemHandler itemHandler = getItemHandler();
        int slot = slot1;

        List<ItemStack> ingredients;
        if (CraftingCardItem.fitsGrid(card) && (slot2-slot1 >= 8)) {
            // We have something that fits a crafting grid and we have enough room for a 3x3 grid
            ingredients = CraftingCardItem.getIngredientsGrid(card);
        } else {
            ingredients = CraftingCardItem.getIngredients(card);
        }

        int failed = 0;
        for (ItemStack ingredient : ingredients) {
            int realSlot = info.getRealSlot(slot);
            if (ingredient != null) {
                ItemStack stack = InventoryTools.extractItem(handler, scanner, ingredient.stackSize, true, false, ingredient, null);
                if (stack != null) {
                    ItemStack remainder = itemHandler.insertItem(realSlot, stack, false);
                    if (remainder != null) {
                        InventoryTools.insertItem(handler, scanner, remainder, null);
                    }
                } else {
                    failed++;
                }
            }
            slot++;
        }
        return failed;
    }

    private IItemHandler getItemHandler() {
        return getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public void craftWait(IProgram program, @Nonnull Inventory inv, ItemStack stack) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        if (stack == null) {
            stack = getCraftResult(program);
            if (stack == null) {
                throw new ProgException(EXCEPT_MISSINGCRAFTRESULT);
            }
        }
        WaitForItem waitForItem = new WaitForItem(program.getCraftTicket(), stack, inv);
        waitingForItems.add(waitForItem);
        markDirty();
    }

    public void craftWaitTimed(IProgram program) {
        if (!program.hasCraftTicket()) {
            throw new ProgException(EXCEPT_MISSINGCRAFTTICKET);
        }
        WaitForItem waitForItem = new WaitForItem(program.getCraftTicket(), null, null);
        waitingForItems.add(waitForItem);
        markDirty();
    }

    public boolean isRequested(ItemStack stack) {
        for (BlockPos p : craftingStations) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                if (craftingStation.isRequested(stack)) {
                    return true;
                }
                return false;
            }
        }
        throw new ProgException(EXCEPT_MISSINGCRAFTINGSTATION);

    }

    @Override
    public boolean requestCraft(@Nonnull ItemStack stack, @Nullable Inventory inventory) {
        for (BlockPos p : craftingStations) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                if (craftingStation.request(stack, inventory)) {
                    return true;
                }
                return false;
            }
        }
        throw new ProgException(EXCEPT_MISSINGCRAFTINGSTATION);
    }

    public void setCraftTicket(IProgram program, String ticket) {
        ((RunningProgram)program).setCraftTicket(ticket);
    }

    @Override
    public ItemStack getCraftResult(IProgram program) {
        if (!program.hasCraftTicket()) {
            // @todo ? exception?
            return null;
        }
        for (BlockPos p : craftingStations) {
            TileEntity te = worldObj.getTileEntity(p);
            if (te instanceof CraftingStationTileEntity) {
                CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                ItemStack stack = craftingStation.getCraftResult(program.getCraftTicket());
                if (stack != null) {
                    return stack;
                }
            }
        }
        return null;
    }

    private ItemStack findCraftingCard(IItemHandler handler, ItemStack craftResult) {
        for (int j = 0 ; j < handler.getSlots() ; j++) {
            ItemStack s = handler.getStackInSlot(j);
            if (s != null && s.getItem() == ModItems.craftingCardItem) {
                ItemStack result = CraftingCardItem.getResult(s);
                if (result != null && result.isItemEqual(craftResult)) {
                    return s;
                }
            }
        }
        return null;
    }

    public void fireCraftEvent(String ticket, ItemStack stackToCraft) {
        for (int i = 0 ; i < cardInfo.length ; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_CRAFT)) {
                    int index = event.getIndex();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    ItemStack stack = evaluateItemParameter(compiledOpcode, null, 0);
                    Inventory inv = evaluateInventoryParameter(compiledOpcode, null, 1);
                    if (stack != null) {
                        if (stack.isItemEqual(stackToCraft)) {
                            runOrQueueEvent(i, event, ticket, null);
                            return;
                        }
                    } else if (inv != null) {
                        IItemHandler handler = getItemHandlerAt(inv);
                        ItemStack craftingCard = findCraftingCard(handler, stackToCraft);
                        if (craftingCard != null) {
                            runOrQueueEvent(i, event, ticket, null);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void handleEvents() {
        for (int i = 0 ; i < cardInfo.length ; i++) {
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
                    for (int i = 0 ; i < waitingForItems.size() ; i++) {
                        WaitForItem wfi = waitingForItems.get(i);
                        if (wfi.getInventory() == null || wfi.getItemStack() == null) {
                            foundIdx = i;
                            found = wfi;
                            break;
                        } else {
                            IItemHandler handler = getItemHandlerAt(wfi.getInventory());
                            int cnt = countItemInHandler(wfi.getItemStack(), handler);
                            if (cnt >= wfi.getItemStack().stackSize) {
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
                    EnumFacing facing = side == null ? null : side.getSide();
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
                    EnumFacing facing = side == null ? null : side.getSide();
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
                    EnumFacing facing = side.getSide();
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
                    EnumFacing facing = side.getSide();
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
        if (eventQueue.size() >= GeneralConfiguration.maxEventQueueSize) {
            // Too many events
            throw new ProgException(ExceptionType.EXCEPT_TOOMANYEVENTS);
        }
        eventQueue.add(new QueuedEvent(cardIndex, event, ticket, parameter));
    }

    @Override
    public int signal(String signal) {
        int cnt = 0;
        for (int i = 0 ; i < cardInfo.length ; i++) {
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

    public void receiveMessage(String name, @Nullable Parameter value) {
        for (int i = 0 ; i < cardInfo.length ; i++) {
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
            if (dt > 60000*60) {
                log("(" + (dt / (60000/60)) + "hours ago)");
            } else if (dt > 60000) {
                log("(" + (dt/60000) + "min ago)");
            } else if (dt > 1000) {
                log("(" + (dt/1000) + "sec ago)");
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
        for (EnumFacing facing : EnumFacing.values()) {
            powerOut[facing.ordinal()] = 0;
        }
        for (BlockPos np : networkNodes.values()) {
            TileEntity te = worldObj.getTileEntity(np);
            if (te instanceof NodeTileEntity) {
                NodeTileEntity tileEntity = (NodeTileEntity) te;
                for (EnumFacing facing : EnumFacing.values()) {
                    tileEntity.setPowerOut(facing, 0);
                }
            }
        }
        gfxOps.clear();
        orderedOps.clear();
        for (CpuCore core : cpuCores) {
            core.setDebug(false);
        }

        markDirty();
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
        markDirty();
    }

    public void exception(ExceptionType exception, RunningProgram program) {
        for (int i = 0 ; i < cardInfo.length ; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_EXCEPTION)) {
                    int index = event.getIndex();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    String code = evaluateStringParameter(compiledOpcode, null, 0);
                    if (exception.getCode().equals(code)) {
                        runOrQueueEvent(i, event, program.getCraftTicket(), null);
                        return;
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
                message = TextFormatting.RED + "[" + gridX + "," + gridY + "] " + exception.getDescription();
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
        while (logMessages.size() > GeneralConfiguration.processorMaxloglines) {
            logMessages.remove();
        }
    }

    private List<String> getDebugLog() {
        List<String> result = new ArrayList<>();
        for (int i = 0 ; i < Math.min(5, cpuCores.size()) ; i++) {
            result.add(TextFormatting.BLUE + "Core " + i + " " + TextFormatting.WHITE + getStatus(i));
        }

        showWithWarn("Event queue: ", eventQueue.size(), 20, result);
        showWithWarn("Waiting items: ", waitingForItems.size(), 20, result);
        showWithWarn("Locks: ", locks.size(), 10, result);

        if (lastException != null) {
            long dt = System.currentTimeMillis() - lastExceptionTime;
            result.add(TextFormatting.RED + lastException);
            if (dt > 60000*60) {
                result.add("(" + (dt / (60000/60)) + "hours ago)");
            } else if (dt > 60000) {
                result.add("(" + (dt/60000) + "min ago)");
            } else if (dt > 1000) {
                result.add("(" + (dt/1000) + "sec ago)");
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
            if (i >= logMessages.size()-n) {
                rc.add(s);
            }
            i++;
        }
        return rc;
    }

    public Parameter[] getVariableArray() {
        return variables;
    }

    public List<Parameter> getVariables() {
        List<Parameter> pars = new ArrayList<>();
        Collections.addAll(pars, variables);
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
        int rf = getEnergyStored(EnumFacing.DOWN);

        for (CpuCore core : cpuCores) {
            if (core.hasProgram()) {
                int rft = GeneralConfiguration.coreRFPerTick[core.getTier()];
                if (rft < rf) {
                    core.run(this);
                    consumeEnergy(rft);
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
            for (int i = ProcessorContainer.SLOT_EXPANSION ; i < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS ; i++) {
                ItemStack expansionStack = inventoryHelper.getStackInSlot(i);
                if (expansionStack != null && expansionStack.getItem() instanceof CPUCoreItem) {
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
                ItemStack cardStack = inventoryHelper.getStackInSlot(i);
                if (cardStack != null) {
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

    @Override
    public int getEnergy(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) te;
            return handler.getEnergyStored(side.getIntSide() == null ? EnumFacing.DOWN : side.getIntSide());
        } else if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, side.getIntSide())) {
            IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, side.getIntSide());
            return energy.getEnergyStored();
        }
        throw new ProgException(EXCEPT_NORF);
    }

    @Override
    public int getMaxEnergy(Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) te;
            return handler.getMaxEnergyStored(side.getIntSide() == null ? EnumFacing.DOWN : side.getIntSide());
        } else if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, side.getIntSide())) {
            IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, side.getIntSide());
            return energy.getMaxEnergyStored();
        }
        throw new ProgException(EXCEPT_NORF);
    }

    @Override
    public int getLiquid(@Nonnull Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getIntSide())) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getIntSide());
            IFluidTankProperties[] properties = handler.getTankProperties();
            if (properties != null && properties.length > 0) {
                if (properties[0].getContents() != null) {
                    return properties[0].getContents().amount;
                }
            }
            return 0;
        }
        throw new ProgException(EXCEPT_NOLIQUID);
    }

    @Override
    public int getMaxLiquid(@Nonnull Inventory side) {
        TileEntity te = getTileEntityAt(side);
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getIntSide())) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getIntSide());
            IFluidTankProperties[] properties = handler.getTankProperties();
            if (properties != null && properties.length > 0) {
                return properties[0].getCapacity();
            }
            return 0;
        }
        throw new ProgException(EXCEPT_NOLIQUID);
    }

    private IStorageScanner getScannerForInv(Inventory inv) {
        if (inv == null) {
            return getStorageScanner();
        } else {
            return null;
        }
    }

    private IItemHandler getHandlerForInv(Inventory inv) {
        if (inv == null) {
            return null;
        } else {
            return getItemHandlerAt(inv);
        }
    }

    public boolean compareNBTTag(@Nonnull ItemStack v1, @Nonnull ItemStack v2, @Nonnull String tag) {
        if ((!v1.hasTagCompound()) || (!v2.hasTagCompound())) {
            return v1.hasTagCompound() == v2.hasTagCompound();
        }
        NBTBase tag1 = v1.getTagCompound().getTag(tag);
        NBTBase tag2 = v2.getTagCompound().getTag(tag);
        if (tag1 == tag2) {
            return true;
        }
        if (tag1 != null) {
            return tag1.equals(tag2);
        }
        return false;
    }



    public int fetchItems(IProgram program, Inventory inv, Integer slot, @Nullable ItemStack itemMatcher, boolean routable, boolean oredict, @Nullable Integer amount, int virtualSlot) {
        IStorageScanner scanner = getScannerForInv(inv);
        IItemHandler handler = getHandlerForInv(inv);

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);

        ItemStack stack = InventoryTools.tryExtractItem(handler, scanner, amount, routable, oredict, itemMatcher, slot);
        if (stack == null) {
            // Nothing to do
            return 0;
        }
        IItemHandler capability = getItemHandler();
        if (capability.insertItem(realSlot, stack, true) != null) {
            // Not enough room. Do nothing
            return 0;
        }
        // All seems ok. Do the real thing now.
        stack = InventoryTools.extractItem(handler, scanner, amount, routable, oredict, itemMatcher, slot);
        capability.insertItem(realSlot, stack, false);
        return stack.stackSize;
    }

    @Override
    @Nullable
    public ItemStack getItemInternal(IProgram program, int virtualSlot) {
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        IItemHandler capability = getItemHandler();
        return capability.getStackInSlot(realSlot);
    }

    public int pushItems(IProgram program, Inventory inv, Integer slot, @Nullable Integer amount, int virtualSlot) {
        IStorageScanner scanner = getScannerForInv(inv);
        IItemHandler handler = getHandlerForInv(inv);

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        IItemHandler itemHandler = getItemHandler();
        ItemStack extracted = itemHandler.extractItem(realSlot, amount == null ? 64 : amount, false);
        if (extracted == null) {
            // Nothing to do
            return 0;
        }
        ItemStack remaining = InventoryTools.insertItem(handler, scanner, extracted, slot);
        if (remaining != null) {
            itemHandler.insertItem(realSlot, remaining, false);
            return extracted.stackSize - remaining.stackSize;
        }
        return extracted.stackSize;
    }

    @Override
    public void sendMessage(IProgram program, int idSlot, String messageName, @Nullable Integer variableSlot) {
        if (!hasNetworkCard()) {
            throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
        }
        if (hasNetworkCard != NetworkCardItem.TIER_ADVANCED) {
            throw new ProgException(EXCEPT_NEEDSADVANCEDNETWORK);
        }

        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realIdSlot = info.getRealSlot(idSlot);

        Integer realVariable = info.getRealVar(variableSlot);

        IItemHandler handler = getItemHandler();
        ItemStack idCard = handler.getStackInSlot(realIdSlot);
        if (idCard == null || !(idCard.getItem() instanceof NetworkIdentifierItem)) {
            throw new ProgException(EXCEPT_NOTANIDENTIFIER);
        }
        NBTTagCompound tagCompound = idCard.getTagCompound();
        if (tagCompound == null || !tagCompound.hasKey("monitorx")) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        int monitordim = tagCompound.getInteger("monitordim");
        int monitorx = tagCompound.getInteger("monitorx");
        int monitory = tagCompound.getInteger("monitory");
        int monitorz = tagCompound.getInteger("monitorz");
        WorldServer world = DimensionManager.getWorld(monitordim);
        BlockPos dest = new BlockPos(monitorx, monitory, monitorz);
        if (!WorldTools.chunkLoaded(world, dest)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        TileEntity te = world.getTileEntity(dest);
        if (!(te instanceof ProcessorTileEntity)) {
            throw new ProgException(EXCEPT_INVALIDDESTINATION);
        }
        ProcessorTileEntity destTE = (ProcessorTileEntity) te;
        destTE.receiveMessage(messageName, realVariable == null ? null : getVariableArray()[realVariable]);
    }

    private void setOp(String id, GfxOp op) {
        if (!hasGraphicsCard) {
            throw new ProgException(EXCEPT_MISSINGGRAPHICSCARD);
        }
        if (!gfxOps.containsKey(id)) {
            if (gfxOps.size() >= GeneralConfiguration.maxGraphicsOpcodes) {
                throw new ProgException(EXCEPT_MISSINGNETWORKCARD);
            }
            orderedOps = null;
        }
        gfxOps.put(id, op);
        markDirty();
    }

    private void sortOps() {
        orderedOps = new ArrayList<>(gfxOps.keySet());
        orderedOps.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
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
    public void gfxClear(IProgram program, @Nullable String id) {
        if (id == null) {
            gfxOps.clear();
            orderedOps.clear();
        } else {
            gfxOps.remove(id);
            orderedOps = null;
        }
        markDirty();
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

    public int getMaxvars() {
        if (maxVars == -1) {
            maxVars = 0;
            hasNetworkCard = -1;
            hasGraphicsCard = false;
            storageCard = -1;
            Item storageCardItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("rftools", "storage_control_module"));
            for (int i = ProcessorContainer.SLOT_EXPANSION ; i < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS ; i++) {
                ItemStack stack = getStackInSlot(i);
                if (stack != null) {
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
        }
        return maxVars;
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
        ((RunningProgram)program).popLoopStack(this);
    }

    public IOpcodeRunnable.OpcodeResult handleLoop(IProgram program, int varIdx, int end) {
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realVar = info.getRealVar(varIdx);
        if (realVar == -1) {
            throw new ProgException(EXCEPT_MISSINGVARIABLE);
        }
        if (realVar >= getMaxvars()) {
            throw new ProgException(EXCEPT_NOTENOUGHVARIABLES);
        }

        int i = TypeConverters.convertToInt(getVariableArray()[realVar].getParameterValue().getValue());
        if (i > end) {
            return IOpcodeRunnable.OpcodeResult.NEGATIVE;
        } else {
            ((RunningProgram)program).pushLoopStack(realVar);
            return IOpcodeRunnable.OpcodeResult.POSITIVE;
        }
    }

    public void setValueInToken(IProgram program, int slot) {
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realSlot = info.getRealSlot(slot);
        ItemStack stack = getItemHandler().getStackInSlot(realSlot);
        if (stack == null || !(stack.getItem() instanceof TokenItem)) {
            throw new ProgException(EXCEPT_NOTATOKEN);
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        Parameter lastValue = program.getLastValue();
        if (lastValue == null) {
            stack.getTagCompound().removeTag("parameter");
        } else {
            NBTTagCompound tag = ParameterTools.writeToNBT(lastValue);
            stack.getTagCompound().setTag("parameter", tag);
        }
    }

    @Nullable
    public Parameter getParameterFromToken(IProgram program, int slot) {
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realSlot = info.getRealSlot(slot);
        ItemStack stack = getItemHandler().getStackInSlot(realSlot);
        if (stack == null || !(stack.getItem() instanceof TokenItem)) {
            throw new ProgException(EXCEPT_NOTATOKEN);
        }
        if (!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound().getCompoundTag("parameter");
        if (tag.hasNoTags()) {
            return null;
        }
        return ParameterTools.readFromNBT(tag);
    }


    @Override
    public void setVariable(IProgram program, int var) {
        CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
        int realVar = info.getRealVar(var);
        if (realVar == -1) {
            throw new ProgException(EXCEPT_MISSINGVARIABLE);
        }
        if (realVar >= getMaxvars()) {
            throw new ProgException(EXCEPT_NOTENOUGHVARIABLES);
        }
        variables[realVar] = program.getLastValue();
    }

    @Nonnull
    @Override
    public <T> T evaluateParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        T parameter = evaluateParameter(compiledOpcode, program, parIndex);
        if (parameter == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return parameter;
    }

    @Override
    @Nullable
    public <T> T evaluateParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        List<Parameter> parameters = compiledOpcode.getParameters();
        if (parIndex >= parameters.size()) {
            return null;
        }
        ParameterValue value = parameters.get(parIndex).getParameterValue();
        if (value.isConstant()) {
            return (T) value.getValue();
        } else if (value.isFunction()) {
            ParameterValue v = value.getFunction().getFunctionRunnable().run(this, program);
            // @todo  What if the function does not return a constant? Do we support that?
            return (T) v.getValue();
        } else {
            CardInfo info = this.cardInfo[((RunningProgram)program).getCardIndex()];
            int realVar = info.getRealVar(value.getVariableIndex());
            if (realVar == -1) {
                throw new ProgException(EXCEPT_MISSINGVARIABLE);
            }
            if (realVar >= getMaxvars()) {
                throw new ProgException(EXCEPT_NOTENOUGHVARIABLES);
            }
            // @todo  What if the variable does not return a constant? Do we support that?
            Parameter par = variables[realVar];
            if (par == null) {
                return null;
            }
            return par.isSet() ? (T) par.getParameterValue().getValue() : null;
        }
    }

    @Nullable
    @Override
    public ItemStack evaluateItemParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToItem(o);
    }

    @Nonnull
    @Override
    public ItemStack evaluateItemParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        ItemStack stack = TypeConverters.convertToItem(o);
        if (stack == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return stack;
    }

    @Nullable
    @Override
    public BlockSide evaluateSideParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToSide(o);
    }

    @Nonnull
    @Override
    public BlockSide evaluateSideParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        BlockSide side = TypeConverters.convertToSide(o);
        if (side == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return side;
    }

    @Nullable
    @Override
    public Inventory evaluateInventoryParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToInventory(o);
    }

    @Nonnull
    @Override
    public Inventory evaluateInventoryParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object o = evaluateParameter(compiledOpcode, program, parIndex);
        Inventory inv = TypeConverters.convertToInventory(o);
        if (inv == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return inv;
    }

    @Override
    public int evaluateIntParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object value = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToInt(value);
    }

    // This version allows returning null
    @Override
    @Nullable
    public Integer evaluateIntegerParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object value = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToInteger(value);
    }

    @Override
    @Nullable
    public String evaluateStringParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object value = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToString(value);
    }

    @Nonnull
    @Override
    public String evaluateStringParameterNonNull(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object value = evaluateParameter(compiledOpcode, program, parIndex);
        String s = TypeConverters.convertToString(value);
        if (s == null) {
            throw new ProgException(EXCEPT_MISSINGPARAMETER);
        }
        return s;
    }

    @Override
    public boolean evaluateBoolParameter(ICompiledOpcode compiledOpcode, IProgram program, int parIndex) {
        Object value = evaluateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToBool(value);
    }

    public int countItemStorage(ItemStack stack, boolean routable, boolean oredict) {
        IStorageScanner scanner = getStorageScanner();
        if (scanner == null) {
            return 0;
        }
        return scanner.countItems(stack, routable, oredict);
    }

    private IStorageScanner getStorageScanner() {
        int card = getStorageCard();
        if (card == -1) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        ItemStack storageStack = getStackInSlot(card);
        if (!storageStack.hasTagCompound()) {
            throw new ProgException(EXCEPT_MISSINGSTORAGECARD);
        }
        NBTTagCompound tagCompound = storageStack.getTagCompound();
        BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
        int dim = tagCompound.getInteger("monitordim");
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!WorldTools.chunkLoaded(world, c)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        TileEntity te = world.getTileEntity(c);
        if (te == null) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }

        if (!(te instanceof IStorageScanner)) {
            throw new ProgException(EXCEPT_MISSINGSTORAGE);
        }
        return (IStorageScanner) te;
    }

    public int countItem(Inventory inv, Integer slot, ItemStack itemMatcher, boolean oredict, boolean routable, IProgram program) {
        if (inv == null) {
            return countItemStorage(itemMatcher, routable, oredict);
        }
        // @todo support oredict here?
        IItemHandler handler = getItemHandlerAt(inv);
        if (slot != null) {
            ItemStack stackInSlot = handler.getStackInSlot(slot);
            return stackInSlot == null ? 0 : stackInSlot.stackSize;
        } else if (itemMatcher != null) {
            return countItemInHandler(itemMatcher, handler);
        } else {
            // Just count all items
            int cnt = 0;
            for (int i = 0 ; i < handler.getSlots() ; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack != null) {
                    cnt += stack.stackSize;
                }
            }
            return cnt;
        }
    }

    private int countItemInHandler(ItemStack itemMatcher, IItemHandler handler) {
        int cnt = 0;
        for (int i = 0 ; i < handler.getSlots() ; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                cnt += stack.stackSize;
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
        return worldObj.getTileEntity(np);
    }

    @Override
    @Nullable
    public BlockPos getPositionAt(@Nullable BlockSide inv) {
        if (inv == null) {
            return null;
        }
        BlockPos p = pos;
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
            return p.offset(inv.getSide());
        }
    }

    @Override
    public IItemHandler getItemHandlerAt(@Nonnull Inventory inv) {
        TileEntity te = getTileEntityAt(inv);
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.getIntSide())) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.getIntSide());
            if (handler != null) {
                return handler;
            }
        } else if (te instanceof ISidedInventory) {
            // Support for old inventory
            ISidedInventory sidedInventory = (ISidedInventory) te;
            return new SidedInvWrapper(sidedInventory, inv.getIntSide());
        } else if (te instanceof IInventory) {
            // Support for old inventory
            IInventory inventory = (IInventory) te;
            return new InvWrapper(inventory);
        }
        throw new ProgException(EXCEPT_INVALIDINVENTORY);
    }

    private boolean isExpansionSlot(int index) {
        return index >= ProcessorContainer.SLOT_EXPANSION && index < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS;
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

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (isCardSlot(index)) {
            removeCard(index-ProcessorContainer.SLOT_CARD);
            cardsDirty = true;
        } else if (isExpansionSlot(index)) {
            coresDirty = true;
            maxVars = -1;
        }
        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (isCardSlot(index)) {
            removeCard(index-ProcessorContainer.SLOT_CARD);
            cardsDirty = true;
        } else if (isExpansionSlot(index)) {
            coresDirty = true;
            maxVars = -1;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    public int getShowHud() {
        return showHud;
    }

    public void setShowHud(int showHud) {
        this.showHud = showHud;
        markDirtyClient();
    }

    @Override
    public void readClientDataFromNBT(NBTTagCompound tagCompound) {
        exclusive = tagCompound.getBoolean("exclusive");
        showHud = tagCompound.getByte("hud");
        readCardInfo(tagCompound);
    }

    @Override
    public void writeClientDataToNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("exclusive", exclusive);
        tagCompound.setByte("hud", (byte) showHud);
        writeCardInfo(tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getInteger("prevIn");
        for (int i = 0 ; i < 6 ; i++) {
            powerOut[i] = tagCompound.getByte("p" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        for (int i = 0 ; i < 6 ; i++) {
            tagCompound.setByte("p" + i, (byte) powerOut[i]);
        }
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        tickCount = tagCompound.getInteger("tickCount");
        channel = tagCompound.getString("channel");
        exclusive = tagCompound.getBoolean("exclusive");
        showHud = tagCompound.getByte("hud");
        if (tagCompound.hasKey("lastExc")) {
            lastException = tagCompound.getString("lastExc");
            lastExceptionTime = tagCompound.getLong("lastExcT");
        } else {
            lastException = null;
            lastExceptionTime = 0;
        }
        readBufferFromNBT(tagCompound, inventoryHelper);

        readCardInfo(tagCompound);
        readCores(tagCompound);
        readEventQueue(tagCompound);
        readLog(tagCompound);
        readVariables(tagCompound);
        readNetworkNodes(tagCompound);
        readCraftingStations(tagCompound);
        readWaitingForItems(tagCompound);
        readLocks(tagCompound);
        readRunningEvents(tagCompound);
        readGraphicsOperations(tagCompound);
    }

    private void readGraphicsOperations(NBTTagCompound tagCompound) {
        gfxOps.clear();
        NBTTagCompound opTag = tagCompound.getCompoundTag("gfxop");
        for (String key : opTag.getKeySet()) {
            gfxOps.put(key, GfxOp.readFromNBT(opTag.getCompoundTag(key)));
        }
        sortOps();
    }

    private void readRunningEvents(NBTTagCompound tagCompound) {
        runningEvents.clear();
        NBTTagList evList = tagCompound.getTagList("singev", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < evList.tagCount() ; i++) {
            NBTTagCompound tag = evList.getCompoundTagAt(i);
            int cardIndex = tag.getInteger("card");
            int eventIndex = tag.getInteger("event");
            runningEvents.add(Pair.of(cardIndex, eventIndex));
        }
    }

    private void readLocks(NBTTagCompound tagCompound) {
        locks.clear();
        NBTTagList lockList = tagCompound.getTagList("locks", Constants.NBT.TAG_STRING);
        for (int i = 0 ; i < lockList.tagCount() ; i++) {
            String name = lockList.getStringTagAt(i);
            locks.add(name);
        }
    }

    private void readWaitingForItems(NBTTagCompound tagCompound) {
        waitingForItems.clear();
        NBTTagList waitingList = tagCompound.getTagList("waiting", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < waitingList.tagCount() ; i++) {
            NBTTagCompound tag = waitingList.getCompoundTagAt(i);
            String ticket = tag.getString("ticket");

            ItemStack stack;
            if (tag.hasKey("item")) {
                stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("item"));
            } else {
                stack = null;
            }

            Inventory inventory;
            if (tag.hasKey("inv")) {
                inventory = InventoryUtil.readFromNBT(tag.getCompoundTag("inv"));
            } else {
                inventory = null;
            }

            WaitForItem waitForItem = new WaitForItem(ticket, stack, inventory);
            waitingForItems.add(waitForItem);
        }
    }


    private void readCraftingStations(NBTTagCompound tagCompound) {
        craftingStations.clear();
        NBTTagList stationList = tagCompound.getTagList("stations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < stationList.tagCount() ; i++) {
            NBTTagCompound tag = stationList.getCompoundTagAt(i);
            BlockPos nodePos = new BlockPos(tag.getInteger("nodex"), tag.getInteger("nodey"), tag.getInteger("nodez"));
            craftingStations.add(nodePos);
        }
    }

    private void readNetworkNodes(NBTTagCompound tagCompound) {
        networkNodes.clear();
        NBTTagList networkList = tagCompound.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < networkList.tagCount() ; i++) {
            NBTTagCompound tag = networkList.getCompoundTagAt(i);
            String name = tag.getString("name");
            BlockPos nodePos = new BlockPos(tag.getInteger("nodex"), tag.getInteger("nodey"), tag.getInteger("nodez"));
            networkNodes.put(name, nodePos);
        }
    }

    private void readVariables(NBTTagCompound tagCompound) {
        for (int i = 0 ; i < MAXVARS ; i++) {
            variables[i] = null;
        }
        NBTTagList varList = tagCompound.getTagList("vars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < varList.tagCount() ; i++) {
            NBTTagCompound var = varList.getCompoundTagAt(i);
            int index = var.getInteger("varidx");
            variables[index] = ParameterTools.readFromNBT(var);
        }
    }

    private void readLog(NBTTagCompound tagCompound) {
        logMessages.clear();
        NBTTagList logList = tagCompound.getTagList("log", Constants.NBT.TAG_STRING);
        for (int i = 0 ; i < logList.tagCount() ; i++) {
            logMessages.add(logList.getStringTagAt(i));
        }
    }

    private void readCores(NBTTagCompound tagCompound) {
        NBTTagList coreList = tagCompound.getTagList("cores", Constants.NBT.TAG_COMPOUND);
        cpuCores.clear();
        coresDirty = false;
        for (int i = 0 ; i < coreList.tagCount() ; i++) {
            CpuCore core = new CpuCore();
            core.readFromNBT(coreList.getCompoundTagAt(i));
            cpuCores.add(core);
        }
        if (cpuCores.isEmpty()) {
            coresDirty = true;
        }
    }

    private void readEventQueue(NBTTagCompound tagCompound) {
        eventQueue.clear();
        NBTTagList eventQueueList = tagCompound.getTagList("events", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < eventQueueList.tagCount() ; i++) {
            NBTTagCompound tag = eventQueueList.getCompoundTagAt(i);
            int card = tag.getInteger("card");
            int index = tag.getInteger("index");
            boolean single = tag.getBoolean("single");
            String ticket = tag.hasKey("ticket") ? tag.getString("ticket") : null;
            Parameter parameter = null;
            if (tag.hasKey("parameter")) {
                parameter = ParameterTools.readFromNBT(tag.getCompoundTag("parameter"));
            }
            eventQueue.add(new QueuedEvent(card, new CompiledEvent(index, single), ticket, parameter));
        }
    }

    private void readCardInfo(NBTTagCompound tagCompound) {
        NBTTagList cardInfoList = tagCompound.getTagList("cardInfo", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < cardInfoList.tagCount() ; i++) {
            cardInfo[i] = CardInfo.readFromNBT(cardInfoList.getCompoundTagAt(i));
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("tickCount", tickCount);
        tagCompound.setString("channel", channel == null ? "" : channel);
        tagCompound.setBoolean("exclusive", exclusive);
        tagCompound.setByte("hud", (byte) showHud);
        if (lastException != null) {
            tagCompound.setString("lastExc", lastException);
            tagCompound.setLong("lastExcT", lastExceptionTime);
        }
        writeBufferToNBT(tagCompound, inventoryHelper);

        writeCardInfo(tagCompound);
        writeCores(tagCompound);
        writeEventQueue(tagCompound);
        writeLog(tagCompound);
        writeVariables(tagCompound);
        writeNetworkNodes(tagCompound);
        writeCraftingStations(tagCompound);
        writeWaitingForItems(tagCompound);
        writeLocks(tagCompound);
        writeRunningEvents(tagCompound);
        writeGraphicsOperation(tagCompound);
    }

    private void writeGraphicsOperation(NBTTagCompound tagCompound) {
        NBTTagCompound opTag = new NBTTagCompound();
        for (Map.Entry<String, GfxOp> entry : gfxOps.entrySet()) {
            opTag.setTag(entry.getKey(), entry.getValue().writeToNBT());
        }
        tagCompound.setTag("gfxop", opTag);
    }

    private void writeRunningEvents(NBTTagCompound tagCompound) {
        NBTTagList evList = new NBTTagList();
        for (Pair<Integer, Integer> pair : runningEvents) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("card", pair.getLeft());
            tag.setInteger("event", pair.getRight());
            evList.appendTag(tag);
        }
        tagCompound.setTag("singev", evList);
    }

    private void writeLocks(NBTTagCompound tagCompound) {
        NBTTagList lockList = new NBTTagList();
        for (String name : locks) {
            lockList.appendTag(new NBTTagString(name));
        }
        tagCompound.setTag("locks", lockList);
    }

    private void writeWaitingForItems(NBTTagCompound tagCompound) {
        NBTTagList waitingList = new NBTTagList();
        for (WaitForItem waitingForItem : waitingForItems) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("ticket", waitingForItem.getTicket());
            if (waitingForItem.getInventory() != null) {
                tag.setTag("inv", InventoryUtil.writeToNBT(waitingForItem.getInventory()));
            }
            if (waitingForItem.getItemStack() != null) {
                tag.setTag("item", waitingForItem.getItemStack().serializeNBT());
            }
            waitingList.appendTag(tag);
        }
        tagCompound.setTag("waiting", waitingList);
    }


    private void writeCraftingStations(NBTTagCompound tagCompound) {
        NBTTagList stationList = new NBTTagList();
        for (BlockPos pos : craftingStations) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("nodex", pos.getX());
            tag.setInteger("nodey", pos.getY());
            tag.setInteger("nodez", pos.getZ());
            stationList.appendTag(tag);
        }
        tagCompound.setTag("stations", stationList);
    }

    private void writeNetworkNodes(NBTTagCompound tagCompound) {
        NBTTagList networkList = new NBTTagList();
        for (Map.Entry<String, BlockPos> entry : networkNodes.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", entry.getKey());
            tag.setInteger("nodex", entry.getValue().getX());
            tag.setInteger("nodey", entry.getValue().getY());
            tag.setInteger("nodez", entry.getValue().getZ());
            networkList.appendTag(tag);
        }
        tagCompound.setTag("nodes", networkList);
    }

    private void writeVariables(NBTTagCompound tagCompound) {
        NBTTagList varList = new NBTTagList();
        for (int i = 0 ; i < MAXVARS ; i++) {
            if (variables[i] != null) {
                NBTTagCompound var = ParameterTools.writeToNBT(variables[i]);
                var.setInteger("varidx", i);
                varList.appendTag(var);
            }
        }
        tagCompound.setTag("vars", varList);
    }

    private void writeLog(NBTTagCompound tagCompound) {
        NBTTagList logList = new NBTTagList();
        for (String message : logMessages) {
            logList.appendTag(new NBTTagString(message));
        }
        tagCompound.setTag("log", logList);
    }

    private void writeCores(NBTTagCompound tagCompound) {
        NBTTagList coreList = new NBTTagList();
        for (CpuCore core : cpuCores) {
            coreList.appendTag(core.writeToNBT());
        }
        tagCompound.setTag("cores", coreList);
    }

    private void writeEventQueue(NBTTagCompound tagCompound) {
        NBTTagList eventQueueList = new NBTTagList();
        for (QueuedEvent queuedEvent : eventQueue) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("card", queuedEvent.getCardIndex());
            tag.setInteger("index", queuedEvent.getCompiledEvent().getIndex());
            tag.setBoolean("single", queuedEvent.getCompiledEvent().isSingle());
            if (queuedEvent.getTicket() != null) {
                tag.setString("ticket", queuedEvent.getTicket());
            }
            if (queuedEvent.getParameter() != null) {
                NBTTagCompound parTag = ParameterTools.writeToNBT(queuedEvent.getParameter());
                tag.setTag("parameter", parTag);
            }
            eventQueueList.appendTag(tag);
        }
        tagCompound.setTag("events", eventQueueList);
    }

    private void writeCardInfo(NBTTagCompound tagCompound) {
        NBTTagList cardInfoList = new NBTTagList();
        for (CardInfo info : cardInfo) {
            cardInfoList.appendTag(info.writeToNBT());
        }
        tagCompound.setTag("cardInfo", cardInfoList);
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
        ItemStack cardStack = inventoryHelper.getStackInSlot(index + ProcessorContainer.SLOT_CARD);
        if (card == null && cardStack != null) {
            card = CompiledCard.compile(ProgramCardInstance.parseInstance(cardStack));
            cardInfo[index].setCompiledCard(card);
        }
        return card;
    }

    private void allocate(int card, int itemAlloc, int varAlloc) {
        cardInfo[card].setItemAllocation(itemAlloc);
        cardInfo[card].setVarAllocation(varAlloc);
        markDirty();
    }

    public void showNetworkInfo() {
        log("Channel: " + channel);
        log("Nodes: " + networkNodes.size());
    }

    public void listNodes() {
        if (networkNodes.isEmpty()) {
            log("No nodes!");
        } else {
            for (Map.Entry<String, BlockPos> entry : networkNodes.entrySet()) {
                log("Node " + entry.getKey() + " at " + BlockPosTools.toString(entry.getValue()));
            }
        }
    }

    public void setupNetwork(String name) {
        channel = name;
        markDirty();
    }

    public void redstoneNodeChange(int previousMask, int newMask, String node) {
        for (int i = 0 ; i < cardInfo.length ; i++) {
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
                    BlockPos n = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    TileEntity te = worldObj.getTileEntity(n);
                    if (te instanceof NodeTileEntity) {
                        NodeTileEntity node = (NodeTileEntity) te;
                        if (channel.equals(node.getChannelName())) {
                            if (node.getNodeName() == null || node.getNodeName().isEmpty()) {
                                log("Node is missing a name!");
                            } else {
                                networkNodes.put(node.getNodeName(), n);
                                node.setProcessor(getPos());
                            }
                        }
                    } else if (te instanceof CraftingStationTileEntity) {
                        CraftingStationTileEntity craftingStation = (CraftingStationTileEntity) te;
                        craftingStation.registerProcessor(pos);
                        craftingStations.add(n);
                    }
                }
            }
        }
        log("Found " + networkNodes.size() + " node(s)");
        log("Found " + craftingStations.size() + " crafting station(s)");
        markDirty();
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_ALLOCATE.equals(command)) {
            int card = args.get("card").getInteger();
            int itemAlloc = args.get("items").getInteger();
            int varAlloc = args.get("vars").getInteger();
            allocate(card, itemAlloc, varAlloc);
            return true;
        } else if (CMD_CLEARLOG.equals(command)) {
            Commands.executeCommand(this, args.get("cmd").getString());
            return true;
        } else if (CMD_SETEXCLUSIVE.equals(command)) {
            boolean v = args.get("v").getBoolean();
            setExclusive(v);
            return true;
        } else if (CMD_SETHUDMODE.equals(command)) {
            int v = args.get("v").getInteger();
            setShowHud(v);
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
        if (CMD_GETLOG.equals(command)) {
            return getLog();
        } else if (CMD_GETDEBUGLOG.equals(command)) {
            return getDebugLog();
        } else if (CMD_GETVARS.equals(command)) {
            return getVariables();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETLOG.equals(command)) {
            clientLog = new ArrayList<>(list);
            return true;
        } else if (CLIENTCMD_GETDEBUGLOG.equals(command)) {
            clientDebugLog = new ArrayList<>(list);
            return true;
        } else if (CLIENTCMD_GETVARS.equals(command)) {
            GuiProcessor.storeVarsForClient(list);
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        return new AxisAlignedBB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 21, zCoord + 1);
    }
}

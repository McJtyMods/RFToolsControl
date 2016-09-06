package mcjty.rftoolscontrol.blocks.processor;

import cofh.api.energy.IEnergyHandler;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.api.storage.IStorageScanner;
import mcjty.rftoolscontrol.blocks.node.NodeTileEntity;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import mcjty.rftoolscontrol.items.CPUCoreItem;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.TypeConverters;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.Opcodes;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import mcjty.rftoolscontrol.logic.running.CpuCore;
import mcjty.rftoolscontrol.logic.running.RunningProgram;
import mcjty.rftoolscontrol.network.PacketGetLog;
import mcjty.rftoolscontrol.network.PacketGetVariables;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3*8;
    public static final int EXPANSION_SLOTS = 4*4;
    public static final int MAXVARS = 32;

    public static final String CMD_ALLOCATE = "allocate";
    public static final String CMD_CLEARLOG = "clearLog";
    public static final String CMD_GETLOG = "getLog";
    public static final String CLIENTCMD_GETLOG = "getLog";
    public static final String CMD_GETVARS = "getVars";
    public static final String CLIENTCMD_GETVARS = "getVars";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProcessorContainer.factory, ProcessorContainer.SLOTS);
    private boolean working = false;
    private List<CpuCore> cpuCores = new ArrayList<>();

    // If true some cards might need compiling
    private boolean cardsDirty = true;
    // If true some cpu cores need updating
    private boolean coresDirty = true;

    private int maxVars = -1;   // If -1 we need updating
    private boolean hasNetworkCard = false;
    private int storageCard = -2;   // -2 is unknown

    private String channel = "";
    private Map<String, BlockPos> networkNodes = new HashMap<>();

    // Bitmask for all six sides
    private int prevIn = 0;
    private int powerOut[] = new int[] { 0, 0, 0, 0, 0, 0 };

    private int tickCount = 0;

    private Parameter[] variables = new Parameter[MAXVARS];

    private CardInfo[] cardInfo = new CardInfo[CARD_SLOTS];

    private Queue<Pair<Integer, CompiledEvent>> eventQueue = new ArrayDeque<>();        // Integer == card index

    private Queue<String> logMessages = new ArrayDeque<>();

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

    public Parameter getParameter(int idx) {
        return variables[idx];
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private BlockPos getAdjacentPosition(@Nonnull BlockSide side, @Nonnull RunningProgram program) {
        BlockPos p;
        if (side.getNodeName() != null && !side.getNodeName().isEmpty()) {
            p = networkNodes.get(side.getNodeName());
            if (p == null) {
                exception("Missing node!", program);
                return null;
            }
            TileEntity te = worldObj.getTileEntity(p);
            if (!(te instanceof NodeTileEntity)) {
                exception("Missing node!", program);
                return null;
            }
        } else {
            p = pos;
        }
        return p;
    }

    public int readRedstoneIn(@Nonnull BlockSide side, @Nonnull RunningProgram program) {
        EnumFacing facing = side.getSide();
        BlockPos p = getAdjacentPosition(side, program);
        if (p == null) {
            return 0;
        }
        return worldObj.getRedstonePower(p.offset(facing), facing);
    }

    public void setPowerOut(@Nonnull BlockSide side, int level, RunningProgram program) {
        EnumFacing facing = side.getSide();
        BlockPos p = getAdjacentPosition(side, program);
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
            boolean old = working;
            working = true; // @todo
            if (working != old) {
                markDirtyClient();
            }

            if (working) {
                process();
            }

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
        Pair<Integer, CompiledEvent> pair = eventQueue.peek();
        if (pair != null) {
            CpuCore core = findAvailableCore();
            if (core != null) {
                eventQueue.remove();
                RunningProgram program = new RunningProgram(pair.getKey());
                program.setCurrent(pair.getRight().getIndex());
                core.startProgram(program);
            }
        }
    }

    private void handleEvents() {
        for (int i = 0 ; i < cardInfo.length ; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                int redstoneOnMask = powerLevel & ~prevIn;
                if (redstoneOnMask != 0) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_ON)) {
                        int index = event.getIndex();
                        CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                        BlockSide side = evalulateParameter(compiledOpcode, null, 0);
                        EnumFacing facing = side == null ? null : side.getSide();
                        if (facing == null || ((redstoneOnMask >> facing.ordinal()) & 1) == 1) {
                            runOrQueueEvent(i, event);
                        }
                    }
                }
                int redstoneOffMask = prevIn & ~powerLevel;
                if (redstoneOffMask != 0) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_OFF)) {
                        int index = event.getIndex();
                        CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                        BlockSide side = evalulateParameter(compiledOpcode, null, 0);
                        EnumFacing facing = side == null ? null : side.getSide();
                        if (facing == null || ((redstoneOffMask >> facing.ordinal()) & 1) == 1) {
                            runOrQueueEvent(i, event);
                        }
                    }
                }

                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_TIMER)) {
                    int index = event.getIndex();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    int ticks = evalulateParameter(compiledOpcode, null, 0);
                    if (tickCount % ticks == 0) {
                        runOrQueueEvent(i, event);
                    }
                }
            }
        }
    }

    private void runOrQueueEvent(int cardIndex, CompiledEvent event) {
        CpuCore core = findAvailableCore();
        if (core == null) {
            // No available core
            eventQueue.add(Pair.of(cardIndex, event));
        } else {
            RunningProgram program = new RunningProgram(cardIndex);
            program.setCurrent(event.getIndex());
            core.startProgram(program);
        }
    }

    public void signal(String signal) {
        for (int i = 0 ; i < cardInfo.length ; i++) {
            CardInfo info = cardInfo[i];
            CompiledCard compiledCard = info.getCompiledCard();
            if (compiledCard != null) {
                for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_SIGNAL)) {
                    int index = event.getIndex();
                    CompiledOpcode compiledOpcode = compiledCard.getOpcodes().get(index);
                    String sig = evalulateParameter(compiledOpcode, null, 0);
                    if (signal.equals(sig)) {
                        runOrQueueEvent(i, event);
                    }
                }
            }
        }
    }

    public void clearLog() {
        logMessages.clear();
    }

    public void exception(String message, RunningProgram program) {
        if (program != null) {
            CompiledOpcode opcode = program.getCurrentOpcode(this);
            int gridX = opcode.getGridX();
            int gridY = opcode.getGridY();
            message = "[" + gridX + "," + gridY + "] " + message;
        }
        log(message);
    }

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

    private List<PacketGetLog.StringConverter> getLog() {
        return logMessages.stream().map(s -> new PacketGetLog.StringConverter(s)).collect(Collectors.toList());
    }

    private List<PacketGetVariables.ParameterConverter> getVariables() {
        List<PacketGetVariables.ParameterConverter> pars = new ArrayList<>();
        for (Parameter variable : variables) {
            pars.add(new PacketGetVariables.ParameterConverter(variable));
        }
        return pars;
    }

    public List<CpuCore> getCpuCores() {
        return cpuCores;
    }

    private CpuCore findAvailableCore() {
        for (CpuCore core : cpuCores) {
            if (!core.hasProgram()) {
                return core;
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
                        System.out.println("compiled = " + compiled);
                        cardInfo[cardIndex].setCompiledCard(compiled);
                    }
                }
            }
        }
    }

    public int getEnergy(Inventory side, RunningProgram program) {
        TileEntity te = getTileEntityAt(side, program);
        if (te instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) te;
            return handler.getEnergyStored(side.getIntSide() == null ? EnumFacing.DOWN : side.getIntSide());
        }
        exception("No RF support!", program);
        return 0;
    }

    public int getMaxEnergy(Inventory side, RunningProgram program) {
        TileEntity te = getTileEntityAt(side, program);
        if (te instanceof IEnergyHandler) {
            IEnergyHandler handler = (IEnergyHandler) te;
            return handler.getMaxEnergyStored(side.getIntSide() == null ? EnumFacing.DOWN : side.getIntSide());
        }
        exception("No RF support!", program);
        return 0;
    }

    @Nonnull
    private static List<Integer> extractFromHandlerSimulate(IItemHandler handler, Integer slot, ItemStack itemMatcher, int amount) {
        List<Integer> result = new ArrayList<>();

        if (slot == null) {
            // No slot specified, check if we have an item
            if (itemMatcher == null) {
                // Just find the first available stack
                for (int i = 0 ; i < handler.getSlots() ; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack != null && amount <= stack.stackSize) {
                        if (handler.extractItem(i, amount, true) != null) {
                            result.add(i);
                            return result;
                        }
                    }
                }
                // If we come here then we failed
                return Collections.emptyList();
            } else {
                for (int i = 0 ; i < handler.getSlots() ; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                        ItemStack r = handler.extractItem(i, amount, true);
                        if (r != null) {
                            result.add(i);
                            amount -= r.stackSize;
                        }
                        if (amount <= 0) {
                            return result;
                        }
                    }
                }
                // If we come here then we failed
                return Collections.emptyList();
            }
        } else {
            if (handler.extractItem(slot, amount, true) != null) {
                result.add(slot);
            }
            return result;
        }
    }

    @Nonnull
    private static ItemStack extractFromHandler(IItemHandler handler, List<Integer> slots, int amount) {
        ItemStack result = null;
        for (Integer i : slots) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack != null && amount <= stack.stackSize) {
                ItemStack rc = handler.extractItem(i, amount, false);
                if (rc != null) {
                    if (result == null) {
                        result = rc;
                    } else {
                        result.stackSize += rc.stackSize;
                    }
                }
            }
        }
        return result;
    }

    public int fetchItemsStorage(RunningProgram program, @Nullable ItemStack itemMatcher,
                                 boolean routable, boolean oredict,
                                 int amount, int virtualSlot) {
        IStorageScanner scanner = getStorageScanner(program);
        if (scanner == null) {
            return 0;
        }

        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            exception("No slot!", program);
            return 0;
        }

        ItemStack result = scanner.requestItem(itemMatcher, amount, routable, oredict);
        if (result == null) {
            return 0;
        }
        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack overflow = capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, result, false);
        if (overflow != null) {
            int lostItems = scanner.insertItem(overflow);
            // 'lostItems' don't fit in the processor and they don't fit in the storage
            // Throw them in the world
            if (lostItems > 0) {
                overflow.stackSize = lostItems;
                EntityItem entityItem = new EntityItem(worldObj, pos.getX(), pos.getY(), pos.getZ(), overflow);
                worldObj.spawnEntityInWorld(entityItem);
            }
            return result.stackSize - overflow.stackSize;
        }
        return result.stackSize;
    }

    public void fetchItems(RunningProgram program, Inventory inv, Integer slot, @Nullable ItemStack itemMatcher, int amount, int virtualSlot) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            exception("No slot!", program);
            return;
        }
        IItemHandler handler = getItemHandlerAt(inv, program);
        if (handler == null) {
            exception("Invalid inventory!", program);
            return;
        }

        List<Integer> extracted = extractFromHandlerSimulate(handler, slot, itemMatcher, amount);
        if (extracted.isEmpty()) {
            // Nothing to do
            return;
        }
        // Make a new itemstack as it would be inserted
        ItemStack testExtract = handler.extractItem(extracted.get(0), 1, true).copy();
        testExtract.stackSize = amount;

        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, testExtract, true) != null) {
            // Not enough room. Do nothing
            return;
        }

        // All seems ok. Do the real thing now.
        ItemStack extractItem = extractFromHandler(handler, extracted, amount);
        capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, extractItem, false);
    }

    public ItemStack getItemInternal(RunningProgram program, int virtualSlot) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            exception("No slot!", program);
            return null;
        }
        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        return capability.getStackInSlot(realSlot + ProcessorContainer.SLOT_BUFFER);
    }

    public int pushItemsStorage(RunningProgram program, int amount, int virtualSlot) {
        IStorageScanner scanner = getStorageScanner(program);
        if (scanner == null) {
            return 0;
        }

        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            exception("No slot!", program);
            return 0;
        }

        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack extracted = capability.extractItem(realSlot + ProcessorContainer.SLOT_BUFFER, amount, false);
        if (extracted == null) {
            // Nothing to do
            return 0;
        }

        int remaining = scanner.insertItem(extracted);
        if (remaining > 0) {
            int inserted = extracted.stackSize - remaining;
            extracted.stackSize = remaining;
            capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, extracted, false);
            return remaining;
        }

        return extracted.stackSize;
    }

    public void pushItems(RunningProgram program, Inventory inv, Integer slot, int amount, int virtualSlot) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            exception("No slot!", program);
            return;
        }
        IItemHandler handler = getItemHandlerAt(inv, program);
        if (handler == null) {
            exception("Invalid inventory!", program);
            return;
        }
        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack extracted = capability.extractItem(realSlot + ProcessorContainer.SLOT_BUFFER, amount, true);
        if (extracted == null) {
            // Nothing to do
            return;
        }
        if (slot == null) {
            if (ItemHandlerHelper.insertItem(handler, extracted, true) != null) {
                // Not enough room. Do nothing
                return;
            }
        } else {
            if (handler.insertItem(slot, extracted, true) != null) {
                // Not enough room. Do nothing
                return;
            }
        }

        // All seems ok. Do the real thing now.
        extracted = capability.extractItem(realSlot + ProcessorContainer.SLOT_BUFFER, amount, false);
        if (slot == null) {
            ItemHandlerHelper.insertItem(handler, extracted, false);
        } else {
            handler.insertItem(slot, extracted, false);
        }
    }

    public int getMaxvars() {
        if (maxVars == -1) {
            maxVars = 0;
            hasNetworkCard = false;
            storageCard = -1;
            Item storageCardItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("rftools", "storage_control_module"));
            for (int i = ProcessorContainer.SLOT_EXPANSION ; i < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS ; i++) {
                ItemStack stack = getStackInSlot(i);
                if (stack != null) {
                    if (stack.getItem() == ModItems.networkCardItem) {
                        hasNetworkCard = true;
                    } else if (stack.getItem() == ModItems.ramChipItem) {
                        maxVars += 8;
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
        return hasNetworkCard;
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


    public void setVariable(RunningProgram program, int var) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realVar = info.getRealVar(var);
        if (realVar == -1) {
            exception("No variable!", program);
            return;
        }
        if (realVar >= getMaxvars()) {
            exception("Not enough variables!", program);
            return;
        }
        variables[realVar] = program.getLastValue();
    }

    public <T> T evalulateParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        ParameterValue value = compiledOpcode.getParameters().get(parIndex).getParameterValue();
        if (value.isConstant()) {
            return (T) value.getValue();
        } else if (value.isFunction()) {
            ParameterValue v = value.getFunction().getFunctionRunnable().run(this, program, value.getFunction());
            // @todo  What if the function does not return a constant? Do we support that?
            return (T) v.getValue();
        } else {
            CardInfo info = this.cardInfo[program.getCardIndex()];
            int realVar = info.getRealVar(value.getVariableIndex());
            if (realVar == -1) {
                exception("No variable!", program);
                return null;
            }
            if (realVar >= getMaxvars()) {
                exception("Not enough variables!", program);
                return null;
            }
            // @todo  What if the variable does not return a constant? Do we support that?
            Parameter par = variables[realVar];
            if (par == null) {
                return null;
            }
            return par.isSet() ? (T) par.getParameterValue().getValue() : null;
        }
    }

    public int evaluateIntParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        Object value = evalulateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToInt(value);
    }

    // This version allows returning null
    public Integer evaluateIntegerParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        Object value = evalulateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToInteger(value);
    }

    public String evalulateStringParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        Object value = evalulateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToString(value);
    }

    public boolean evalulateBoolParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        Object value = evalulateParameter(compiledOpcode, program, parIndex);
        return TypeConverters.convertToBool(value);
    }

    public int countItemStorage(ItemStack stack, boolean routable, boolean oredict, RunningProgram program) {
        IStorageScanner scanner = getStorageScanner(program);
        if (scanner == null) {
            return 0;
        }
        return scanner.countItems(stack, routable, oredict);
    }

    private IStorageScanner getStorageScanner(RunningProgram program) {
        int card = getStorageCard();
        if (card == -1) {
            exception("No storage card!", program);
            return null;
        }
        ItemStack storageStack = getStackInSlot(card);
        if (!storageStack.hasTagCompound()) {
            exception("Invalid storage card!", program);
            return null;
        }
        NBTTagCompound tagCompound = storageStack.getTagCompound();
        BlockPos c = new BlockPos(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
        int dim = tagCompound.getInteger("monitordim");
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            exception("Can't find storage!", program);
            return null;
        }

        if (!WorldTools.chunkLoaded(world, c)) {
            exception("Can't find storage!", program);
            return null;
        }

        TileEntity te = world.getTileEntity(c);
        if (te == null) {
            exception("Storage scanner invalid!", program);
            return null;
        }

        if (!(te instanceof IStorageScanner)) {
            exception("Storage scanner invalid!", program);
            return null;
        }
        return (IStorageScanner) te;
    }

    public int countItem(Inventory inv, Integer slot, ItemStack itemMatcher, RunningProgram program) {
        IItemHandler handler = getItemHandlerAt(inv, program);
        if (handler != null) {
            if (slot != null) {
                ItemStack stackInSlot = handler.getStackInSlot(slot);
                return stackInSlot == null ? 0 : stackInSlot.stackSize;
            } else if (itemMatcher != null) {
                int cnt = 0;
                for (int i = 0 ; i < handler.getSlots() ; i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack != null && ItemStack.areItemsEqual(stack, itemMatcher)) {
                        cnt += stack.stackSize;
                    }
                }
                return cnt;
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
        } else {
            // @todo error?
        }
        return 0;
    }

    public TileEntity getTileEntityAt(Inventory inv, RunningProgram program) {
        BlockPos p = pos;
        if (inv.hasNodeName()) {
            if (!hasNetworkCard()) {
                exception("No network card!", program);
                return null;
            }
            p = networkNodes.get(inv.getNodeName());
            if (p == null) {
                exception("Can't find node: " + inv.getNodeName(), program);
                return null;
            }
        }
        BlockPos np = p.offset(inv.getSide());
        return worldObj.getTileEntity(np);
    }

    public IItemHandler getItemHandlerAt(Inventory inv, RunningProgram program) {
        TileEntity te = getTileEntityAt(inv, program);
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.getIntSide())) {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.getIntSide());
        }
        return null;
    }

    private boolean isExpansionSlot(int index) {
        return index >= ProcessorContainer.SLOT_EXPANSION && index < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS;
    }

    private boolean isCardSlot(int index) {
        return index >= ProcessorContainer.SLOT_CARD && index < ProcessorContainer.SLOT_CARD + CARD_SLOTS;
    }

    private void stopPrograms(int cardIndex) {
        for (CpuCore core : cpuCores) {
            if (core.hasProgram() && core.getProgram().getCardIndex() == cardIndex) {
                core.stopProgram();
            }
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (isCardSlot(index)) {
            cardInfo[index-ProcessorContainer.SLOT_CARD].setCompiledCard(null);
            stopPrograms(index-ProcessorContainer.SLOT_CARD);
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
            cardInfo[index-ProcessorContainer.SLOT_CARD].setCompiledCard(null);
            stopPrograms(index-ProcessorContainer.SLOT_CARD);
            cardsDirty = true;
        } else if (isExpansionSlot(index)) {
            coresDirty = true;
            maxVars = -1;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public void readClientDataFromNBT(NBTTagCompound tagCompound) {
        working = tagCompound.getBoolean("working");
        readCardInfo(tagCompound);
    }

    @Override
    public void writeClientDataToNBT(NBTTagCompound tagCompound) {
        tagCompound.setBoolean("working", working);
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
        working = tagCompound.getBoolean("working");
        tickCount = tagCompound.getInteger("tickCount");
        channel = tagCompound.getString("channel");
        readBufferFromNBT(tagCompound, inventoryHelper);

        readCardInfo(tagCompound);
        readCores(tagCompound);
        readEventQueue(tagCompound);
        readLog(tagCompound);
        readVariables(tagCompound);
        readNetworkNodes(tagCompound);
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
            variables[index] = Parameter.readFromNBT(var);
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
            eventQueue.add(Pair.of(card, new CompiledEvent(index)));
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
        tagCompound.setBoolean("working", working);
        tagCompound.setInteger("tickCount", tickCount);
        tagCompound.setString("channel", channel == null ? "" : channel);
        writeBufferToNBT(tagCompound, inventoryHelper);

        writeCardInfo(tagCompound);
        writeCores(tagCompound);
        writeEventQueue(tagCompound);
        writeLog(tagCompound);
        writeVariables(tagCompound);
        writeNetworkNodes(tagCompound);
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
                NBTTagCompound var = Parameter.writeToNBT(variables[i]);
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
        for (Pair<Integer, CompiledEvent> pair : eventQueue) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("card", pair.getKey());
            tag.setInteger("index", pair.getRight().getIndex());
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

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (worldObj.isRemote) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
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
        ItemStack cardStack = inventoryHelper.getStackInSlot(index + ProcessorContainer.SLOT_CARD);
        if (card == null && cardStack != null) {
            card = CompiledCard.compile(ProgramCardInstance.parseInstance(cardStack));
            System.out.println("compiled2 = " + card);
            cardInfo[index].setCompiledCard(card);
        }
        return card;
    }

    public boolean isWorking() {
        return working && isMachineEnabled();
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
        log("Ready to scan");
    }

    public void scanNodes() {
        if (channel == null || channel.isEmpty()) {
            log("Setup a channel first!");
            return;
        }
        networkNodes.clear();
        for (int x = -8 ; x <= 8 ; x++) {
            for (int y = -8 ; y <= 8 ; y++) {
                for (int z = -8 ; z <= 8 ; z++) {
                    BlockPos n = new BlockPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    TileEntity te = worldObj.getTileEntity(n);
                    if (te instanceof NodeTileEntity) {
                        NodeTileEntity node = (NodeTileEntity) te;
                        if (channel.equals(node.getChannelName())) {
                            if (node.getNodeName() == null || node.getNodeName().isEmpty()) {
                                log("Node is missing a name!");
                            } else {
                                networkNodes.put(node.getNodeName(), n);
                            }
                        }
                    }
                }
            }
        }
        log("Found " + networkNodes.size() + " node(s)");
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
            GuiProcessor.storeLogForClient(list);
            return true;
        } else if (CLIENTCMD_GETVARS.equals(command)) {
            GuiProcessor.storeVarsForClient(list);
            return true;
        }
        return false;
    }


}

package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import mcjty.rftoolscontrol.items.ModItems;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.compiled.CompiledEvent;
import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.Opcodes;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import mcjty.rftoolscontrol.logic.running.CpuCore;
import mcjty.rftoolscontrol.logic.running.RunningProgram;
import mcjty.rftoolscontrol.network.PacketGetLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3*8;
    public static final int EXPANSION_SLOTS = 4*4;

    public static final String CMD_ALLOCATE = "allocate";
    public static final String CMD_CLEARLOG = "clearLog";
    public static final String CMD_GETLOG = "getLog";
    public static final String CLIENTCMD_GETLOG = "getLog";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProcessorContainer.factory, ProcessorContainer.SLOTS);
    private boolean working = false;
    private List<CpuCore> cpuCores = new ArrayList<>();

    // If true some cards might need compiling
    private boolean cardsDirty = true;
    // If true some cpu cores need updating
    private boolean coresDirty = true;

    // @todo, do this for all six sides
    private int prevIn = 0;

    private int tickCount = 0;

    private CardInfo[] cardInfo = new CardInfo[CARD_SLOTS];

    private Queue<Pair<Integer, CompiledEvent>> eventQueue = new ArrayDeque<>();        // Integer == card index

    private Queue<String> logMessages = new ArrayDeque<>();

    public ProcessorTileEntity() {
        super(GeneralConfiguration.processorMaxenergy, GeneralConfiguration.processorReceivepertick);
        for (int i = 0 ; i < cardInfo.length ; i++) {
            cardInfo[i] = new CardInfo();
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

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
                if (prevIn == 0 && powerLevel > 0) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_ON)) {
                        runOrQueueEvent(i, event);
                    }
                } else if (prevIn > 0 && powerLevel == 0) {
                    for (CompiledEvent event : compiledCard.getEvents(Opcodes.EVENT_REDSTONE_OFF)) {
                        runOrQueueEvent(i, event);
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
        System.out.println("runOrQueueEvent: cardIndex = " + cardIndex);
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

    private CpuCore findAvailableCore() {
        for (CpuCore core : cpuCores) {
            if (!core.hasProgram()) {
                return core;
            }
        }
        return null;
    }

    private void run() {
        for (CpuCore core : cpuCores) {
            core.run(this);
        }
    }

    private void updateCores() {
        if (coresDirty) {
            coresDirty = false;
            // @todo, keep state of current running programs?
            cpuCores.clear();
            for (int i = ProcessorContainer.SLOT_EXPANSION ; i < ProcessorContainer.SLOT_EXPANSION + EXPANSION_SLOTS ; i++) {
                ItemStack expansionStack = inventoryHelper.getStackInSlot(i);
                if (expansionStack != null && expansionStack.getItem() == ModItems.cpuCoreEX2000Item) {
                    cpuCores.add(new CpuCore());
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

    public void fetchItems(RunningProgram program, Inventory inv, int slot, int amount, int virtualSlot) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            // @todo Exception
            log("No slot!");
            return;
        }
        IItemHandler handler = getItemHandlerAt(inv);
        if (handler == null) {
            // @todo exception
            log("Invalid inventory!");
            return;
        }
        ItemStack extracted = handler.extractItem(slot, amount, true);
        if (extracted == null) {
            // Nothing to do
            return;
        }
        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, extracted, true) != null) {
            // Not enough room. Do nothing
            return;
        }

        // All seems ok. Do the real thing now.
        extracted = handler.extractItem(slot, amount, false);
        capability.insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, extracted, false);
    }


    public void pushItems(RunningProgram program, Inventory inv, int slot, int amount, int virtualSlot) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            // @todo Exception
            log("No slot!");
            return;
        }
        IItemHandler handler = getItemHandlerAt(inv);
        if (handler == null) {
            // @todo exception
            log("Invalid inventory!");
            return;
        }
        IItemHandler capability = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        ItemStack extracted = capability.extractItem(realSlot + ProcessorContainer.SLOT_BUFFER, amount, true);
        if (extracted == null) {
            // Nothing to do
            return;
        }
        if (handler.insertItem(slot, extracted, true) != null) {
            // Not enough room. Do nothing
            return;
        }

        // All seems ok. Do the real thing now.
        extracted = capability.extractItem(realSlot + ProcessorContainer.SLOT_BUFFER, amount, false);
        handler.insertItem(slot, extracted, false);
    }


    public void insertStack(RunningProgram program, int virtualSlot, ItemStack stack) {
        CardInfo info = this.cardInfo[program.getCardIndex()];
        int realSlot = info.getRealSlot(virtualSlot);
        if (realSlot == -1) {
            // @todo Exception
            log("No slot!");
            return;
        }
        getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).insertItem(realSlot + ProcessorContainer.SLOT_BUFFER, stack, false);
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
            // @todo support variables
            return null;
        }
    }

    public boolean evalulateBoolParameter(CompiledOpcode compiledOpcode, RunningProgram program, int parIndex) {
        Object value = evalulateParameter(compiledOpcode, program, parIndex);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Integer) {
            return ((Integer) value) != 0;
        } else if (value instanceof String) {
            return !((String) value).isEmpty();
        } else if (value instanceof EnumFacing) {
            return true;
        } else {
            return false;
        }
    }

    public IItemHandler getItemHandlerAt(Inventory inv) {
        BlockPos np = pos.offset(inv.getSide());
        TileEntity te = worldObj.getTileEntity(np);
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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        working = tagCompound.getBoolean("working");
        tickCount = tagCompound.getInteger("tickCount");
        readBufferFromNBT(tagCompound, inventoryHelper);

        readCardInfo(tagCompound);
        readCores(tagCompound);
        readEventQueue(tagCompound);
        readLog(tagCompound);
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
        writeBufferToNBT(tagCompound, inventoryHelper);

        writeCardInfo(tagCompound);
        writeCores(tagCompound);
        writeEventQueue(tagCompound);
        writeLog(tagCompound);
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

    private void executeCommand(String cmd) {
        markDirty();
        cmd = cmd.toLowerCase();
        if ("clear".equals(cmd)) {
            logMessages.clear();
        } else if ("stop".equals(cmd)) {
            int n = 0;
            for (CpuCore core : cpuCores) {
                if (core.hasProgram()) {
                    n++;
                    core.stopProgram();
                }
            }
            log("Stopped " + n + " programs!");
        } else if ("list".equals(cmd)) {
            int n = 0;
            for (CpuCore core : cpuCores) {
                if (core.hasProgram()) {
                    log("Core: " + n + " -> <busy>");
                } else {
                    log("Core: " + n + " -> <idle>");
                }
                n++;
            }
        }
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
            executeCommand(args.get("cmd").getString());
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
        }
        return false;
    }


}

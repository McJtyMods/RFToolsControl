package mcjty.rftoolscontrol.blocks.processor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftoolscontrol.config.GeneralConfiguration;
import mcjty.rftoolscontrol.logic.compiled.CompiledCard;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class ProcessorTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    // Number of card slots the processor supports
    public static final int CARD_SLOTS = 6;
    public static final int ITEM_SLOTS = 3*8;
    public static final int EXPANSION_SLOTS = 4*4;

    public static final String CMD_ALLOCATE = "allocate";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ProcessorContainer.factory, ProcessorContainer.SLOTS);
    private boolean working = false;

    // If true some cards might need compiling
    private boolean cardsDirty = true;

    private CardInfo[] cardInfo = new CardInfo[CARD_SLOTS];

    public ProcessorTileEntity() {
        super(GeneralConfiguration.PROCESSOR_MAXENERGY, GeneralConfiguration.PROCESSOR_RECEIVEPERTICK);
        for (int i = 0 ; i < cardInfo.length ; i++) {
            cardInfo[i] = new CardInfo();
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            boolean old = working;
            working = isMachineEnabled();
            if (working != old) {
                markDirtyClient();
            }

            if (working) {
                process();
            }
        }
    }

    private void process() {
        compileCards();
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
                        cardInfo[cardIndex].setCompiledCard(CompiledCard.compile(ProgramCardInstance.parseInstance(cardStack)));
                    }
                }
            }
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= ProcessorContainer.SLOT_CARD && index < ProcessorContainer.SLOT_CARD + CARD_SLOTS) {
            cardInfo[index-ProcessorContainer.SLOT_CARD].setCompiledCard(null);
            cardsDirty = true;
        }
        getInventoryHelper().setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index >= ProcessorContainer.SLOT_CARD && index < ProcessorContainer.SLOT_CARD + CARD_SLOTS) {
            cardInfo[index-ProcessorContainer.SLOT_CARD].setCompiledCard(null);
            cardsDirty = true;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        working = tagCompound.getBoolean("working");
        readBufferFromNBT(tagCompound, inventoryHelper);
        NBTTagList cardInfoList = tagCompound.getTagList("cardInfo", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < cardInfoList.tagCount() ; i++) {
            cardInfo[i] = CardInfo.readFromNBT(cardInfoList.getCompoundTagAt(i));
        }

    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setBoolean("working", working);
        writeBufferToNBT(tagCompound, inventoryHelper);
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

    public CardInfo getCardInfo(int index) {
        return cardInfo[index];
    }

    public boolean isWorking() {
        return working && isMachineEnabled();
    }

    private void allocate(int card, int itemAlloc, int varAlloc) {
        cardInfo[card].setItemAllocation(itemAlloc);
        cardInfo[card].setVarAllocation(varAlloc);
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
        }
        return false;
    }
}

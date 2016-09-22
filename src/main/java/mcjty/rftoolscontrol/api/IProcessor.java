package mcjty.rftoolscontrol.api;

import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.registry.BlockSide;
import mcjty.rftoolscontrol.logic.registry.Inventory;
import mcjty.rftoolscontrol.logic.registry.OpcodeRunnable;
import mcjty.rftoolscontrol.logic.running.RunningProgram;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The processor
 */
public interface IProcessor {

    <T> T evaluateParameter(CompiledOpcode compiledOpcode, IProgram program, int parIndex);

    /**
     * Evaluate an integer parameter. Return 0 if the parameter was not an integer or null
     */
    int evaluateIntParameter(CompiledOpcode compiledOpcode, IProgram program, int parIndex);

    /**
     * Evaluate an integer parameter. Return null if the parameter was not given
     */
    Integer evaluateIntegerParameter(CompiledOpcode compiledOpcode, IProgram program, int parIndex);

    String evaluateStringParameter(CompiledOpcode compiledOpcode, IProgram program, int parIndex);

    boolean evaluateBoolParameter(CompiledOpcode compiledOpcode, IProgram program, int parIndex);

    /**
     * Set the output redstone power at a given side on the network
     *
     * @param side
     * @param level
     */
    void setPowerOut(@Nonnull BlockSide side, int level);

    /**
     * Read the redstone value at a specific side on the network
     *
     * @param side
     * @return
     */
    int readRedstoneIn(@Nonnull BlockSide side);

    /**
     * Get an itemhandler for an inventory at a given side on the network
     *
     * @param inv
     * @return
     */
    IItemHandler getItemHandlerAt(Inventory inv);

    /**
     * Log a message on the console
     *
     * @param message
     */
    void log(String message);

    /**
     * Get an item from an internal slot
     *
     * @param program
     * @param virtualSlot
     * @return
     */
    ItemStack getItemInternal(IProgram program, int virtualSlot);

    /**
     * Set a variable to an integer
     *  @param program
     * @param var
     */
    void setVariable(IProgram program, int var);

    /**
     * Get the amount of energy on a given block
     *
     * @param side
     * @return
     */
    int getEnergy(Inventory side);

    int getMaxEnergy(Inventory side);

    OpcodeRunnable.OpcodeResult placeLock(String name);

    void releaseLock(String name);

    boolean testLock(String name);

    boolean requestCraft(ItemStack stack, @Nullable Inventory inventory);

    int getLiquid(Inventory side);

    int getMaxLiquid(Inventory side);

    int signal(String signal);

    @Nullable
    ItemStack getCraftResult(IProgram program);

}
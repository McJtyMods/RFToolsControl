package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.code.Opcode;
import mcjty.rftoolscontrol.api.parameters.ParameterDescription;
import mcjty.rftoolscontrol.api.parameters.ParameterValue;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.running.ProgException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.rftoolscontrol.api.code.OpcodeOutput.*;
import static mcjty.rftoolscontrol.api.code.IOpcodeRunnable.OpcodeResult.*;
import static mcjty.rftoolscontrol.api.parameters.ParameterType.*;

public class Opcodes {

    public static final Opcode DO_REDSTONE = Opcode.builder()
            .id("do_rs")
            .description(
                    TextFormatting.GREEN + "Operation: set redstone",
                    "set redstone level at a specific side",
                    "on the processor or a node in the network")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .parameter(ParameterDescription.builder().name("level").type(PAR_INTEGER).description("redstone level").build())
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateParameter(opcode, program, 0);
                int level = processor.evaluateIntParameter(opcode, program, 1);
                processor.setPowerOut(side, level);
                return POSITIVE;
            }))
            .icon(0, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .description(
                    TextFormatting.GREEN + "Event: redstone on",
                    "execute program when redstone signal at",
                    "a specific side (or in general) goes on")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(3, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .description(
                    TextFormatting.GREEN + "Event: redstone off",
                    "execute program when redstone signal at",
                    "a specific side (or in general) goes off")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(4, 0)
            .build();

    public static final Opcode EVENT_SIGNAL = Opcode.builder()
            .id("ev_signal")
            .description(
                    TextFormatting.GREEN + "Event: signal",
                    "execute program when a signal is",
                    "received from an rftools screen",
                    "or from the processor console")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).description("matching signal").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(5, 0)
            .build();

    public static final Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
            .description(
                    TextFormatting.GREEN + "Operation: wait",
                    "wait a specific number of ticks")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).description("amount of ticks to wait").build())
            .icon(6, 0)
            .runnable(((processor, program, opcode) -> {
                int ticks = processor.evaluateIntParameter(opcode, program, 0);
                program.setDelay(ticks);
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_COUNTINV = Opcode.builder()
            .id("eval_countinv")
            .description(
                    TextFormatting.GREEN + "Eval: count items external",
                    "count the amount of items in a specific slot",
                    "or of a certain type in an external inventory",
                    "adjacent to the processor or a connected node",
                    "Can also be used to count items in storage system")
            .outputDescription("amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory", "(not for storage)").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to count").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).optional().description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).optional().description("count routable items", "(only for storage)").build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 3);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 4);
                int cnt = ((ProcessorTileEntity)processor).countItem(inv, slot, item, oredict, routable, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETITEM = Opcode.builder()
            .id("eval_getitem")
            .description(
                    TextFormatting.GREEN + "Eval: examine item",
                    "examine an item in a specific slot",
                    "from an external inventory adjacent to",
                    "the processor or a connected node")
            .outputDescription("itemstack in target slot (stack)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("slot in inventory").build())
            .icon(10, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int slot = processor.evaluateIntParameter(opcode, program, 1);
                IItemHandler handler = processor.getItemHandlerAt(inv);
                ItemStack item = handler.getStackInSlot(slot);
                program.setLastValue(Parameter.builder().type(PAR_ITEM).value(ParameterValue.constant(item)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_REDSTONE = Opcode.builder()
            .id("eval_rs")
            .description(
                    TextFormatting.GREEN + "Eval: read redstone",
                    "read the redstone value coming to a specific",
                    "side of the processor or a connected node")
            .outputDescription("read redstone value (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .icon(1, 0)
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateParameter(opcode, program, 0);
                int rs = processor.readRedstoneIn(side);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rs)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_STOP = Opcode.builder()
            .id("do_stop")
            .description(
                    TextFormatting.GREEN + "Operation: stop program",
                    "stop executing at this point",
                    "you normally don't have to use this",
                    "manually except to break a loop")
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                program.killMe();
                return POSITIVE;
            })
            .build();

    // Internal opcode that will stop the program or resume a loop
    public static final Opcode DO_STOP_OR_RESUME = Opcode.builder()
            .id("do_stop_or_resume")
            .description(
                    TextFormatting.GREEN + "Operation: stop/resume")
            .deprecated(true)   // Not really deprecated but this prevents it being in the list
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                ((ProcessorTileEntity)processor).stopOrResume(program);
                return HOLD;
            })
            .build();

    public static final Opcode DO_LOG = Opcode.builder()
            .id("do_log")
            .description(
                    TextFormatting.GREEN + "Operation: log message",
                    "log a message on the processor console")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("message").type(PAR_STRING).description("message to output").build())
            .icon(8, 0)
            .runnable(((processor, program, opcode) -> {
                String message = processor.evaluateStringParameter(opcode, program, 0);
                processor.log(message);
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVENT_TIMER = Opcode.builder()
            .id("ev_timer")
            .description(
                    TextFormatting.GREEN + "Event: repeat",
                    "execute program every <N> ticks")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).description("ticks between each execution").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(9, 0)
            .build();

    public static final Opcode TEST_GT = Opcode.builder()
            .id("test_gt")
            .description(
                    TextFormatting.GREEN + "Test: greater than",
                    "check if the first value is greater",
                    "then the second value")
            .outputDescription("v1 > v2 (boolean)")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(10, 0)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                return v1 > v2 ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode TEST_EQ = Opcode.builder()
            .id("test_eq")
            .description(
                    TextFormatting.GREEN + "Test: equality",
                    "check if the first value is equal",
                    "to the second value")
            .outputDescription("v1 = v2 (boolean)")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(11, 0)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                return v1 == v2 ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode DO_FETCHITEMS = Opcode.builder()
            .id("do_fetchitems")
            .description(
                    TextFormatting.GREEN + "Operation: fetch items",
                    "fetch items from an external inventory adjacent",
                    "to the processor or a connected node and place",
                    "the result in the internal inventory",
                    "Also works for a storage scanner system")
            .outputDescription("amount of items fetched (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory", "(not used for storage)").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to fetch (not", "optional for storage)").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).optional().description("amount of items to fetch", "if not given it will fetch the stack").build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).description("internal (processor) slot for result").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).optional().description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).optional().description("only routable items", "(only for storage)").build())
            .icon(0, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                Integer amount = processor.evaluateIntegerParameter(opcode, program, 3);
                int slotOut = processor.evaluateIntParameter(opcode, program, 4);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 5);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 6);
                int cnt = ((ProcessorTileEntity)processor).fetchItems(program, inv, slot, item, routable, oredict, amount, slotOut);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_PUSHITEMS = Opcode.builder()
            .id("do_pushitems")
            .description(
                    TextFormatting.GREEN + "Operation: push items",
                    "push items to an external inventory",
                    "adjacent to the processor or a connected",
                    "node from the internal inventory",
                    "Can also be used for modular storage systems")
            .outputDescription("amount of items inserted (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory (not", "used for storage)").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).optional().description("amount of items to push").build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).description("internal (processor) slot for input").build())
            .icon(1, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                Integer amount = processor.evaluateIntegerParameter(opcode, program, 2);
                int slotIn = processor.evaluateIntParameter(opcode, program, 3);
                int cnt = ((ProcessorTileEntity)processor).pushItems(program, inv, slot, amount, slotIn);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_COUNTINVINT = Opcode.builder()
            .id("eval_countinvint")
            .description(
                    TextFormatting.GREEN + "Eval: count items internal",
                    "count the amount of items in a",
                    "specific slot in the processor inventory")
            .outputDescription("amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal (processor) slot").build())
            .icon(2, 1)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                ItemStack stack = processor.getItemInternal(program, slot);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(stack == null ? 0 : stack.stackSize)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_SETVAR = Opcode.builder()
            .id("do_setvar")
            .description(
                    TextFormatting.GREEN + "Operation: set variable",
                    "copy the last returned value to",
                    "the specified variable")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("index of the variable").build())
            .icon(3, 1)
            .runnable(((processor, program, opcode) -> {
                int var = processor.evaluateIntParameter(opcode, program, 0);
                processor.setVariable(program, var);
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_ADD = Opcode.builder()
            .id("do_add")
            .description(
                    TextFormatting.GREEN + "Operation: add integers",
                    "add the two given integers")
            .outputDescription("v1 + v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(4, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1+v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_SUBTRACT = Opcode.builder()
            .id("do_subtract")
            .description(
                    TextFormatting.GREEN + "Operation: subtract integers",
                    "subtract the two given integers")
            .outputDescription("v1 - v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(5, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1-v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_DIVIDE = Opcode.builder()
            .id("do_divide")
            .description(
                    TextFormatting.GREEN + "Operation: divide integers",
                    "divide the two given integers")
            .outputDescription("v1 / v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(6, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1/v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_MULTIPLY = Opcode.builder()
            .id("do_multiply")
            .description(
                    TextFormatting.GREEN + "Operation: multiply integers",
                    "multiply the two given integers")
            .outputDescription("v1 * v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(7, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1*v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_MODULO = Opcode.builder()
            .id("do_modulo")
            .description(
                    TextFormatting.GREEN + "Operation: modulo",
                    "calculate the modulo of two given integers")
            .outputDescription("v1 % v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(8, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1%v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_CONCAT = Opcode.builder()
            .id("do_concat")
            .description(
                    TextFormatting.GREEN + "Operation: string concat",
                    "concatenate the two given strings")
            .outputDescription("v1 + v2 (string)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(9, 1)
            .runnable(((processor, program, opcode) -> {
                String v1 = processor.evaluateStringParameter(opcode, program, 0);
                String v2 = processor.evaluateStringParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_STRING).value(ParameterValue.constant(v1+v2)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETRF = Opcode.builder()
            .id("eval_getrf")
            .description(
                    TextFormatting.GREEN + "Eval: get RF in machine",
                    "get the amount of RF/Forge Energy stored in",
                    "a specific machine adjacent to the processor",
                    "or a connected node")
            .outputDescription("amount of energy (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(3, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getEnergy(side);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_GETMAXRF = Opcode.builder()
            .id("eval_getmaxrf")
            .description(
                    TextFormatting.GREEN + "Eval: get max RF in machine",
                    "get the maximum amount of RF/Forge Energy stored",
                    "in a specific machine adjacent to the procesor",
                    "or a connected node")
            .outputDescription("max amount of energy (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(4, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getMaxEnergy(side);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_WIRE = Opcode.builder()
            .id("do_wire")
            .description(
                    TextFormatting.GREEN + "Operation: wire",
                    "use this to connect opcodes that",
                    "are not adjacent to each other")
            .opcodeOutput(SINGLE)
            .icon(11, 1)
            .runnable(((processor, program, opcode) -> {
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVENT_CRAFT = Opcode.builder()
            .id("ev_craft")
            .description(
                    TextFormatting.GREEN + "Event: craft",
                    "execute program when a crafting",
                    "station requests a specific item",
                    "or for an inventory with crafting cards",
                    "This operation sets the crafting ticket")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft").build())
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory with crafting cards").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(5, 2)
            .build();

    public static final Opcode DO_CRAFTOK = Opcode.builder()
            .id("do_craftok")
            .description(
                    TextFormatting.GREEN + "Operation: mark craft ok",
                    "as a result of a crafting event you can use",
                    "this opcode to mark the craft operation as ok",
                    "The optional item in the slot will be sent back to",
                    "whatever requested the item")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("internal (processor) slot", "with craft result").build())
            .icon(6, 2)
            .runnable(((processor, program, opcode) -> {
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 0);
                ((ProcessorTileEntity)processor).craftOk(program, slot);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_CRAFTFAIL = Opcode.builder()
            .id("do_craftfail")
            .description(
                    TextFormatting.GREEN + "Operation: mark craft failure",
                    "as a result of a crafting event",
                    "you can use this opcode to mark",
                    "the craft operation as failed")
            .opcodeOutput(SINGLE)
            .icon(7, 2)
            .runnable(((processor, program, opcode) -> {
                ((ProcessorTileEntity)processor).craftFail(program);
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_GETINGREDIENTS = Opcode.builder()
            .id("do_getingredients")
            .description(
                    TextFormatting.GREEN + "Operation: get ingredients",
                    "given a crafting card inventory get the needed and",
                    "missing ingredients from another inventory and insert",
                    "in processor. Can also be used with a storage scanner",
                    "Returns number of items that it could not find")
            .outputDescription("amount of failed items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("cardInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "with crafting cards").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft or empty", "for default from ticket").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last slot of that range").build())
            .icon(8, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Inventory cardInv = processor.evaluateParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                int slot1 = processor.evaluateIntParameter(opcode, program, 3);
                int slot2 = processor.evaluateIntParameter(opcode, program, 4);
                int failed = ((ProcessorTileEntity)processor).getIngredients(program, inv, cardInv, item, slot1, slot2);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(failed)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_GETINGREDIENTS_SMART = Opcode.builder()
            .id("do_getingredients_smart")
            .description(
                    TextFormatting.GREEN + "Operation: get ingredients smart",
                    "check if all ingredients are available in another",
                    "inventory or storage system. If so fetch them in the",
                    "processor. Otherwise try to request all missing items",
                    "Returns 0 on success, -1 on failure and otherwise the",
                    "number of items that are requested")
            .outputDescription("0, -1, or requested items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("cardInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "with crafting cards").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft or empty", "for default from ticket").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last slot of that range").build())
            .parameter(ParameterDescription.builder().name("destInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "for end result of requests").build())
            .icon(11, 3)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Inventory cardInv = processor.evaluateParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                int slot1 = processor.evaluateIntParameter(opcode, program, 3);
                int slot2 = processor.evaluateIntParameter(opcode, program, 4);
                Inventory destInv = processor.evaluateParameter(opcode, program, 5);
                int failed = ((ProcessorTileEntity)processor).getIngredientsSmart(program, inv, cardInv, item, slot1, slot2, destInv);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(failed)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_PUSHMULTI = Opcode.builder()
            .id("do_pushmulti")
            .description(
                    TextFormatting.GREEN + "Operation: push multiple items",
                    "push multiple items from the internal inventory",
                    "to an external inventory adjacent to the processor",
                    "or a connected node. Also works on storage system")
            .outputDescription("amount of failed items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("first internal slot for input").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last internal slot for input").build())
            .parameter(ParameterDescription.builder().name("extSlot").type(PAR_INTEGER).optional().description("first external slot").build())
            .icon(11, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int slot1 = processor.evaluateIntParameter(opcode, program, 1);
                int slot2 = processor.evaluateIntParameter(opcode, program, 2);
                Integer extSlot = processor.evaluateIntegerParameter(opcode, program, 3);
                int failed = ((ProcessorTileEntity)processor).pushItemsMulti(program, inv, slot1, slot2, extSlot);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(failed)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_SETCRAFTTICKET = Opcode.builder()
            .id("do_setticket")
            .description(
                    TextFormatting.GREEN + "Operation: set craft ticket",
                    "set a craft ticket so that you can resume",
                    "a previously stored crafting operation")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("ticket").type(PAR_STRING).description("crafting ticket").build())
            .icon(0, 3)
            .runnable(((processor, program, opcode) -> {
                String ticket = processor.evaluateStringParameter(opcode, program, 0);
                ((ProcessorTileEntity)processor).setCraftTicket(program, ticket);
                return POSITIVE;
            }))
            .build();

    public static final Opcode TEST_SET = Opcode.builder()
            .id("test_set")
            .description(
                    TextFormatting.GREEN + "Test: is value set/true",
                    "check if the boolean value is true")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v").type(PAR_BOOLEAN).description("value to test").build())
            .icon(1, 3)
            .runnable(((processor, program, opcode) -> {
                return processor.evaluateBoolParameter(opcode, program, 0) ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode EVENT_CRAFTRESUME = Opcode.builder()
            .id("ev_craftresume")
            .description(
                    TextFormatting.GREEN + "Event: craft resume",
                    "resume crafting operation",
                    "This operation sets the crafting ticket")
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).description("ticks between each check").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .icon(2, 3)
            .build();

    public static final Opcode DO_CRAFTWAIT = Opcode.builder()
            .id("do_craftwait")
            .description(
                    TextFormatting.GREEN + "Operation: wait for finished craft (item)",
                    "suspend the crafting operation and resume it",
                    "as soon as a certain item appears in an inventory")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to wait for. If not given", "it will use current craft result").build())
            .icon(3, 3)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                ItemStack item = processor.evaluateParameter(opcode, program, 1);
                ((ProcessorTileEntity)processor).craftWait(program, inv, item);
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_CRAFTWAIT_TIMED = Opcode.builder()
            .id("do_craftwait_ticked")
            .description(
                    TextFormatting.GREEN + "Operation: wait for finished craft (timed)",
                    "suspend the crafting operation and resume",
                    "it at regular times")
            .opcodeOutput(SINGLE)
            .icon(2, 4)
            .runnable(((processor, program, opcode) -> {
                ((ProcessorTileEntity)processor).craftWaitTimed(program);
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVENT_EXCEPTION = Opcode.builder()
            .id("ev_exception")
            .description(
                    TextFormatting.GREEN + "Event: exception",
                    "execute program on exception")
            .parameter(ParameterDescription.builder().name("exception").type(PAR_EXCEPTION).description("the exception code to catch").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .icon(4, 3)
            .build();

    public static final Opcode DO_LOCK = Opcode.builder()
            .id("do_lock")
            .description(
                    TextFormatting.GREEN + "Operation: test and lock",
                    "test if a named lock is free and if it",
                    "is place the lock and continue. If the",
                    "lock is not free wait until it is")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("name").type(PAR_STRING).description("name of the lock").build())
            .icon(9, 2)
            .runnable(((processor, program, opcode) -> {
                String name = processor.evaluateStringParameter(opcode, program, 0);
                return processor.placeLock(name);
            }))
            .build();
    public static final Opcode DO_RELEASELOCK = Opcode.builder()
            .id("do_releaselock")
            .description(
                    TextFormatting.GREEN + "Operation: release lock",
                    "release a named lock")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("name").type(PAR_STRING).description("name of the lock").build())
            .icon(10, 2)
            .runnable(((processor, program, opcode) -> {
                String name = processor.evaluateStringParameter(opcode, program, 0);
                processor.releaseLock(name);
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_LOCK = Opcode.builder()
            .id("eval_lock")
            .description(
                    TextFormatting.GREEN + "Eval: test lock",
                    "test if the named lock is set and",
                    "return true if it is")
            .outputDescription("true if lock is set (boolean)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("name").type(PAR_STRING).description("name of the lock to test").build())
            .icon(5, 3)
            .runnable(((processor, program, opcode) -> {
                String name = processor.evaluateStringParameter(opcode, program, 0);
                boolean locked = processor.testLock(name);
                program.setLastValue(Parameter.builder().type(PAR_BOOLEAN).value(ParameterValue.constant(locked)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_REQUESTCRAFT = Opcode.builder()
            .id("do_requestcraft")
            .description(
                    TextFormatting.GREEN + "Operation: request craft",
                    "request crafting for a specific item from a",
                    "connected crafting station. If the optional inventory",
                    "is given the craft result will be directed there",
                    "Otherwise it goes to the crafting station")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("the item to request").build())
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory for the end result").build())
            .icon(6, 3)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateParameter(opcode, program, 0);
                Inventory inv = processor.evaluateParameter(opcode, program, 1);
                processor.requestCraft(item, inv);
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETITEMINT = Opcode.builder()
            .id("eval_getitemint")
            .description(
                    TextFormatting.GREEN + "Eval: examine item internal",
                    "examine an item in a specific slot",
                    "in the processor")
            .outputDescription("itemstack in target slot (stack)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal slot in processor").build())
            .icon(7, 3)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                ItemStack item = processor.getItemInternal(program, slot);
                program.setLastValue(Parameter.builder().type(PAR_ITEM).value(ParameterValue.constant(item)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode TEST_LOOP = Opcode.builder()
            .id("test_loop")
            .description(
                    TextFormatting.GREEN + "Test: loop",
                    "loop a variable until it reaches a specific value",
                    "Make sure to set the variable to the starting value",
                    "of the loop before this opcode")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable index (first var is 0)").build())
            .parameter(ParameterDescription.builder().name("end").type(PAR_INTEGER).description("end index (inclusive)").build())
            .icon(8, 3)
            .runnable(((processor, program, opcode) -> {
                int varIdx = processor.evaluateIntParameter(opcode, program, 0);
                int end = processor.evaluateIntParameter(opcode, program, 1);
                return ((ProcessorTileEntity)processor).handleLoop(program, varIdx, end);
            }))
            .build();

    public static final Opcode EVAL_INTEGER = Opcode.builder()
            .id("eval_integer")
            .description(
                    TextFormatting.GREEN + "Eval: integer",
                    "evaluate an integer and set it as",
                    "the result for future opcodes to use")
            .outputDescription("integer result (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("value").type(PAR_INTEGER).description("integer value to set as result").build())
            .icon(9, 3)
            .runnable(((processor, program, opcode) -> {
                int value = processor.evaluateIntParameter(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(value)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_STRING = Opcode.builder()
            .id("eval_string")
            .description(
                    TextFormatting.GREEN + "Eval: string",
                    "evaluate a string and set it as",
                    "the result for future opcodes to use")
            .outputDescription("string result (string)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("value").type(PAR_STRING).description("string value to set as result").build())
            .icon(10, 3)
            .runnable(((processor, program, opcode) -> {
                String value = processor.evaluateStringParameter(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_STRING).value(ParameterValue.constant(value)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_INGREDIENTS = Opcode.builder()
            .id("eval_ingredients")
            .description(
                    TextFormatting.GREEN + "Eval: check ingredients",
                    "given a crafting card inventory check if all",
                    "the ingredients for the given recipe are present",
                    "at exactly the right amount and right spot")
            .outputDescription("if the ingredients are complete (boolean)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("cardInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "with crafting cards").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft or empty", "for default from ticket").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last slot of that range").build())
            .icon(0, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory cardInv = processor.evaluateParameter(opcode, program, 0);
                ItemStack item = processor.evaluateParameter(opcode, program, 1);
                int slot1 = processor.evaluateIntParameter(opcode, program, 2);
                int slot2 = processor.evaluateIntParameter(opcode, program, 3);
                boolean ok = ((ProcessorTileEntity)processor).checkIngredients(program, cardInv, item, slot1, slot2);
                program.setLastValue(Parameter.builder().type(PAR_BOOLEAN).value(ParameterValue.constant(ok)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_INVENTORY = Opcode.builder()
            .id("eval_inventory")
            .description(
                    TextFormatting.GREEN + "Eval: inventory",
                    "get an inventory adjacent to the processor",
                    "or a connected node and put it as the last",
                    "result (useful for storing in variables)",
                    "If inventory is kept empty then this can",
                    "indicate a storage system as well")
            .outputDescription("inventory result (inventory)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty for storage").build())
            .icon(1, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_INVENTORY).value(ParameterValue.constant(inv)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETLIQUID = Opcode.builder()
            .id("eval_getliquid")
            .description(
                    TextFormatting.GREEN + "Eval: get liquid in tank",
                    "get the amount of liquid stored in a",
                    "specific tank adjacent to the",
                    "processor or a connected node")
            .outputDescription("amount of mb (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(3, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getLiquid(side);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETMAXLIQUID = Opcode.builder()
            .id("eval_getmaxliquid")
            .description(
                    TextFormatting.GREEN + "Eval: get max liquid in tank",
                    "get the maximum amount of liquid stored",
                    "in a specific tank adjacent to the",
                    "processor or a connected node")
            .outputDescription("max amount of mb (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(4, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getMaxLiquid(side);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_SIGNAL = Opcode.builder()
            .id("do_signal")
            .description(
                    TextFormatting.GREEN + "Operation: send signal",
                    "send a signal to a program that has a signal event",
                    "installed on this processor. That program will",
                    "start as soon as a core is available to do so",
                    "This operation returns the amount of event handlers",
                    "that reacted to this signal")
            .outputDescription("event handlers that reacted (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).description("signal name").build())
            .icon(5, 4)
            .runnable(((processor, program, opcode) -> {
                String signal = processor.evaluateStringParameter(opcode, program, 0);
                int cnt = processor.signal(signal);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETDAMAGE = Opcode.builder()
            .id("eval_getdamage")
            .description(
                    TextFormatting.GREEN + "Eval: get damage",
                    "get the damage value from an item")
            .outputDescription("damage value (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("item to get damage from").build())
            .icon(6, 4)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateParameter(opcode, program, 0);
                if (item == null) {
                    throw new ProgException(ExceptionType.EXCEPT_MISSINGITEM);
                }
                int damage = item.getItemDamage();
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(damage)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_GETNAME = Opcode.builder()
            .id("eval_getname")
            .description(
                    TextFormatting.GREEN + "Eval: get name",
                    "get the readable name from an item")
            .outputDescription("item name (string)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("item to get name from").build())
            .icon(7, 4)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateParameter(opcode, program, 0);
                if (item == null) {
                    throw new ProgException(ExceptionType.EXCEPT_MISSINGITEM);
                }
                String name = item.getDisplayName();
                program.setLastValue(Parameter.builder().type(PAR_STRING).value(ParameterValue.constant(name)).build());
                return POSITIVE;
            }))
            .build();


    public static final Map<String, Opcode> OPCODES = new HashMap<>();
    public static final List<Opcode> SORTED_OPCODES = new ArrayList<>();

    public static void init() {
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(EVENT_TIMER);
        register(EVENT_SIGNAL);
        register(EVENT_CRAFT);
        register(EVENT_CRAFTRESUME);
        register(EVENT_EXCEPTION);
        register(DO_WIRE);
        register(EVAL_COUNTINV);
        register(EVAL_COUNTINVINT);
        register(EVAL_GETITEM);
        register(EVAL_GETITEMINT);
        register(EVAL_GETDAMAGE);
        register(EVAL_GETNAME);
        register(EVAL_INGREDIENTS);
        register(EVAL_REDSTONE);
        register(EVAL_GETRF);
        register(EVAL_GETMAXRF);
        register(EVAL_GETLIQUID);
        register(EVAL_GETMAXLIQUID);
        register(EVAL_INTEGER);
        register(EVAL_STRING);
        register(EVAL_INVENTORY);
        register(EVAL_LOCK);
        register(TEST_GT);
        register(TEST_EQ);
        register(TEST_SET);
        register(TEST_LOOP);
        register(DO_REDSTONE);
        register(DO_DELAY);
        register(DO_STOP);
        register(DO_STOP_OR_RESUME);
        register(DO_SIGNAL);
        register(DO_LOG);
        register(DO_FETCHITEMS);
        register(DO_PUSHITEMS);
        register(DO_PUSHMULTI);
        register(DO_SETVAR);
        register(DO_ADD);
        register(DO_SUBTRACT);
        register(DO_DIVIDE);
        register(DO_MULTIPLY);
        register(DO_MODULO);
        register(DO_CONCAT);
        register(DO_CRAFTOK);
        register(DO_CRAFTFAIL);
        register(DO_GETINGREDIENTS);
        register(DO_GETINGREDIENTS_SMART);
        register(DO_SETCRAFTTICKET);
        register(DO_CRAFTWAIT);
        register(DO_CRAFTWAIT_TIMED);
        register(DO_REQUESTCRAFT);
        register(DO_LOCK);
        register(DO_RELEASELOCK);
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
        if (!opcode.isDeprecated()) {
            SORTED_OPCODES.add(opcode);
        }
    }
}

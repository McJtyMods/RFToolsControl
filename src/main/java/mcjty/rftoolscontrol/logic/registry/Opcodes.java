package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.rftoolscontrol.logic.registry.OpcodeOutput.*;
import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class Opcodes {

    public static final Opcode DO_REDSTONE = Opcode.builder()
            .id("do_rs")
            .description(
                    TextFormatting.GREEN + "Operation: set redstone",
                    "set redstone level at a specific",
                    "side on the processor or a node",
                    "in the network")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .parameter(ParameterDescription.builder().name("level").type(PAR_INTEGER).description("redstone level").build())
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateParameter(opcode, program, 0);
                int level = processor.evaluateIntParameter(opcode, program, 1);
                processor.setPowerOut(side, level, program);
                return true;
            }))
            .icon(0, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .description(
                    TextFormatting.GREEN + "Event: redstone on",
                    "execute program when redstone",
                    "signal at a specific side (or in",
                    "general) goes on",
                    "Note: not yet supported on nodes")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .icon(3, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .description(
                    TextFormatting.GREEN + "Event: redstone off",
                    "execute program when redstone",
                    "signal at a specific side (or in",
                    "general) goes off",
                    "Note: not yet supported on nodes")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .icon(4, 0)
            .build();

    public static final Opcode EVENT_SIGNAL = Opcode.builder()
            .id("ev_signal")
            .description(
                    TextFormatting.GREEN + "Event: signal",
                    "execute program when a signal",
                    "is received from an rftools screen")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).description("matching signal").build())
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
                return true;
            }))
            .build();

    public static final Opcode EVAL_COUNTINV = Opcode.builder()
            .id("eval_countinv")
            .description(
                    TextFormatting.GREEN + "Eval: count items external",
                    "count the amount of items in a",
                    "specific slot or of a certain type",
                    "in an external inventory adjacent to",
                    "the processor or a connected node",
                    "Can also be used to count items in",
                    "in a storage scanner network")
            .outputDescription("amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("optional slot in inventory", "(not for storage)").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("optional item to count").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).description("count routable items", "(only for storage)").build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 3);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 4);
                int cnt = processor.countItem(inv, slot, item, oredict, routable, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
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
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("optional slot in inventory").build())
            .icon(10, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int slot = processor.evaluateIntParameter(opcode, program, 1);
                IItemHandler handler = processor.getItemHandlerAt(inv, program);
                ItemStack item = handler.getStackInSlot(slot);
                program.setLastValue(Parameter.builder().type(PAR_ITEM).value(ParameterValue.constant(item)).build());
                return true;
            }))
            .build();

    public static final Opcode EVAL_REDSTONE = Opcode.builder()
            .id("eval_rs")
            .description(
                    TextFormatting.GREEN + "Eval: read redstone",
                    "read the redstone value coming",
                    "to a specific side of the processor",
                    "or a connected node")
            .outputDescription("read redstone value (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .icon(1, 0)
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateParameter(opcode, program, 0);
                int rs = processor.readRedstoneIn(side, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rs)).build());
                return true;
            }))
            .build();

    public static final Opcode DO_STOP = Opcode.builder()
            .id("do_stop")
            .description(
                    TextFormatting.GREEN + "Operation: stop program",
                    "stop executing at this point",
                    "you normally don't have to use",
                    "this manually")
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                program.killMe();
                return true;
            })
            .build();

    public static final Opcode DO_LOG = Opcode.builder()
            .id("do_log")
            .description(
                    TextFormatting.GREEN + "Operation: log message",
                    "log a message on the processor",
                    "console")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("message").type(PAR_STRING).description("message to output").build())
            .icon(8, 0)
            .runnable(((processor, program, opcode) -> {
                String message = processor.evaluateStringParameter(opcode, program, 0);
                processor.log(message);
                return true;
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
                return v1 > v2;
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
                return v1 == v2;
            }))
            .build();

    public static final Opcode DO_FETCHITEMS = Opcode.builder()
            .id("do_fetchitems")
            .description(
                    TextFormatting.GREEN + "Operation: fetch items",
                    "fetch items from an external",
                    "inventory adjacent to the processor",
                    "or a connected node and place the",
                    "result in the internal inventory",
                    "Also works for a storage scanner system")
            .outputDescription("amount of items fetched (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("optional slot in inventory", "(not used for storage)").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("optional item to fetch", "(not optional for storage)").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of items to fetch").build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).description("internal (processor) slot for result").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).description("only routable items", "(only for storage)").build())
            .icon(0, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateParameter(opcode, program, 2);
                int amount = processor.evaluateIntegerParameter(opcode, program, 3);
                int slotOut = processor.evaluateIntParameter(opcode, program, 4);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 5);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 6);
                int cnt = processor.fetchItems(program, inv, slot, item, routable, oredict, amount, slotOut);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
            }))
            .build();

    public static final Opcode DO_PUSHITEMS = Opcode.builder()
            .id("do_pushitems")
            .description(
                    TextFormatting.GREEN + "Operation: push items",
                    "push items to an external",
                    "inventory adjacent to the processor",
                    "or a connected node from the",
                    "internal inventory",
                    "Can also be used for modular",
                    "storage systems")
            .outputDescription("amount of items inserted (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("optional slot in inventory", "(not used for storage)").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of items to push").build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).description("internal (processor) slot for input").build())
            .icon(1, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                int amount = processor.evaluateIntParameter(opcode, program, 2);
                int slotIn = processor.evaluateIntParameter(opcode, program, 3);
                int cnt = processor.pushItems(program, inv, slot, amount, slotIn);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
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
                return true;
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
                return true;
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
                return true;
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
                return true;
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
                return true;
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
                return true;
            }))
            .build();

    public static final Opcode DO_MODULO = Opcode.builder()
            .id("do_modulo")
            .description(
                    TextFormatting.GREEN + "Operation: modulo",
                    "calculate the modulo of two",
                    "given integers")
            .outputDescription("v1 % v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(8, 1)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(v1%v2)).build());
                return true;
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
                return true;
            }))
            .build();

    public static final Opcode EVAL_COUNTSTOR = Opcode.builder()
            .id("eval_countstor")
            .description(
                    TextFormatting.GREEN + "Eval: count items storage",
                    "count the amount of items in a",
                    "stroage system (scanner) of a",
                    "certain type",
                    TextFormatting.RED + "Needs storage scanner module")
            .deprecated(true)
            .outputDescription("amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("the item to count").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).description("count routable items").build())
            .icon(0, 2)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateParameter(opcode, program, 0);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 1);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 2);
                int cnt = processor.countItemStorage(item, routable, oredict, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
            }))
            .build();

    public static final Opcode DO_FETCHSTOR = Opcode.builder()
            .id("do_fetchstor")
            .description(
                    TextFormatting.GREEN + "Operation: fetch from storage",
                    "fetch items from a storage system",
                    "(scanner) and place the result",
                    "in the internal inventory",
                    TextFormatting.RED + "Needs storage scanner module")
            .deprecated(true)
            .outputDescription("amount of items fetched (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("the item to fetch").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).description("only routable items").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of items to fetch").build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).description("internal (processor) slot for result").build())
            .icon(1, 2)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateParameter(opcode, program, 0);
                boolean oredict = processor.evaluateBoolParameter(opcode, program, 1);
                boolean routable = processor.evaluateBoolParameter(opcode, program, 2);
                int amount = processor.evaluateIntParameter(opcode, program, 3);
                int slotOut = processor.evaluateIntParameter(opcode, program, 4);
                int cnt = processor.fetchItems(program, null, null, item, routable, oredict, amount, slotOut);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
            }))
            .build();

    public static final Opcode DO_PUSHSTOR = Opcode.builder()
            .id("do_pushstor")
            .description(
                    TextFormatting.GREEN + "Operation: push to storage",
                    "push items to a storage system",
                    "(scanner) from the internal inventory",
                    TextFormatting.RED + "Needs storage scanner module")
            .deprecated(true)
            .outputDescription("amount of items inserted (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of items to push").build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).description("internal (processor) slot for input").build())
            .icon(2, 2)
            .runnable(((processor, program, opcode) -> {
                int amount = processor.evaluateIntParameter(opcode, program, 0);
                int slotIn = processor.evaluateIntParameter(opcode, program, 1);
                int cnt = processor.pushItems(program, null, null, amount, slotIn);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
            }))
            .build();

    public static final Opcode EVAL_GETRF = Opcode.builder()
            .id("eval_getrf")
            .description(
                    TextFormatting.GREEN + "Eval: get RF in machine",
                    "get the amount of RF stored in a",
                    "specific machine adjacent to the",
                    "processor or a connected node")
            .outputDescription("amount of RF (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(3, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getEnergy(side, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return true;
            }))
            .build();
    public static final Opcode EVAL_GETMAXRF = Opcode.builder()
            .id("eval_getmaxrf")
            .description(
                    TextFormatting.GREEN + "Eval: get max RF in machine",
                    "get the maximum amount of RF stored",
                    "in a specific machine adjacent to the",
                    "processor or a connected node")
            .outputDescription("max amount of RF (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(4, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateParameter(opcode, program, 0);
                int rf = processor.getMaxEnergy(side, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(rf)).build());
                return true;
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
                return true;
            }))
            .build();

    public static final Opcode EVENT_CRAFT = Opcode.builder()
            .id("ev_craft")
            .description(
                    TextFormatting.GREEN + "Event: craft",
                    "execute program when a crafting",
                    "station requests a specific item",
                    "This operation sets the crafting context")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("the item to craft").build())
            .icon(5, 2)
            .build();

    public static final Opcode DO_CRAFTOK = Opcode.builder()
            .id("do_craftok")
            .description(
                    TextFormatting.GREEN + "Operation: mark craft ok",
                    "as a result of a crafting event",
                    "you can use this opcode to mark",
                    "the craft operation as ok")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal (processor) slot with craft result").build())
            .icon(6, 2)
            .runnable(((processor, program, opcode) -> {
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 0);
                processor.craftOk(program, slot);
                return true;
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
                processor.craftFail(program);
                return true;
            }))
            .build();

    public static final Opcode DO_GETINGREDIENTS = Opcode.builder()
            .id("do_getingredients")
            .description(
                    TextFormatting.GREEN + "Operation: get ingredients",
                    "given a crafting card get the",
                    "needed and missing ingredients",
                    "from an adjacent inventory and",
                    "insert in processor",
                    "Can also bse used on a storage",
                    "scanner system")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("cardSlot").type(PAR_INTEGER).description("internal (processor) slot for crafting card").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .icon(8, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int cardSlot = processor.evaluateIntParameter(opcode, program, 1);
                int slot1 = processor.evaluateIntParameter(opcode, program, 2);
                processor.getIngredients(program, inv, cardSlot, slot1);
                return true;
            }))
            .build();

    public static final Opcode DO_FETCH_CARD = Opcode.builder()
            .id("do_fetch_card")
            .description(
                    TextFormatting.GREEN + "Operation: fetch crafting card",
                    "fetch the right crafting card (from",
                    "current card context) from an adjacent",
                    "inventory and place it in the processor.",
                    "Move the card that was already there back",
                    "to that inventory")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("cardSlot").type(PAR_INTEGER).description("internal (processor) slot for crafting card").build())
            .icon(10, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int cardSlot = processor.evaluateIntParameter(opcode, program, 1);
                processor.fetchCard(program, inv, cardSlot);
                return true;
            }))
            .build();

    public static final Opcode DO_PUSHMULTI = Opcode.builder()
            .id("do_pushmulti")
            .description(
                    TextFormatting.GREEN + "Operation: push multiple items",
                    "push multiple items to an external",
                    "inventory adjacent to the processor",
                    "or a connected node from the",
                    "internal inventory")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("first internal slot for input").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last internal slot for input").build())
            .icon(11, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                int slot1 = processor.evaluateIntParameter(opcode, program, 1);
                int slot2 = processor.evaluateIntParameter(opcode, program, 2);
                processor.pushItemsMulti(program, inv, slot1, slot2);
                return true;
            }))
            .build();

    public static final Opcode DO_SETCRAFTID = Opcode.builder()
            .id("do_setcraftid")
            .description(
                    TextFormatting.GREEN + "Operation: resume craft operation",
                    "resume a previously stored",
                    "crafting operation")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("craftid").type(PAR_STRING).description("crafting identification").build())
            .icon(0, 3)
            .runnable(((processor, program, opcode) -> {
                String craftId = processor.evaluateStringParameter(opcode, program, 0);
                processor.setCraftId(program, craftId);
                return true;
            }))
            .build();

    public static final Opcode TEST_SET = Opcode.builder()
            .id("test_set")
            .description(
                    TextFormatting.GREEN + "Test: is value set/true",
                    "check if the boolean value",
                    "is true")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v").type(PAR_BOOLEAN).description("value to test").build())
            .icon(1, 3)
            .runnable(((processor, program, opcode) -> {
                return processor.evaluateBoolParameter(opcode, program, 0);
            }))
            .build();

    public static final Opcode EVENT_CRAFTRESUME = Opcode.builder()
            .id("ev_craftresume")
            .description(
                    TextFormatting.GREEN + "Event: craft resume",
                    "resume crafting operation",
                    "This operation sets the crafting context")
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).description("ticks between each check").build())
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .icon(2, 3)
            .build();

    public static final Opcode DO_CRAFTWAIT = Opcode.builder()
            .id("do_craftwait")
            .description(
                    TextFormatting.GREEN + "Operation: wait for finished craft",
                    "suspend the crafting operation",
                    "and resume it as soon as a certain",
                    "item appears in an inventory")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("optional item to wait for", "if not given it will use", "current craft result").build())
            .icon(3, 3)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateParameter(opcode, program, 0);
                ItemStack item = processor.evaluateParameter(opcode, program, 1);
                processor.craftWait(program, inv, item);
                return true;
            }))
            .build();

    public static final Opcode EVENT_EXCEPTION = Opcode.builder()
            .id("ev_exception")
            .description(
                    TextFormatting.GREEN + "Event: exception",
                    "execute program on exception")
            .parameter(ParameterDescription.builder().name("exception").type(PAR_EXCEPTION).description("the exception code to catch").build())
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .icon(4, 3)
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
        register(EVAL_COUNTSTOR);
        register(EVAL_GETITEM);
        register(EVAL_REDSTONE);
        register(EVAL_GETRF);
        register(EVAL_GETMAXRF);
        register(TEST_GT);
        register(TEST_EQ);
        register(TEST_SET);
        register(DO_REDSTONE);
        register(DO_DELAY);
        register(DO_STOP);
        register(DO_LOG);
        register(DO_FETCHITEMS);
        register(DO_PUSHITEMS);
        register(DO_FETCHSTOR);
        register(DO_PUSHSTOR);
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
        register(DO_FETCH_CARD);
        register(DO_SETCRAFTID);
        register(DO_CRAFTWAIT);
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
        if (!opcode.isDeprecated()) {
            SORTED_OPCODES.add(opcode);
        }
    }
}

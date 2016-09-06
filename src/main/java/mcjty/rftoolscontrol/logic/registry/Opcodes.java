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
                    "in the network",
                    TextFormatting.BLUE + "Par 'side': node + side",
                    TextFormatting.BLUE + "Par 'level': amount of redstone to set",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("level").type(PAR_INTEGER).build())
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evalulateParameter(opcode, program, 0);
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
                    "Note: not yet supported on nodes",
                    TextFormatting.BLUE + "Par 'side': node + side",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .description(
                    TextFormatting.GREEN + "Event: redstone off",
                    "execute program when redstone",
                    "signal at a specific side (or in",
                    "general) goes off",
                    "Note: not yet supported on nodes",
                    TextFormatting.BLUE + "Par 'side': node + side",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(4, 0)
            .build();

    public static final Opcode EVENT_SIGNAL = Opcode.builder()
            .id("ev_signal")
            .description(
                    TextFormatting.GREEN + "Event: signal",
                    "execute program when a signal",
                    "is received from an rftools screen",
                    TextFormatting.BLUE + "Par 'signal': match this signal",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).build())
            .icon(5, 0)
            .build();

    public static final Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
            .description(
                    TextFormatting.GREEN + "Operation: wait",
                    "wait a specific number of ticks",
                    TextFormatting.BLUE + "Par 'ticks': amount of ticks to wait",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).build())
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
                    TextFormatting.BLUE + "Par 'inv': an adjacent inventory",
                    TextFormatting.BLUE + "Par 'slot': an optional slot",
                    TextFormatting.BLUE + "Par 'item': an optional item",
                    TextFormatting.YELLOW + "Result: amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evalulateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evalulateParameter(opcode, program, 2);
                int cnt = processor.countItem(inv, slot, item, program);
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
                    "the processor or a connected node",
                    TextFormatting.BLUE + "Par 'inv': an adjacent inventory",
                    TextFormatting.BLUE + "Par 'slot': an optional slot",
                    TextFormatting.YELLOW + "Result: the item (stack)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .icon(10, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evalulateParameter(opcode, program, 0);
                int slot = processor.evaluateIntParameter(opcode, program, 1);
                IItemHandler handler = processor.getItemHandlerAt(inv, program);
                if (handler != null) {
                    ItemStack item = handler.getStackInSlot(slot);
                    program.setLastValue(Parameter.builder().type(PAR_ITEM).value(ParameterValue.constant(item)).build());
                }
                return true;
            }))
            .build();

    public static final Opcode EVAL_REDSTONE = Opcode.builder()
            .id("eval_rs")
            .description(
                    TextFormatting.GREEN + "Eval: read redstone",
                    "read the redstone value coming",
                    "to a specific side of the processor",
                    "or a connected node",
                    TextFormatting.BLUE + "Par 'side': a specific side",
                    TextFormatting.YELLOW + "Result: redstone value (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(1, 0)
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evalulateParameter(opcode, program, 0);
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
                    "this manually",
                    TextFormatting.BLUE + "No parameters",
                    TextFormatting.YELLOW + "No result")
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
                    "console",
                    TextFormatting.BLUE + "Par 'message': the message",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("message").type(PAR_STRING).build())
            .icon(8, 0)
            .runnable(((processor, program, opcode) -> {
                String message = processor.evalulateStringParameter(opcode, program, 0);
                processor.log(message);
                return true;
            }))
            .build();

    public static final Opcode EVENT_TIMER = Opcode.builder()
            .id("ev_timer")
            .description(
                    TextFormatting.GREEN + "Event: repeat",
                    "execute program every <N> ticks",
                    TextFormatting.BLUE + "Par 'ticks': ticks to wait",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).build())
            .icon(9, 0)
            .build();

    public static final Opcode TEST_GT = Opcode.builder()
            .id("test_gt")
            .description(
                    TextFormatting.GREEN + "Test: greater than",
                    "check if the first value is greater",
                    "then the second value",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 > v2 (boolean)")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "to the second value",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 = v2 (boolean)")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    TextFormatting.BLUE + "Par 'inv': an adjacent inventory",
                    TextFormatting.BLUE + "Par 'slot': an optional slot",
                    TextFormatting.BLUE + "Par 'item': an optional item",
                    TextFormatting.BLUE + "Par 'amount': number of items",
                    TextFormatting.BLUE + "Par 'slotOut': slot in processor",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).build())
            .icon(0, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evalulateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evalulateParameter(opcode, program, 2);
                int amount = processor.evaluateIntParameter(opcode, program, 3);
                int slotOut = processor.evaluateIntParameter(opcode, program, 4);
                processor.fetchItems(program, inv, slot, item, amount, slotOut);
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
                    TextFormatting.BLUE + "Par 'inv': an adjacent inventory",
                    TextFormatting.BLUE + "Par 'slot': an optional slot",
                    TextFormatting.BLUE + "Par 'amount': number of items",
                    TextFormatting.BLUE + "Par 'slotIn': slot in processor",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).build())
            .icon(1, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evalulateParameter(opcode, program, 0);
                int slot = processor.evaluateIntParameter(opcode, program, 1);  // @todo allow null?
                int amount = processor.evaluateIntParameter(opcode, program, 2);
                int slotIn = processor.evaluateIntParameter(opcode, program, 3);
                processor.pushItems(program, inv, slot, amount, slotIn);
                return true;
            }))
            .build();

    public static final Opcode EVAL_COUNTINVINT = Opcode.builder()
            .id("eval_countinvint")
            .description(
                    TextFormatting.GREEN + "Eval: count items internal",
                    "count the amount of items in a",
                    "specific slot in the processor inventory",
                    TextFormatting.BLUE + "Par 'slot': internal slot",
                    TextFormatting.YELLOW + "Result: amount of items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
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
                    "the specified variable",
                    TextFormatting.BLUE + "Par 'var': variable index",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).build())
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
                    "add the two given integers",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 + v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "subtract the two given integers",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 - v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "divide the two given integers",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 / v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "multiply the two given integers",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 * v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "given integers",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 % v2 (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).build())
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
                    "concatenate the two given strings",
                    TextFormatting.BLUE + "Par 'v1': first value",
                    TextFormatting.BLUE + "Par 'v2': second value",
                    TextFormatting.YELLOW + "Result: v1 + v2 (string)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_STRING).build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_STRING).build())
            .icon(9, 1)
            .runnable(((processor, program, opcode) -> {
                String v1 = processor.evalulateStringParameter(opcode, program, 0);
                String v2 = processor.evalulateStringParameter(opcode, program, 1);
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
                    TextFormatting.BLUE + "Par 'item': the item to count",
                    TextFormatting.BLUE + "Par 'oredict': use oredict matching",
                    TextFormatting.BLUE + "Par 'routable': only routable",
                    TextFormatting.YELLOW + "Result: amount of items (integer)",
                    TextFormatting.RED + "Needs storage scanner module")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).build())
            .icon(0, 2)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evalulateParameter(opcode, program, 0);
                boolean oredict = processor.evalulateBoolParameter(opcode, program, 1);
                boolean routable = processor.evalulateBoolParameter(opcode, program, 2);
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
                    TextFormatting.BLUE + "Par 'item': the item to fetch",
                    TextFormatting.BLUE + "Par 'oredict': use oredict matching",
                    TextFormatting.BLUE + "Par 'routable': only routable",
                    TextFormatting.BLUE + "Par 'amount': number of items",
                    TextFormatting.BLUE + "Par 'slotOut': slot in processor",
                    TextFormatting.YELLOW + "Result: amount of items fetched (integer)",
                    TextFormatting.RED + "Needs storage scanner module")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).build())
            .icon(1, 2)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evalulateParameter(opcode, program, 0);
                boolean oredict = processor.evalulateBoolParameter(opcode, program, 1);
                boolean routable = processor.evalulateBoolParameter(opcode, program, 2);
                int amount = processor.evaluateIntParameter(opcode, program, 3);
                int slotOut = processor.evaluateIntParameter(opcode, program, 4);
                int cnt = processor.fetchItemsStorage(program, item, routable, oredict, amount, slotOut);
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
                    TextFormatting.BLUE + "Par 'amount': number of items",
                    TextFormatting.BLUE + "Par 'slotIn': slot in processor",
                    TextFormatting.YELLOW + "Result: amount of items inserted (integer)",
                    TextFormatting.RED + "Needs storage scanner module")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).build())
            .icon(2, 2)
            .runnable(((processor, program, opcode) -> {
                int amount = processor.evaluateIntParameter(opcode, program, 2);
                int slotIn = processor.evaluateIntParameter(opcode, program, 3);
                int cnt = processor.pushItemsStorage(program, amount, slotIn);
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
                    "processor or a connected node",
                    TextFormatting.BLUE + "Par 'side': an adjacent block",
                    TextFormatting.YELLOW + "Result: amount of RF (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).build())
            .icon(3, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evalulateParameter(opcode, program, 0);
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
                    "processor or a connected node",
                    TextFormatting.BLUE + "Par 'side': an adjacent block",
                    TextFormatting.YELLOW + "Result: max amount of RF (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).build())
            .icon(4, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evalulateParameter(opcode, program, 0);
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
                    "are not adjacent to each other",
                    TextFormatting.BLUE + "No parameters",
                    TextFormatting.YELLOW + "No result")
            .opcodeOutput(SINGLE)
            .icon(11, 1)
            .runnable(((processor, program, opcode) -> {
                return true;
            }))
            .build();

    public static final Map<String, Opcode> OPCODES = new HashMap<>();
    public static final List<Opcode> SORTED_OPCODES = new ArrayList<>();

    public static void init() {
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(EVENT_TIMER);
        register(EVENT_SIGNAL);
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
        register(DO_REDSTONE);
        register(DO_DELAY);
        register(DO_STOP);
        register(DO_LOG);
        register(DO_FETCHITEMS);
        register(DO_PUSHITEMS);
        register(DO_FETCHSTOR);
        register(DO_PUSHSTOR);
        register(DO_SETVAR);
        register(DO_ADD);
        register(DO_SUBTRACT);
        register(DO_DIVIDE);
        register(DO_MULTIPLY);
        register(DO_MODULO);
        register(DO_CONCAT);
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
        SORTED_OPCODES.add(opcode);
    }
}

package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.tools.ItemStackTools;
import mcjty.rftoolscontrol.api.code.Opcode;
import mcjty.rftoolscontrol.api.parameters.*;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import mcjty.rftoolscontrol.logic.running.ProgException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static mcjty.rftoolscontrol.api.code.IOpcodeRunnable.OpcodeResult.*;
import static mcjty.rftoolscontrol.api.code.OpcodeCategory.*;
import static mcjty.rftoolscontrol.api.code.OpcodeOutput.*;
import static mcjty.rftoolscontrol.api.parameters.ParameterType.*;

public class Opcodes {

    public static final Opcode DO_REDSTONE = Opcode.builder()
            .id("do_rs")
            .description(
                    TextFormatting.GREEN + "Operation: set redstone",
                    "set redstone level at a specific side",
                    "on the processor or a node in the network")
            .opcodeOutput(SINGLE)
            .category(CATEGORY_REDSTONE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .parameter(ParameterDescription.builder().name("level").type(PAR_INTEGER).description("redstone level").build())
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateSideParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_REDSTONE)
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
            .category(CATEGORY_REDSTONE)
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
            .category(CATEGORY_COMMUNICATION)
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory", "(not for storage)").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to count").build())
            .parameter(ParameterDescription.builder().name("oredict").type(PAR_BOOLEAN).optional().description("use oredict matching").build())
            .parameter(ParameterDescription.builder().name("routable").type(PAR_BOOLEAN).optional().description("count routable items", "(only for storage)").build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 2);
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("slot in inventory").build())
            .icon(10, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                int slot = processor.evaluateIntParameter(opcode, program, 1);
                IItemHandler handler = processor.getItemHandlerAt(inv);
                ItemStack item = handler.getStackInSlot(slot);
                if (ItemStackTools.isValid(item)) {
                    item = item.copy();
                }
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
            .category(CATEGORY_REDSTONE)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).description("side of (networked) block").build())
            .icon(1, 0)
            .runnable(((processor, program, opcode) -> {
                BlockSide side = processor.evaluateSideParameterNonNull(opcode, program, 0);
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
            .opcodeOutput(YESNO)
            .category(CATEGORY_NUMBERS)
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
            .opcodeOutput(YESNO)
            .category(CATEGORY_NUMBERS)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_INTEGER).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_INTEGER).description("second value").build())
            .icon(11, 0)
            .runnable(((processor, program, opcode) -> {
                int v1 = processor.evaluateIntParameter(opcode, program, 0);
                int v2 = processor.evaluateIntParameter(opcode, program, 1);
                return v1 == v2 ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode TEST_GT_VAR = Opcode.builder()
            .id("test_gt_var")
            .description(
                    TextFormatting.GREEN + "Test: greater than var",
                    "check if the last result is greater",
                    "then a value in a variable")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable index").build())
            .icon(10, 5)
            .runnable(((processor, program, opcode) -> {
                int var = processor.evaluateIntParameter(opcode, program, 0);
                return ((ProcessorTileEntity) processor).testGreater(program, var) ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode TEST_EQ_VAR = Opcode.builder()
            .id("test_eq_var")
            .description(
                    TextFormatting.GREEN + "Test: equality with var",
                    "check if the last result is equal",
                    "to a value in a variable")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable index").build())
            .icon(11, 5)
            .runnable(((processor, program, opcode) -> {
                int var = processor.evaluateIntParameter(opcode, program, 0);
                return ((ProcessorTileEntity) processor).testEquality(program, var) ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode DO_FETCHLIQUID = Opcode.builder()
            .id("do_fetchliquid")
            .description(
                    TextFormatting.GREEN + "Operation: fetch liquid",
                    "fetch a liquid from an external tank adjacent",
                    "to the processor or a connected node and place",
                    "the result in an internal tank (provided by",
                    "a multi-tank adjacent to the processor)")
            .outputDescription("amount of fluid fetched in mb (integer)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("tank").type(PAR_INVENTORY).description("tank adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of mb to fetch").build())
            .parameter(ParameterDescription.builder().name("fluid").type(PAR_FLUID).optional().description("optional fluid to fetch").build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).description("internal (processor) fluid slot for result").build())
            .icon(0, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                int amount = processor.evaluateIntParameter(opcode, program, 1);
                FluidStack fluidStack = processor.evaluateFluidParameter(opcode, program, 2);
                int slotOut = processor.evaluateIntParameter(opcode, program, 3);
                int cnt = ((ProcessorTileEntity)processor).fetchLiquid(program, inv, amount, fluidStack, slotOut);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_PUSHLIQUID = Opcode.builder()
            .id("do_pushliquid")
            .description(
                    TextFormatting.GREEN + "Operation: push liquid",
                    "push a liquid to an external tank adjacent",
                    "to the processor or a connected node",
                    "from an internal tank (provided by",
                    "a multi-tank adjacent to the processor)")
            .outputDescription("amount of fluid pushed in mb (integer)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("tank").type(PAR_INVENTORY).description("tank adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).description("amount of mb to push").build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).description("internal (processor) fluid slot for input").build())
            .icon(1, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                int amount = processor.evaluateIntParameter(opcode, program, 1);
                int slotIn = processor.evaluateIntParameter(opcode, program, 2);
                int cnt = ((ProcessorTileEntity)processor).pushLiquid(program, inv, amount, slotIn);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
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
            .category(CATEGORY_ITEMS)
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
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 2);
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory (not", "used for storage)").build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).optional().description("amount of items to push").build())
            .parameter(ParameterDescription.builder().name("slotIn").type(PAR_INTEGER).description("internal (processor) slot for input").build())
            .icon(1, 1)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal (processor) slot").build())
            .icon(2, 1)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                ItemStack stack = processor.getItemInternal(program, slot);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(
                        ItemStackTools.isEmpty(stack) ? 0 : ItemStackTools.getStackSize(stack))).build());
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
            .category(CATEGORY_NUMBERS)
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
            .category(CATEGORY_NUMBERS)
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
            .category(CATEGORY_NUMBERS)
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
            .category(CATEGORY_NUMBERS)
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
            .category(CATEGORY_NUMBERS)
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
            .parameter(ParameterDescription.builder().name("v1").type(PAR_STRING).description("first value").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_STRING).description("second value").build())
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
            .category(CATEGORY_ENERGY)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(3, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_ENERGY)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(4, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
            .outputDescription("amount of failed items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("cardInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "with crafting cards").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft or empty", "for default from ticket").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last slot of that range").build())
            .icon(8, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                Inventory cardInv = processor.evaluateInventoryParameter(opcode, program, 1);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 2);
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
            .category(CATEGORY_CRAFTING)
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
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                Inventory cardInv = processor.evaluateInventoryParameterNonNull(opcode, program, 1);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 2);
                int slot1 = processor.evaluateIntParameter(opcode, program, 3);
                int slot2 = processor.evaluateIntParameter(opcode, program, 4);
                Inventory destInv = processor.evaluateInventoryParameterNonNull(opcode, program, 5);
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
            .category(CATEGORY_ITEMS)
            .category(CATEGORY_CRAFTING)
            .outputDescription("amount of failed items (integer)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory adjacent to (networked)", "block or empty to access storage").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("first internal slot for input").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last internal slot for input").build())
            .parameter(ParameterDescription.builder().name("extSlot").type(PAR_INTEGER).optional().description("first external slot").build())
            .icon(11, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                int slot1 = processor.evaluateIntParameter(opcode, program, 1);
                int slot2 = processor.evaluateIntParameter(opcode, program, 2);
                Integer extSlot = processor.evaluateIntegerParameter(opcode, program, 3);
                int failed = ((ProcessorTileEntity)processor).pushItemsMulti(program, inv, slot1, slot2, extSlot);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(failed)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_PUSHWORKBENCH = Opcode.builder()
            .id("do_pushworkbench")
            .description(
                    TextFormatting.GREEN + "Operation: push items to workbench",
                    "push multiple items from the internal inventory to a",
                    "workbench adjacent to the processor or a connected node.",
                    "This operation will use a crafting card to push exactly (and",
                    "only) the items that are required for the craft operation",
                    "and that are not already present in the workbench.",
                    "Crafting card has to be present in the workbench storage.",
                    "Returns true if all items are present to craft",
                    "and false if items are missing or the workbench already has",
                    "incompatible items in it")
            .category(CATEGORY_ITEMS)
            .category(CATEGORY_CRAFTING)
            .outputDescription("true on success (boolean)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_SIDE).description("workbench adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to push ingredients for. If not given", "it will use current craft result").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("first internal slot for input").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last internal slot for input").build())
            .icon(6, 7)
            .runnable(((processor, program, opcode) -> {
                BlockSide workbench = processor.evaluateSideParameterNonNull(opcode, program, 0);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 1);
                int slot1 = processor.evaluateIntParameter(opcode, program, 2);
                int slot2 = processor.evaluateIntParameter(opcode, program, 3);
                boolean result = ((ProcessorTileEntity)processor).pushItemsWorkbench(program, workbench, item, slot1, slot2);
                program.setLastValue(Parameter.builder().type(PAR_BOOLEAN).value(ParameterValue.constant(result)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode DO_SETCRAFTTICKET = Opcode.builder()
            .id("do_setticket")
            .description(
                    TextFormatting.GREEN + "Operation: set craft ticket",
                    "set a craft ticket so that you can resume",
                    "a previously stored crafting operation")
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("item to wait for. If not given", "it will use current craft result").build())
            .icon(3, 3)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 1);
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
            .category(CATEGORY_CRAFTING)
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
            .category(CATEGORY_CRAFTING)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("the item to request").build())
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).optional().description("inventory for the end result").build())
            .icon(6, 3)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateItemParameterNonNull(opcode, program, 0);
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 1);
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
            .category(CATEGORY_ITEMS)
            .outputDescription("itemstack in target slot (stack)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal slot in processor").build())
            .icon(7, 3)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                ItemStack item = processor.getItemInternal(program, slot);
                if (ItemStackTools.isValid(item)) {
                    item = item.copy();
                }
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
                    "of the loop before this opcode.",
                    "The red output of this opcode is executed when the",
                    "loop ends")
            .opcodeOutput(YESNO)
            .category(CATEGORY_NUMBERS)
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable index (first var is 0)").build())
            .parameter(ParameterDescription.builder().name("end").type(PAR_INTEGER).description("end index (inclusive)").build())
            .icon(8, 3)
            .runnable(((processor, program, opcode) -> {
                int varIdx = processor.evaluateIntParameter(opcode, program, 0);
                int end = processor.evaluateIntParameter(opcode, program, 1);
                return ((ProcessorTileEntity)processor).handleLoop(program, varIdx, end);
            }))
            .build();

    public static final Opcode TEST_CALL = Opcode.builder()
            .id("test_call")
            .description(
                    TextFormatting.GREEN + "Test: call function",
                    "call a function (signal). When that code has",
                    "done executing resume execution here",
                    "Note that the signal has to be defined on the",
                    "same card!")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).description("name of the signal to call").build())
            .icon(7, 7)
            .runnable(((processor, program, opcode) -> {
                String signal = processor.evaluateStringParameterNonNull(opcode, program, 0);
                ((ProcessorTileEntity)processor).handleCall(program, signal);
                return HOLD;
            }))
            .build();

    public static final Opcode EVAL_INTEGER = Opcode.builder()
            .id("eval_integer")
            .description(
                    TextFormatting.GREEN + "Eval: integer",
                    "evaluate an integer and set it as",
                    "the result for future opcodes to use")
            .outputDescription("integer result (integer)")
            .category(CATEGORY_NUMBERS)
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
            .category(CATEGORY_CRAFTING)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("cardInv").type(PAR_INVENTORY).description("inventory adjacent to (networked) block", "with crafting cards").build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).optional().description("the item to craft or empty", "for default from ticket").build())
            .parameter(ParameterDescription.builder().name("slot1").type(PAR_INTEGER).description("start of internal slot range for ingredients").build())
            .parameter(ParameterDescription.builder().name("slot2").type(PAR_INTEGER).description("last slot of that range").build())
            .icon(0, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory cardInv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                ItemStack item = processor.evaluateItemParameter(opcode, program, 1);
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
                Inventory inv = processor.evaluateInventoryParameter(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_INVENTORY).value(ParameterValue.constant(inv)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_EXAMINELIQUID = Opcode.builder()
            .id("eval_examineliquid")
            .description(
                    TextFormatting.GREEN + "Eval: examine liquid",
                    "examine a liquid in a specific slot",
                    "from an external tank adjacent to",
                    "the processor or a connected node")
            .outputDescription("fluidstack in target slot (stack)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("tank").type(PAR_INVENTORY).description("tank adjacent to (networked) block").build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).optional().description("slot in inventory", "(defaults to 0)").build())
            .icon(2, 2)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                FluidStack stack = ((ProcessorTileEntity)processor).examineLiquid(inv, slot);
                program.setLastValue(Parameter.builder().type(PAR_FLUID).value(ParameterValue.constant(stack)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_EXAMINELIQUIDINT = Opcode.builder()
            .id("eval_examineliquidint")
            .description(
                    TextFormatting.GREEN + "Eval: examine liquid internal",
                    "examine a liquid in a liquid slot")
            .outputDescription("fluidstack in target slot (stack)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).description("internal liquid slot").build())
            .icon(4, 7)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                FluidStack stack = ((ProcessorTileEntity)processor).examineLiquidInternal(program, slot);
                program.setLastValue(Parameter.builder().type(PAR_FLUID).value(ParameterValue.constant(stack)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_GETLIQUIDNAME = Opcode.builder()
            .id("eval_getliquidname")
            .description(
                    TextFormatting.GREEN + "Eval: get liquid name",
                    "get the readable name from a liquid")
            .outputDescription("item name (string)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("liquid").type(PAR_FLUID).description("liquid to get name from").build())
            .icon(5, 7)
            .runnable(((processor, program, opcode) -> {
                FluidStack fluid = processor.evaluateFluidParameterNonNull(opcode, program, 0);
                String name = fluid.getLocalizedName();
                program.setLastValue(Parameter.builder().type(PAR_STRING).value(ParameterValue.constant(name)).build());
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
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(3, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_INVENTORY).description("side of (networked) block").build())
            .icon(4, 4)
            .runnable(((processor, program, opcode) -> {
                Inventory side = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_COMMUNICATION)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("signal").type(PAR_STRING).description("signal name").build())
            .icon(5, 4)
            .runnable(((processor, program, opcode) -> {
                String signal = processor.evaluateStringParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("item to get damage from").build())
            .icon(6, 4)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateItemParameterNonNull(opcode, program, 0);
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
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("item to get name from").build())
            .icon(7, 4)
            .runnable(((processor, program, opcode) -> {
                ItemStack item = processor.evaluateItemParameterNonNull(opcode, program, 0);
                String name = item.getDisplayName();
                program.setLastValue(Parameter.builder().type(PAR_STRING).value(ParameterValue.constant(name)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode TEST_NBT_EQ = Opcode.builder()
            .id("test_nbt_eq")
            .description(
                    TextFormatting.GREEN + "Test: NBT equality",
                    "check if a specific tag of the first item",
                    "is exactly equal to the value of that tag of",
                    "the second item")
            .category(CATEGORY_ITEMS)
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("v1").type(PAR_ITEM).description("first item").build())
            .parameter(ParameterDescription.builder().name("v2").type(PAR_ITEM).description("second item").build())
            .parameter(ParameterDescription.builder().name("tag").type(PAR_STRING).description("the tag to compare").build())
            .icon(8, 4)
            .runnable(((processor, program, opcode) -> {
                ItemStack v1 = processor.evaluateItemParameterNonNull(opcode, program, 0);
                ItemStack v2 = processor.evaluateItemParameterNonNull(opcode, program, 1);
                String tag = processor.evaluateStringParameterNonNull(opcode, program, 2);
                boolean rc = ((ProcessorTileEntity) processor).compareNBTTag(v1, v2, tag);
                return rc ? POSITIVE : NEGATIVE;
            }))
            .build();

    public static final Opcode DO_SETTOKEN = Opcode.builder()
            .id("do_settoken")
            .description(
                    TextFormatting.GREEN + "Operation: set value in token",
                    "copy the last returned value to a token",
                    "item in an internal slot")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("tokenSlot").type(PAR_INTEGER).description("internal (processor) slot with token").build())
            .icon(9, 4)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                ((ProcessorTileEntity)processor).setValueInToken(program, slot);
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_GETTOKEN = Opcode.builder()
            .id("eval_gettoken")
            .description(
                    TextFormatting.GREEN + "Eval: get value from token",
                    "get the value out of a token in an internal slot")
            .outputDescription("token value (any type)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("tokenSlot").type(PAR_INTEGER).description("internal (processor) slot with token").build())
            .icon(10, 4)
            .runnable(((processor, program, opcode) -> {
                int slot = processor.evaluateIntParameter(opcode, program, 0);
                Parameter parameter = ((ProcessorTileEntity) processor).getParameterFromToken(program, slot);
                program.setLastValue(parameter);
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVENT_MESSAGE = Opcode.builder()
            .id("ev_message")
            .description(
                    TextFormatting.GREEN + "Event: message",
                    "receive a message from another processor",
                    "If that message was sent with a variable",
                    "then the last value will be set to that")
            .outputDescription("optional value (any)")
            .category(CATEGORY_COMMUNICATION)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("name").type(PAR_STRING).description("matching message name").build())
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .isEvent(true)
            .icon(11, 4)
            .build();
    public static final Opcode DO_MESSAGE = Opcode.builder()
            .id("do_message")
            .description(
                    TextFormatting.GREEN + "Operation: send message",
                    "send a message to another processor",
                    "This needs a network identifier in a slot",
                    "and an advanced networking card",
                    "Can optionally send a variable")
            .category(CATEGORY_COMMUNICATION)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("name").type(PAR_STRING).description("message name").build())
            .parameter(ParameterDescription.builder().name("idSlot").type(PAR_INTEGER).description("internal (processor) slot with identifier").build())
            .parameter(ParameterDescription.builder().name("variable").type(PAR_INTEGER).optional().description("variable index to send over").build())
            .icon(4, 5)
            .runnable(((processor, program, opcode) -> {
                String name = processor.evaluateStringParameterNonNull(opcode, program, 0);
                int idSlot = processor.evaluateIntParameter(opcode, program, 1);
                Integer variable = processor.evaluateIntegerParameter(opcode, program, 2);
                processor.sendMessage(program, idSlot, name, variable);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_BOX_OLD = Opcode.builder()
            .id("do_gfx_box")
            .description(
                    TextFormatting.GREEN + "Operation: gfx box",
                    "draw a box",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .deprecated(true)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this box)").build())
            .parameter(ParameterDescription.builder().name("x").type(PAR_INTEGER).description("x location").build())
            .parameter(ParameterDescription.builder().name("y").type(PAR_INTEGER).description("y location").build())
            .parameter(ParameterDescription.builder().name("w").type(PAR_INTEGER).description("width").build())
            .parameter(ParameterDescription.builder().name("h").type(PAR_INTEGER).description("height").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(5, 5)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                int x = processor.evaluateIntParameter(opcode, program, 1);
                int y = processor.evaluateIntParameter(opcode, program, 2);
                int w = processor.evaluateIntParameter(opcode, program, 3);
                int h = processor.evaluateIntParameter(opcode, program, 4);
                int color = processor.evaluateIntParameter(opcode, program, 5);
                processor.gfxDrawBox(program, id, x, y, w, h, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_LINE_OLD = Opcode.builder()
            .id("do_gfx_line")
            .description(
                    TextFormatting.GREEN + "Operation: gfx line",
                    "draw a line",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .deprecated(true)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this line)").build())
            .parameter(ParameterDescription.builder().name("x1").type(PAR_INTEGER).description("start x location").build())
            .parameter(ParameterDescription.builder().name("y1").type(PAR_INTEGER).description("start y location").build())
            .parameter(ParameterDescription.builder().name("x2").type(PAR_INTEGER).description("end x location").build())
            .parameter(ParameterDescription.builder().name("y2").type(PAR_INTEGER).description("end y location").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(6, 5)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                int x1 = processor.evaluateIntParameter(opcode, program, 1);
                int y1 = processor.evaluateIntParameter(opcode, program, 2);
                int x2 = processor.evaluateIntParameter(opcode, program, 3);
                int y2 = processor.evaluateIntParameter(opcode, program, 4);
                int color = processor.evaluateIntParameter(opcode, program, 5);
                processor.gfxDrawLine(program, id, x1, y1, x2, y2, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_TEXT_OLD = Opcode.builder()
            .id("do_gfx_text")
            .description(
                    TextFormatting.GREEN + "Operation: gfx text",
                    "draw text",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .deprecated(true)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this text)").build())
            .parameter(ParameterDescription.builder().name("x").type(PAR_INTEGER).description("x location").build())
            .parameter(ParameterDescription.builder().name("y").type(PAR_INTEGER).description("y location").build())
            .parameter(ParameterDescription.builder().name("text").type(PAR_STRING).description("text").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(7, 5)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                int x = processor.evaluateIntParameter(opcode, program, 1);
                int y = processor.evaluateIntParameter(opcode, program, 2);
                String txt = processor.evaluateStringParameterNonNull(opcode, program, 3);
                int color = processor.evaluateIntParameter(opcode, program, 4);
                processor.gfxDrawText(program, id, x, y, txt, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_BOX = Opcode.builder()
            .id("do_box")
            .description(
                    TextFormatting.GREEN + "Operation: gfx box",
                    "draw a box",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this box)").build())
            .parameter(ParameterDescription.builder().name("loc").type(PAR_TUPLE).description("location").build())
            .parameter(ParameterDescription.builder().name("size").type(PAR_TUPLE).description("size").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(5, 6)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                Tuple loc = processor.evaluateTupleParameterNonNull(opcode, program, 1);
                Tuple size = processor.evaluateTupleParameterNonNull(opcode, program, 2);
                int color = processor.evaluateIntParameter(opcode, program, 3);
                processor.gfxDrawBox(program, id, loc, size, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_LINE = Opcode.builder()
            .id("do_line")
            .description(
                    TextFormatting.GREEN + "Operation: gfx line",
                    "draw a line",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this line)").build())
            .parameter(ParameterDescription.builder().name("loc1").type(PAR_TUPLE).description("location 1").build())
            .parameter(ParameterDescription.builder().name("loc2").type(PAR_TUPLE).description("location 2").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(6, 6)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                Tuple loc1 = processor.evaluateTupleParameterNonNull(opcode, program, 1);
                Tuple loc2 = processor.evaluateTupleParameterNonNull(opcode, program, 2);
                int color = processor.evaluateIntParameter(opcode, program, 3);
                processor.gfxDrawLine(program, id, loc1, loc2, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_TEXT = Opcode.builder()
            .id("do_text")
            .description(
                    TextFormatting.GREEN + "Operation: gfx text",
                    "draw text",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).description("id (used to identify this text)").build())
            .parameter(ParameterDescription.builder().name("loc").type(PAR_TUPLE).description("location").build())
            .parameter(ParameterDescription.builder().name("text").type(PAR_STRING).description("text").build())
            .parameter(ParameterDescription.builder().name("color").type(PAR_INTEGER).description("color").build())
            .icon(7, 6)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameterNonNull(opcode, program, 0);
                Tuple loc = processor.evaluateTupleParameterNonNull(opcode, program, 1);
                String txt = processor.evaluateStringParameterNonNull(opcode, program, 2);
                int color = processor.evaluateIntParameter(opcode, program, 3);
                processor.gfxDrawText(program, id, loc, txt, color);
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_GFX_CLEAR = Opcode.builder()
            .id("do_gfx_clear")
            .description(
                    TextFormatting.GREEN + "Operation: gfx clear",
                    "clear an operation with a specific id or",
                    "all operations",
                    TextFormatting.RED + "Needs a graphics card")
            .category(CATEGORY_GRAPHICS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("id").type(PAR_STRING).optional().description("id to delete or empty to delete all").build())
            .icon(8, 5)
            .runnable(((processor, program, opcode) -> {
                String id = processor.evaluateStringParameter(opcode, program, 0);
                processor.gfxClear(program, id);
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_RANDOM = Opcode.builder()
            .id("eval_random")
            .description(
                    TextFormatting.GREEN + "Eval: random integer",
                    "get a random integer between two values")
            .outputDescription("random result (integer)")
            .category(CATEGORY_NUMBERS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("lower").type(PAR_INTEGER).description("lower bound").build())
            .parameter(ParameterDescription.builder().name("upper").type(PAR_INTEGER).description("upper bound (exclusive)").build())
            .icon(9, 5)
            .runnable(((processor, program, opcode) -> {
                int lower = processor.evaluateIntParameter(opcode, program, 0);
                int upper = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(
                        ParameterValue.constant(Functions.RANDOM.nextInt(upper-lower)+lower)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_TUPLE = Opcode.builder()
            .id("eval_tuple")
            .description(
                    TextFormatting.GREEN + "Eval: tuple",
                    "evaluate a tuple (two integers) and set it",
                    "as the result for future opcodes to use")
            .outputDescription("tuple result (tuple)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("x").type(PAR_INTEGER).description("x (or first) value for the tuple").build())
            .parameter(ParameterDescription.builder().name("y").type(PAR_INTEGER).description("y (or second) value for the tuple").build())
            .icon(4, 6)
            .runnable(((processor, program, opcode) -> {
                int x = processor.evaluateIntParameter(opcode, program, 0);
                int y = processor.evaluateIntParameter(opcode, program, 1);
                program.setLastValue(Parameter.builder().type(PAR_TUPLE).value(ParameterValue.constant(new Tuple(x, y))).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVENT_GFX_SELECT = Opcode.builder()
            .id("ev_select")
            .description(
                    TextFormatting.GREEN + "Event: gfx select",
                    "execute program when a screen with a",
                    "vector module is selected.",
                    "The last value will be set to the tuple",
                    "of the selected location")
            .category(CATEGORY_GRAPHICS)
            .outputDescription("selected location (tuple)")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("single").type(PAR_BOOLEAN).optional().description("only one simultaneous run").build())
            .icon(8, 6)
            .build();
    public static final Opcode EVAL_SLOTS = Opcode.builder()
            .id("eval_slots")
            .description(
                    TextFormatting.GREEN + "Eval: get number of slots",
                    "return the amount of slots in an",
                    "external inventory")
            .outputDescription("amount of slots (integer)")
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).description("inventory adjacent to (networked)", "block").build())
            .icon(10, 6)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evaluateInventoryParameterNonNull(opcode, program, 0);
                int cnt = ((ProcessorTileEntity)processor).countSlots(inv, program);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_ITEM = Opcode.builder()
            .id("eval_item")
            .description(
                    TextFormatting.GREEN + "Eval: evaluate item",
                    "set the last value to a specific item")
            .outputDescription("itemstack (stack)")
            .category(CATEGORY_ITEMS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).description("an item").build())
            .icon(9, 6)
            .runnable(((processor, program, opcode) -> {
                ItemStack stack = processor.evaluateItemParameterNonNull(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_ITEM).value(ParameterValue.constant(stack)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_FLUID = Opcode.builder()
            .id("eval_fluid")
            .description(
                    TextFormatting.GREEN + "Eval: evaluate fluid",
                    "set the last value to a specific fluid")
            .outputDescription("fluidstack (fluid)")
            .category(CATEGORY_LIQUIDS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("fluid").type(PAR_FLUID).description("a fluid stack (bucket)").build())
            .icon(11, 6)
            .runnable(((processor, program, opcode) -> {
                FluidStack stack = processor.evaluateFluidParameterNonNull(opcode, program, 0);
                program.setLastValue(Parameter.builder().type(PAR_FLUID).value(ParameterValue.constant(stack)).build());
                return POSITIVE;
            }))
            .build();

    public static final Opcode EVAL_VECTOR = Opcode.builder()
            .id("eval_vector")
            .description(
                    TextFormatting.GREEN + "Eval: vector",
                    "set the last value to a vector",
                    "Leave empty for empty vector")
            .outputDescription("vector (vector)")
            .category(CATEGORY_VECTORS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).optional().description("vector").build())
            .icon(8, 7)
            .runnable(((processor, program, opcode) -> {
                List<Parameter> vector = processor.evaluateVectorParameter(opcode, program, 0);
                if (vector == null) {
                    vector = Collections.emptyList();
                }
                program.setLastValue(Parameter.builder().type(PAR_VECTOR).value(ParameterValue.constant(vector)).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode EVAL_VECTOR_ELEMENT = Opcode.builder()
            .id("eval_vector_element")
            .description(
                    TextFormatting.GREEN + "Eval: evaluate element from vector",
                    "get a specific element out of a vector")
            .outputDescription("element (any type)")
            .category(CATEGORY_VECTORS)
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).description("vector to get item from").build())
            .parameter(ParameterDescription.builder().name("index").type(PAR_INTEGER).description("index (starts at 0)").build())
            .icon(10, 7)
            .runnable(((processor, program, opcode) -> {
                List<Parameter> vector = processor.evaluateVectorParameterNonNull(opcode, program, 0);
                int index = processor.evaluateIntParameter(opcode, program, 1);
                if (index < 0 || index >= vector.size()) {
                    throw new ProgException(ExceptionType.EXCEPT_BADINDEX);
                }
                program.setLastValue(vector.get(index));
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_VECTOR_PUSH = Opcode.builder()
            .id("do_vector_push")
            .description(
                    TextFormatting.GREEN + "Operation: push item to vector",
                    "add an item in a variable to a vector and",
                    "return a new vector")
            .category(CATEGORY_VECTORS)
            .outputDescription("new vector (vector)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).description("vector").build())
            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable to add to vector").build())
            .icon(9, 7)
            .runnable(((processor, program, opcode) -> {
                List<Parameter> vector = processor.evaluateVectorParameterNonNull(opcode, program, 0);
                int var = processor.evaluateIntParameter(opcode, program, 1);
                List<Parameter> newvector = new ArrayList<Parameter>(vector);
                newvector.add(processor.getVariable(program, var));
                program.setLastValue(Parameter.builder().type(PAR_VECTOR).value(ParameterValue.constant(Collections.unmodifiableList(newvector))).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_VECTOR_PUSH_INT = Opcode.builder()
            .id("do_vector_push_int")
            .description(
                    TextFormatting.GREEN + "Operation: push integer to vector",
                    "add an integer to a vector and",
                    "return a new vector")
            .category(CATEGORY_VECTORS)
            .outputDescription("new vector (vector)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).description("vector").build())
            .parameter(ParameterDescription.builder().name("integer").type(PAR_INTEGER).description("integer to add to vector").build())
            .icon(9, 8)
            .runnable(((processor, program, opcode) -> {
                List<Parameter> vector = processor.evaluateVectorParameterNonNull(opcode, program, 0);
                int integer = processor.evaluateIntParameter(opcode, program, 1);
                List<Parameter> newvector = new ArrayList<Parameter>(vector);
                newvector.add(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(integer)).build());
                program.setLastValue(Parameter.builder().type(PAR_VECTOR).value(ParameterValue.constant(Collections.unmodifiableList(newvector))).build());
                return POSITIVE;
            }))
            .build();
    public static final Opcode DO_VECTOR_POP = Opcode.builder()
            .id("do_vector_pop")
            .description(
                    TextFormatting.GREEN + "Operation: pop item from vector",
                    "remove the last item from a vector and",
                    "return a new vector")
            .category(CATEGORY_VECTORS)
            .outputDescription("new vector (vector)")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).description("vector").build())
            .icon(11, 7)
            .runnable(((processor, program, opcode) -> {
                List<Parameter> vector = processor.evaluateVectorParameterNonNull(opcode, program, 0);
                List<Parameter> newvector = new ArrayList<Parameter>(vector.size()-1);
                for (int i = 0 ; i < vector.size()-1 ; i++) {
                    newvector.add(vector.get(i));
                }
                program.setLastValue(Parameter.builder().type(PAR_VECTOR).value(ParameterValue.constant(Collections.unmodifiableList(newvector))).build());
                return POSITIVE;
            }))
            .build();
//    public static final Opcode TEST_LOOP_VECTOR = Opcode.builder()
//            .id("test_loop_vector")
//            .description(
//                    TextFormatting.GREEN + "Test: loop vector",
//                    "loop over all elements in a vector",
//                    "In every iteration of the loop the last",
//                    "value will be set to that element",
//                    "The given variable will be used for the index",
//                    "in the loop. You can examine that during the loop",
//                    "The red output of this opcode is executed when the",
//                    "loop ends")
//    .category(CATEGORY_VECTORS)
//            .opcodeOutput(YESNO)
//            .parameter(ParameterDescription.builder().name("vector").type(PAR_VECTOR).description("vector to iterate").build())
//            .parameter(ParameterDescription.builder().name("var").type(PAR_INTEGER).description("variable index for the loop").build())
//            .icon(7, 8)
//            .runnable(((processor, program, opcode) -> {
//                List<Parameter> vector = processor.evaluateVectorParameterNonNull(opcode, program, 0);
//                int varIdx = processor.evaluateIntParameter(opcode, program, 1);
//                return ((ProcessorTileEntity)processor).handleLoop(program, vector, varIdx);
//            }))
//            .build();


    public static final Map<String, Opcode> OPCODES = new HashMap<>();
    public static final List<Opcode> SORTED_OPCODES = new ArrayList<>();

    public static void init() {
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(EVENT_TIMER);
        register(EVENT_SIGNAL);
        register(EVENT_GFX_SELECT);
        register(EVENT_MESSAGE);
        register(EVENT_CRAFT);
        register(EVENT_CRAFTRESUME);
        register(EVENT_EXCEPTION);
        register(DO_WIRE);
        register(EVAL_COUNTINV);
        register(EVAL_COUNTINVINT);
        register(EVAL_SLOTS);
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
        register(EVAL_EXAMINELIQUID);
        register(EVAL_EXAMINELIQUIDINT);
        register(EVAL_GETLIQUIDNAME);
        register(EVAL_RANDOM);
        register(EVAL_INTEGER);
        register(EVAL_STRING);
        register(EVAL_TUPLE);
        register(EVAL_INVENTORY);
        register(EVAL_ITEM);
        register(EVAL_FLUID);
        register(EVAL_GETTOKEN);
        register(EVAL_LOCK);
        register(EVAL_VECTOR);
        register(EVAL_VECTOR_ELEMENT);
        register(TEST_GT);
        register(TEST_GT_VAR);
        register(TEST_EQ);
        register(TEST_EQ_VAR);
        register(TEST_SET);
        register(TEST_LOOP);
//        register(TEST_LOOP_VECTOR);
        register(TEST_NBT_EQ);
        register(TEST_CALL);
        register(DO_REDSTONE);
        register(DO_DELAY);
        register(DO_STOP);
        register(DO_STOP_OR_RESUME);
        register(DO_SIGNAL);
        register(DO_MESSAGE);
        register(DO_LOG);
        register(DO_FETCHLIQUID);
        register(DO_PUSHLIQUID);
        register(DO_FETCHITEMS);
        register(DO_PUSHITEMS);
        register(DO_PUSHMULTI);
        register(DO_PUSHWORKBENCH);
        register(DO_SETVAR);
        register(DO_SETTOKEN);
        register(DO_ADD);
        register(DO_SUBTRACT);
        register(DO_DIVIDE);
        register(DO_MULTIPLY);
        register(DO_MODULO);
        register(DO_CONCAT);
        register(DO_VECTOR_PUSH);
        register(DO_VECTOR_PUSH_INT);
        register(DO_VECTOR_POP);
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
        register(DO_GFX_BOX_OLD);
        register(DO_GFX_LINE_OLD);
        register(DO_GFX_TEXT_OLD);
        register(DO_GFX_BOX);
        register(DO_GFX_LINE);
        register(DO_GFX_TEXT);
        register(DO_GFX_CLEAR);
    }

    public static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
        if (!opcode.isDeprecated()) {
            SORTED_OPCODES.add(opcode);
        }
    }
}

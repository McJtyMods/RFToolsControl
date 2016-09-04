package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftoolscontrol.logic.registry.OpcodeOutput.*;
import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class Opcodes {

    public static final Opcode DO_REDSTONE_ON = Opcode.builder()
            .id("do_rs_on")
            .description("WIP")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(0, 0)
            .build();
    public static final Opcode DO_REDSTONE_OFF = Opcode.builder()
            .id("do_rs_off")
            .description("WIP")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(1, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(2, 0).build();
    public static final Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .description("Event: execute program", "on redstone pulse on")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .description("Event: execute program", "on redstone pulse off")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(4, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(5, 0).build();

    public static final Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
            .description("Operation: wait for a number", "of ticks")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).build())
            .icon(6, 0)
            .runnable(((processor, program, opcode) -> {
                int ticks = processor.evaluateIntParameter(opcode, program, 0);
                program.setDelay(ticks);
                return true;
            }))
            .build();

    public static final Opcode TEST_COUNTINV = Opcode.builder()
            .id("test_countinv")
            .description("Test: count the amount of items", "in a specific slot or", "of a certain type in", "an external inventory")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("inv").type(PAR_INVENTORY).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("item").type(PAR_ITEM).build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                Inventory inv = processor.evalulateParameter(opcode, program, 0);
                Integer slot = processor.evaluateIntegerParameter(opcode, program, 1);
                ItemStack item = processor.evalulateParameter(opcode, program, 2);
                int cnt = processor.countItem(inv, slot, item);
                program.setLastValue(Parameter.builder().type(PAR_INTEGER).value(ParameterValue.constant(cnt)).build());
                return true;
            }))
            .build();


    public static final Opcode DO_STOP = Opcode.builder()
            .id("do_stop")
            .description("Operation: stop program")
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                program.killMe();
                return true;
            })
            .build();

    public static final Opcode DO_LOG = Opcode.builder()
            .id("do_log")
            .description("Operation: dump a message", "on the processor console")
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
            .description("Event: execute program every", "<N> ticks")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).build())
            .icon(9, 0)
            .build();

    public static final Opcode CALC_GT = Opcode.builder()
            .id("calc_gt")
            .description("Calculation: compare two", "values")
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

    public static final Opcode CALC_EQ = Opcode.builder()
            .id("calc_eq")
            .description("Calculation: compare two", "values on equality")
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
            .description("Operation: fetch items from", "external inventory", "and place in internal", "inventory. Fails if", "amount does not exactly", "match")
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
            .description("Operation: push items to", "external inventory", "from internal inventory", "Fails if amount does", "not exactly match")
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

    public static final Opcode TEST_COUNTINVINT = Opcode.builder()
            .id("test_countinvint")
            .description("Test: count the amount of items", "in a specific slot of", "the internal inventory")
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
            .description("Operation: copy last returned", "value to a variable")
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
            .description("Operation: add two integers")
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
            .description("Operation: subtract two integers")
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
            .description("Operation: divide two integers")
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
            .description("Operation: multiply two integers")
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
            .description("Operation: calculate modula", "of two integers")
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
            .description("Operation: add two integers")
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



    public static final Map<String, Opcode> OPCODES = new HashMap<>();

    public static void init() {
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(EVENT_TIMER);
        register(TEST_COUNTINV);
        register(TEST_COUNTINVINT);
        register(CALC_GT);
        register(CALC_EQ);
//        register(CONTROL_IF);
        register(DO_REDSTONE_ON);
        register(DO_REDSTONE_OFF);
        register(DO_DELAY);
        register(DO_STOP);
        register(DO_LOG);
        register(DO_FETCHITEMS);
        register(DO_PUSHITEMS);
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
    }
}

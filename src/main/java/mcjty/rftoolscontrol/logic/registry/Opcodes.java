package mcjty.rftoolscontrol.logic.registry;

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
                int ticks = processor.evalulateParameter(opcode, program, 0);
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
                Integer slot = processor.evalulateParameter(opcode, program, 1);
                ItemStack item = processor.evalulateParameter(opcode, program, 2);
                int cnt = processor.countItem(inv, slot, item);
                program.setLastValue(PAR_INTEGER, ParameterValue.constant(cnt));
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
                String message = processor.evalulateParameter(opcode, program, 0);
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
                int v1 = processor.evalulateParameter(opcode, program, 0);
                int v2 = processor.evalulateParameter(opcode, program, 1);
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
                int v1 = processor.evalulateParameter(opcode, program, 0);
                int v2 = processor.evalulateParameter(opcode, program, 1);
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
                Integer slot = processor.evalulateParameter(opcode, program, 1);
                ItemStack item = processor.evalulateParameter(opcode, program, 2);
                int amount = processor.evalulateParameter(opcode, program, 3);
                int slotOut = processor.evalulateParameter(opcode, program, 4);
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
                int slot = processor.evalulateParameter(opcode, program, 1);
                int amount = processor.evalulateParameter(opcode, program, 2);
                int slotIn = processor.evalulateParameter(opcode, program, 3);
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
                int slot = processor.evalulateParameter(opcode, program, 0);
                ItemStack stack = processor.getItemInternal(program, slot);
                program.setLastValue(PAR_INTEGER, ParameterValue.constant(stack == null ? 0 : stack.stackSize));
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
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
    }
}

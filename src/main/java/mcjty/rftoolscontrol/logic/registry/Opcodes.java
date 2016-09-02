package mcjty.rftoolscontrol.logic.registry;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftoolscontrol.logic.registry.OpcodeOutput.*;
import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class Opcodes {

    public static final Opcode DO_REDSTONE_ON = Opcode.builder()
            .id("do_rs_on")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(0, 0)
            .build();
    public static final Opcode DO_REDSTONE_OFF = Opcode.builder()
            .id("do_rs_off")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(1, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(2, 0).build();
    public static final Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static final Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .icon(4, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(5, 0).build();

    public static final Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
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
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("invside").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .icon(2, 0)
            .runnable(((processor, program, opcode) -> {
                EnumFacing side = processor.evalulateParameter(opcode, program, 0);
                EnumFacing invside = processor.evalulateParameter(opcode, program, 1);
                int slot = processor.evalulateParameter(opcode, program, 2);
                IItemHandler handler = processor.getItemHandlerAt(side, invside);
                if (handler != null) {
                    ItemStack stackInSlot = handler.getStackInSlot(slot);
                    program.setLastValue(PAR_INTEGER, ParameterValue.constant(stackInSlot == null ? 0 : stackInSlot.stackSize));
                }
                return true;
            }))
            .build();

    public static final Opcode CONTROL_IF = Opcode.builder()
            .id("ctrl_if")
            .opcodeOutput(YESNO)
            .parameter(ParameterDescription.builder().name("eval").type(PAR_BOOLEAN).build())
            .icon(5, 0)
            .runnable(((processor, program, opcode) -> {
                return processor.evalulateBoolParameter(opcode, program, 0);
            }))
            .build();

    public static final Opcode DO_STOP = Opcode.builder()
            .id("do_stop")
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                program.killMe();
                return true;
            })
            .build();

    public static final Opcode DO_LOG = Opcode.builder()
            .id("do_log")
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
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("ticks").type(PAR_INTEGER).build())
            .icon(9, 0)
            .build();

    public static final Opcode CALC_GT = Opcode.builder()
            .id("calc_gt")
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
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("invside").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).build())
            .icon(0, 1)
            .runnable(((processor, program, opcode) -> {
                EnumFacing side = processor.evalulateParameter(opcode, program, 0);
                EnumFacing invside = processor.evalulateParameter(opcode, program, 1);
                int slot = processor.evalulateParameter(opcode, program, 2);
                int amount = processor.evalulateParameter(opcode, program, 3);
                int slotOut = processor.evalulateParameter(opcode, program, 4);
                IItemHandler handler = processor.getItemHandlerAt(side, invside);
                if (handler != null) {
                    ItemStack extracted = handler.extractItem(slot, amount, false);
                    processor.insertStack(program, slotOut, extracted);
//                    program.setLastValue(PAR_INTEGER, ParameterValue.constant(stackInSlot == null ? 0 : stackInSlot.stackSize));
                }
                return true;
            }))
            .build();

    public static final Opcode DO_PUSHITEMS = Opcode.builder()
            .id("do_pushitems")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("invside").type(PAR_SIDE).build())
            .parameter(ParameterDescription.builder().name("slot").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("amount").type(PAR_INTEGER).build())
            .parameter(ParameterDescription.builder().name("slotOut").type(PAR_INTEGER).build())
            .icon(1, 1)
            .runnable(((processor, program, opcode) -> {
                EnumFacing side = processor.evalulateParameter(opcode, program, 0);
                EnumFacing invside = processor.evalulateParameter(opcode, program, 1);
                int slot = processor.evalulateParameter(opcode, program, 2);
                int amount = processor.evalulateParameter(opcode, program, 3);
                int slotOut = processor.evalulateParameter(opcode, program, 4);
                IItemHandler handler = processor.getItemHandlerAt(side, invside);
                if (handler != null) {
                    // @todo
//                    ItemStack extracted = handler.extractItem(slot, amount, false);
//                    processor.insertStack(program, slotOut, extracted);
                }
                return true;
            }))
            .build();


    public static final Map<String, Opcode> OPCODES = new HashMap<>();

    public static void init() {
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(EVENT_TIMER);
        register(TEST_COUNTINV);
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

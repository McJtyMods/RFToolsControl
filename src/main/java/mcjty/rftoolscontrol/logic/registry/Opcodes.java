package mcjty.rftoolscontrol.logic.registry;

import java.util.HashMap;
import java.util.Map;

import static mcjty.rftoolscontrol.logic.registry.OpcodeOutput.NONE;
import static mcjty.rftoolscontrol.logic.registry.OpcodeOutput.SINGLE;

public class Opcodes {

    public static Opcode DO_REDSTONE_ON = Opcode.builder()
            .id("do_rs_on")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(0, 0)
            .build();
    public static Opcode DO_REDSTONE_OFF = Opcode.builder()
            .id("do_rs_off")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(1, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(2, 0).build();
    public static Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .opcodeOutput(SINGLE)
            .isEvent(true)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(4, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(5, 0).build();
    public static Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
            .opcodeOutput(SINGLE)
            .parameter(ParameterDescription.builder().name("ticks").type(ParameterType.PAR_INTEGER).build())
            .icon(6, 0)
            .runnable(((processor, program, opcode) -> {
                int ticks = processor.evalulateParameter(opcode, 0);
                System.out.println("Opcodes.DO_DELAY: ticks = " + ticks);
                program.setDelay(ticks);
                return true;
            }))
            .build();
    public static Opcode DO_STOP = Opcode.builder()
            .id("do_stop")
            .opcodeOutput(NONE)
            .icon(7, 0)
            .runnable((processor, program, opcode) -> {
                System.out.println("Opcodes.DO_STOP");
                program.killMe();
                return true;
            })
            .build();

    public static final Map<String, Opcode> OPCODES = new HashMap<>();

    public static void init() {
        register(DO_REDSTONE_ON);
        register(DO_REDSTONE_OFF);
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(DO_DELAY);
        register(DO_STOP);
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
    }
}

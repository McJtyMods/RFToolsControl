package mcjty.rftoolscontrol.logic.registry;

import java.util.HashMap;
import java.util.Map;

public class Opcodes {

    public static Opcode DO_REDSTONE_ON = Opcode.builder()
            .id("do_rs_on")
            .opcodeOutput(OpcodeOutput.SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(0, 0)
            .build();
    public static Opcode DO_REDSTONE_OFF = Opcode.builder()
            .id("do_rs_off")
            .opcodeOutput(OpcodeOutput.SINGLE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(1, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(2, 0).build();
    public static Opcode EVENT_REDSTONE_ON = Opcode.builder()
            .id("ev_rs_on")
            .opcodeOutput(OpcodeOutput.SINGLE)
            .opcodeInput(OpcodeInput.NONE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static Opcode EVENT_REDSTONE_OFF = Opcode.builder()
            .id("ev_rs_off")
            .opcodeOutput(OpcodeOutput.SINGLE)
            .opcodeInput(OpcodeInput.NONE)
            .parameter(ParameterDescription.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(4, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(5, 0).build();
    public static Opcode DO_DELAY = Opcode.builder()
            .id("do_delay")
            .opcodeOutput(OpcodeOutput.SINGLE)
            .parameter(ParameterDescription.builder().name("ticks").type(ParameterType.PAR_INTEGER).build())
            .icon(6, 0)
            .build();

    public static final Map<String, Opcode> OPCODES = new HashMap<>();

    public static void init() {
        register(DO_REDSTONE_ON);
        register(DO_REDSTONE_OFF);
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(DO_DELAY);
    }

    private static void register(Opcode opcode) {
        OPCODES.put(opcode.getId(), opcode);
    }
}

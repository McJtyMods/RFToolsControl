package mcjty.rftoolscontrol.logic.program;

import java.util.HashMap;
import java.util.Map;

public class Operands {

    public static Operand DO_REDSTONE_ON = Operand.builder()
            .id("do_rs_on")
            .operandOutput(OperandOutput.SINGLE)
            .parameter(Parameter.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(0, 0)
            .build();
    public static Operand DO_REDSTONE_OFF = Operand.builder()
            .id("do_rs_off")
            .operandOutput(OperandOutput.SINGLE)
            .parameter(Parameter.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(1, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(2, 0).build();
    public static Operand EVENT_REDSTONE_ON = Operand.builder()
            .id("ev_rs_on")
            .operandOutput(OperandOutput.SINGLE)
            .parameter(Parameter.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(3, 0)
            .build();
    public static Operand EVENT_REDSTONE_OFF = Operand.builder()
            .id("ev_rs_off")
            .operandOutput(OperandOutput.SINGLE)
            .parameter(Parameter.builder().name("side").type(ParameterType.PAR_SIDE).build())
            .icon(4, 0)
            .build();
    //    public static Operand DO_REDSTONE_OFF = Operand.builder().id("do_rs_off").operandOutput(OperandOutput.SINGLE).icon(5, 0).build();
    public static Operand DO_DELAY = Operand.builder()
            .id("do_delay")
            .operandOutput(OperandOutput.SINGLE)
            .parameter(Parameter.builder().name("ticks").type(ParameterType.PAR_INTEGER).build())
            .icon(6, 0)
            .build();

    public static final Map<String, Operand> OPERANDS = new HashMap<>();

    public static void init() {
        register(DO_REDSTONE_ON);
        register(DO_REDSTONE_OFF);
        register(EVENT_REDSTONE_ON);
        register(EVENT_REDSTONE_OFF);
        register(DO_DELAY);
    }

    private static void register(Operand operand) {
        OPERANDS.put(operand.getId(), operand);
    }
}

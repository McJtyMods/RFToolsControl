package mcjty.rftoolscontrol.logic.registry;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class Functions {

    public static final Function LASTINT = Function.builder()
            .id("last_int")
            .type(PAR_INTEGER)
            .runnable((processor, program, function) -> {
                return convertToInt(program.getLastValueType(), program.getLastValue());
            })
            .build();
    public static final Function LASTSTRING = Function.builder()
            .id("last_str")
            .type(PAR_STRING)
            .runnable((processor, program, function) -> {
                return convertToString(program.getLastValueType(), program.getLastValue());
            })
            .build();

    private static ParameterValue convertToInt(ParameterType type, ParameterValue value) {
        if (value == null || type == null) {
            return ParameterValue.constant(0);
        }
        if (value.getValue() == null) {
            return ParameterValue.constant(0);
        }
        switch (type) {
            case PAR_STRING:
                return ParameterValue.constant(Integer.parseInt((String) value.getValue()));
            case PAR_INTEGER:
                return value;
            case PAR_FLOAT:
                return ParameterValue.constant(((Float) value.getValue()).intValue());
            case PAR_SIDE:
                return ParameterValue.constant(0);
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) value.getValue()) ? 1 : 0);
        }
        return ParameterValue.constant(0);
    }

    private static ParameterValue convertToString(ParameterType type, ParameterValue value) {
        if (value == null || type == null) {
            return ParameterValue.constant("");
        }
        if (value.getValue() == null) {
            return ParameterValue.constant("");
        }
        switch (type) {
            case PAR_STRING:
                return value;
            case PAR_INTEGER:
                return ParameterValue.constant(Integer.toString((Integer) value.getValue()));
            case PAR_FLOAT:
                return ParameterValue.constant(Float.toString((Float) value.getValue()));
            case PAR_SIDE:
                return ParameterValue.constant(((EnumFacing)value.getValue()).getName());
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) value.getValue()) ? "true" : "false");
        }
        return ParameterValue.constant("");
    }

    public static final Map<String, Function> FUNCTIONS = new HashMap<>();
    private static final Map<ParameterType,List<Function>> FUNCTIONS_BY_TYPE = new HashMap<>();

    public static void init() {
        register(LASTINT);
        register(LASTSTRING);
    }

    private static void register(Function function) {
        FUNCTIONS.put(function.getId(), function);
        ParameterType type = function.getType();
        if (!FUNCTIONS_BY_TYPE.containsKey(type)) {
            FUNCTIONS_BY_TYPE.put(type, new ArrayList<>());
        }
        FUNCTIONS_BY_TYPE.get(type).add(function);
    }

    @Nonnull
    public static List<Function> getFunctionsByType(ParameterType type) {
        if (FUNCTIONS_BY_TYPE.containsKey(type)) {
            return FUNCTIONS_BY_TYPE.get(type);
        } else {
            return Collections.emptyList();
        }
    }
}

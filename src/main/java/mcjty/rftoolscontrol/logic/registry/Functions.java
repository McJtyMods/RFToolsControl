package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftoolscontrol.logic.registry.ParameterType.*;

public class Functions {

    public static final Function LASTBOOL = Function.builder()
            .id("last_bool")
            .name("last")
            .type(PAR_BOOLEAN)
            .runnable((processor, program, function) -> {
                return convertToBool(program.getLastValue());
            })
            .build();
    public static final Function LASTINT = Function.builder()
            .id("last_int")
            .name("last")
            .type(PAR_INTEGER)
            .runnable((processor, program, function) -> {
                return convertToInt(program.getLastValue());
            })
            .build();
    public static final Function LASTSTRING = Function.builder()
            .id("last_str")
            .name("last")
            .type(PAR_STRING)
            .runnable((processor, program, function) -> {
                return convertToString(program.getLastValue());
            })
            .build();
    public static final Function CRAFTID = Function.builder()
            .id("craftid")
            .name("craftid")
            .type(PAR_STRING)
            .runnable((processor, program, function) -> {
                return ParameterValue.constant(program.getCraftTicket());
            })
            .build();
    public static final Function CRAFTRESULT = Function.builder()
            .id("craftresult")
            .name("craftresult")
            .type(PAR_ITEM)
            .runnable((processor, program, function) -> {
                return ParameterValue.constant(processor.getCraftResult(program));
            })
            .build();

    private static ParameterValue convertToBool(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(false);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(false);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return ParameterValue.constant(!((String) v).isEmpty());
            case PAR_INTEGER:
                return ParameterValue.constant(((Integer) v) != 0);
            case PAR_FLOAT:
                return ParameterValue.constant(((Float) v) != 0);
            case PAR_SIDE:
                return ParameterValue.constant(false);
            case PAR_BOOLEAN:
                return value.getParameterValue();
        }
        return ParameterValue.constant(false);
    }

    private static ParameterValue convertToInt(Parameter value) {
        if (value == null) {
            return ParameterValue.constant(0);
        }
        if (!value.isSet()) {
            return ParameterValue.constant(0);
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return ParameterValue.constant(Integer.parseInt((String) v));
            case PAR_INTEGER:
                return value.getParameterValue();
            case PAR_FLOAT:
                return ParameterValue.constant(((Float) v).intValue());
            case PAR_SIDE:
                return ParameterValue.constant(0);
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) v) ? 1 : 0);
            case PAR_ITEM:
                return ParameterValue.constant(((ItemStack) v).stackSize);
        }
        return ParameterValue.constant(0);
    }

    private static ParameterValue convertToString(Parameter value) {
        if (value == null) {
            return ParameterValue.constant("");
        }
        if (!value.isSet()) {
            return ParameterValue.constant("");
        }
        Object v = value.getParameterValue().getValue();
        switch (value.getParameterType()) {
            case PAR_STRING:
                return value.getParameterValue();
            case PAR_INTEGER:
                return ParameterValue.constant(Integer.toString((Integer) v));
            case PAR_FLOAT:
                return ParameterValue.constant(Float.toString((Float) v));
            case PAR_SIDE:
                return ParameterValue.constant(v.toString());
            case PAR_BOOLEAN:
                return ParameterValue.constant(((Boolean) v) ? "true" : "false");
            case PAR_ITEM:
                return ParameterValue.constant(((ItemStack) v).getDisplayName());
        }
        return ParameterValue.constant("");
    }

    public static final Map<String, Function> FUNCTIONS = new HashMap<>();
    private static final Map<ParameterType,List<Function>> FUNCTIONS_BY_TYPE = new HashMap<>();

    public static void init() {
        register(LASTBOOL);
        register(LASTINT);
        register(LASTSTRING);
        register(CRAFTID);
        register(CRAFTRESULT);
    }

    private static void register(Function function) {
        FUNCTIONS.put(function.getId(), function);
        ParameterType type = function.getReturnType();
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

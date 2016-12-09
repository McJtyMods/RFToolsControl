package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.api.code.Function;
import mcjty.rftoolscontrol.api.parameters.Parameter;
import mcjty.rftoolscontrol.api.parameters.ParameterType;
import mcjty.rftoolscontrol.api.parameters.Tuple;
import mcjty.rftoolscontrol.blocks.processor.ProcessorTileEntity;
import mcjty.rftoolscontrol.logic.ParameterTools;
import mcjty.rftoolscontrol.logic.TypeConverters;
import mcjty.rftoolscontrol.logic.running.ProgException;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftoolscontrol.api.parameters.ParameterType.*;
import static mcjty.rftoolscontrol.logic.running.ExceptionType.EXCEPT_NOTAVECTOR;

public class Functions {

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    public static final Function LASTBOOL = Function.builder()
            .id("last_bool")
            .name("last")
            .description("The last opcode result", "converted to a boolean")
            .type(PAR_BOOLEAN)
            .runnable((processor, program) -> TypeConverters.convertToBool(program.getLastValue()))
            .build();
    public static final Function LASTINT = Function.builder()
            .id("last_int")
            .name("last")
            .description("The last opcode result", "converted to an integer")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> TypeConverters.convertToInt(program.getLastValue()))
            .build();
    public static final Function LASTSTRING = Function.builder()
            .id("last_str")
            .name("last")
            .description("The last opcode result", "converted to a string")
            .type(PAR_STRING)
            .runnable((processor, program) -> TypeConverters.convertToString(program.getLastValue()))
            .build();
    public static final Function LASTITEM = Function.builder()
            .id("last_item")
            .name("last")
            .description("The last opcode result as an item", "Can also convert a string representing", "a registry name to an item",
                    "or a fluid to the corresponding bucket")
            .type(PAR_ITEM)
            .runnable((processor, program) -> TypeConverters.convertToItem(program.getLastValue()))
            .build();
    public static final Function LASTFLUID = Function.builder()
            .id("last_fluid")
            .name("last")
            .description("The last opcode result as an fluid", "Can also convert a string representing", "a registry name to an fluid",
                    "or an item containing a fluid")
            .type(PAR_FLUID)
            .runnable((processor, program) -> TypeConverters.convertToFluid(program.getLastValue()))
            .build();
    public static final Function LASTINV = Function.builder()
            .id("last_inv")
            .name("last")
            .description("The last opcode result as an inventory (position)", "Can also convert a string with format", "'<name> S/S' to a position")
            .type(PAR_INVENTORY)
            .runnable((processor, program) -> TypeConverters.convertToInventory(program.getLastValue()))
            .build();
    public static final Function LASTSIDE = Function.builder()
            .id("last_side")
            .name("last")
            .description("The last opcode result as a side (position)", "Can also convert a string with format", "'<name> S' to a side")
            .type(PAR_SIDE)
            .runnable((processor, program) -> TypeConverters.convertToSide(program.getLastValue()))
            .build();
    public static final Function LASTTUPLE = Function.builder()
            .id("last_tuple")
            .name("last")
            .description("The last opcode result as a tuple", "Can also convert a string with format", "'x,y' to a tuple")
            .type(PAR_TUPLE)
            .runnable((processor, program) -> TypeConverters.convertToTuple(program.getLastValue()))
            .build();
    public static final Function LASTVECTOR = Function.builder()
            .id("last_vector")
            .name("last")
            .description("The last opcode result as a vector")
            .type(PAR_VECTOR)
            .runnable((processor, program) -> TypeConverters.convertToVector(program.getLastValue()))
            .build();
    public static final Function MAXVECTOR = Function.builder()
            .id("max_vector")
            .name("maximum")
            .description("The index of the biggest item in a vector")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                List<Parameter> vector = TypeConverters.convertToVector(program.getLastValue());
                if (vector == null) {
                    throw new ProgException(EXCEPT_NOTAVECTOR);
                }
                return ParameterTools.getMaxidxVector(vector);
            })
            .build();
    public static final Function MINVECTOR = Function.builder()
            .id("min_vector")
            .name("minimum")
            .description("The index of the smallest item in a vector")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                List<Parameter> vector = TypeConverters.convertToVector(program.getLastValue());
                if (vector == null) {
                    throw new ProgException(EXCEPT_NOTAVECTOR);
                }
                return ParameterTools.getMaxidxVector(vector);
            })
            .build();
    public static final Function SUMVECTOR = Function.builder()
            .id("sum_vector")
            .name("sum")
            .description("Calculate the sum of all integers in a vector")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                List<Parameter> vector = TypeConverters.convertToVector(program.getLastValue());
                if (vector == null) {
                    throw new ProgException(EXCEPT_NOTAVECTOR);
                }
                return ParameterTools.getSumVector(vector);
            })
            .build();
    public static final Function TICKET = Function.builder()
            .id("ticket")
            .name("ticket")
            .description("The current crafting ticket")
            .type(PAR_STRING)
            .runnable((processor, program) -> program.getCraftTicket())
            .build();
    public static final Function CRAFTRESULT = Function.builder()
            .id("craftresult")
            .name("craftresult")
            .description("The current desired crafting result")
            .type(PAR_ITEM)
            .runnable((processor, program) -> processor.getCraftResult(program))
            .build();
    public static final Function ITEMFROMCARD = Function.builder()
            .id("itemfromcard")
            .name("itemfromcard")
            .description("If the last value is a crafting card then",
                    "this will return the output of that card",
                    "If the last result is a token then this will return",
                    "the value in that token converted to an item (if",
                    "possible)")
            .type(PAR_ITEM)
            .runnable((processor, program) -> ((ProcessorTileEntity)processor).getItemFromCard(program))
            .build();
    public static final Function RANDOMINT = Function.builder()
            .id("random_int")
            .name("random")
            .description("A random integer between 0", "and the last opcode result (exclusive)", "(converted to integer)")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                int v = TypeConverters.convertToInt(program.getLastValue());
                return RANDOM.nextInt(v);
            })
            .build();
    public static final Function TUPLE_X = Function.builder()
            .id("tuple_x")
            .name("tuple_x")
            .description("Get the X component out tuple")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                Tuple v = TypeConverters.convertToTuple(program.getLastValue());
                if (v == null) {
                    return 0;
                }
                return v.getX();
            })
            .build();
    public static final Function TUPLE_Y = Function.builder()
            .id("tuple_y")
            .name("tuple_y")
            .description("Get the Y component out tuple")
            .type(PAR_INTEGER)
            .runnable((processor, program) -> {
                Tuple v = TypeConverters.convertToTuple(program.getLastValue());
                if (v == null) {
                    return 0;
                }
                return v.getY();
            })
            .build();
    public static final Function RANDOMFLOAT = Function.builder()
            .id("random_float")
            .name("random")
            .description("A random floating number between 0", "and the last opcode result (exclusive)", "(converted to float)")
            .type(PAR_FLOAT)
            .runnable((processor, program) -> {
                float v = TypeConverters.convertToFloat(program.getLastValue());
                return RANDOM.nextFloat() * v;
            })
            .build();

    public static final Map<String, Function> FUNCTIONS = new HashMap<>();
    private static final Map<ParameterType,List<Function>> FUNCTIONS_BY_TYPE = new HashMap<>();

    public static void init() {
        register(LASTBOOL);
        register(LASTINT);
        register(LASTSTRING);
        register(LASTITEM);
        register(LASTFLUID);
        register(LASTINV);
        register(LASTSIDE);
        register(LASTTUPLE);
        register(LASTVECTOR);
        register(MINVECTOR);
        register(MAXVECTOR);
        register(SUMVECTOR);
        register(TICKET);
        register(CRAFTRESULT);
        register(ITEMFROMCARD);
        register(RANDOMINT);
        register(RANDOMFLOAT);
        register(TUPLE_X);
        register(TUPLE_Y);
    }

    public static void register(Function function) {
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

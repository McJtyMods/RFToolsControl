package mcjty.rftoolscontrol.setup;


import mcjty.lib.modules.Modules;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fml.ModLoadingContext;
import net.neoforged.neoforge.fml.config.ModConfig;

public class Config {

    public static final String CATEGORY_GENERAL = "general";

    public static ModConfigSpec.IntValue processorMaxenergy;
    public static ModConfigSpec.IntValue processorReceivepertick;

    public static ModConfigSpec.IntValue processorMaxloglines;

    public static ModConfigSpec.IntValue coreSpeed[] = new ModConfigSpec.IntValue[3];
    public static ModConfigSpec.IntValue coreRFPerTick[] = new ModConfigSpec.IntValue[3];

    public static ModConfigSpec.IntValue VARIABLEMODULE_RFPERTICK;
    public static ModConfigSpec.IntValue INTERACTMODULE_RFPERTICK;
    public static ModConfigSpec.IntValue CONSOLEMODULE_RFPERTICK;
    public static ModConfigSpec.IntValue VECTORARTMODULE_RFPERTICK;

    public static ModConfigSpec.BooleanValue doubleClickToChangeConnector;
    public static ModConfigSpec.IntValue tooltipVerbosityLevel;

    public static ModConfigSpec.IntValue maxGraphicsOpcodes;
    public static ModConfigSpec.IntValue maxEventQueueSize;
    public static ModConfigSpec.IntValue maxCraftRequests;
    public static ModConfigSpec.IntValue maxStackSize;

    public static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static void register(IEventBus bus, Modules modules) {
        setupGeneralConfig();
        modules.initConfig(bus);

        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }

    private static void setupGeneralConfig() {
        SERVER_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        CLIENT_BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        processorMaxenergy = SERVER_BUILDER
                .comment("Maximum RF storage that the processor can hold")
                .defineInRange("processorMaxRF", 100000, 1, Integer.MAX_VALUE);
        processorReceivepertick = SERVER_BUILDER
                .comment("RF per tick that the processor can receive")
                .defineInRange("processorRFPerTick", 1000, 1, Integer.MAX_VALUE);
        processorMaxloglines = SERVER_BUILDER
                .comment("Maximum number of lines to keep in the log")
                .defineInRange("processorMaxLogLines", 100, 0, 100000);
        maxStackSize = SERVER_BUILDER
                .comment("Maximum stack size for a program (used by 'call' opcode)")
                .defineInRange("maxStackSize", 100, 1, 10000);
        maxGraphicsOpcodes = SERVER_BUILDER
                .comment("Maximum amount of graphics opcodes that a graphics card supports")
                .defineInRange("maxGraphicsOpcodes", 30, 1, 10000);
        maxEventQueueSize = SERVER_BUILDER
                .comment("Maximum amount of event queue entries supported by a processor. More events will be ignored")
                .defineInRange("maxEventQueueSize", 100, 1, 10000);
        maxCraftRequests = SERVER_BUILDER
                .comment("Maximum amount of craft requests supported by the crafting station. More requests will be ignored")
                .defineInRange("maxCraftRequests", 200, 1, 10000);

        doubleClickToChangeConnector = SERVER_BUILDER
                .comment("If true double click is needed in programmer to change connector. If false single click is sufficient")
                .define("doubleClickToChangeConnector", true);
        tooltipVerbosityLevel = SERVER_BUILDER
                .comment("If 2 tooltips in the programmer gui are verbose and give a lot of info. With 1 the information is decreased. 0 means no tooltips")
                .defineInRange("tooltipVerbosityLevel", 2, 0, 2);

        coreSpeed[0] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core B500")
                .defineInRange("speedB500", 1, 1, 1000);
        coreSpeed[1] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core S1000")
                .defineInRange("speedS1000", 4, 1, 1000);
        coreSpeed[2] = SERVER_BUILDER
                .comment("Amount of instructions per tick for the CPU Core EX2000")
                .defineInRange("speedEX2000", 16, 1, 1000);
        coreRFPerTick[0] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core B500")
                .defineInRange("rfB500", 4, 0, Integer.MAX_VALUE);
        coreRFPerTick[1] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core S1000")
                .defineInRange("rfS1000", 14, 0, Integer.MAX_VALUE);
        coreRFPerTick[2] = SERVER_BUILDER
                .comment("RF per tick for the CPU Core EX2000")
                .defineInRange("rfEX2000", 50, 0, Integer.MAX_VALUE);

        VARIABLEMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the variable screen module")
                .defineInRange("variableModuleRFPerTick", 1, 0, Integer.MAX_VALUE);
        INTERACTMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the interaction screen module")
                .defineInRange("interactionModuleRFPerTick", 2, 0, Integer.MAX_VALUE);
        CONSOLEMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the console screen module")
                .defineInRange("consoleModuleRFPerTick", 2, 0, Integer.MAX_VALUE);
        VECTORARTMODULE_RFPERTICK = SERVER_BUILDER
                .comment("RF per tick/per block for the vector art screen module")
                .defineInRange("vectorArtModuleRFPerTick", 2, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;
}

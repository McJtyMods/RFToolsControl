package mcjty.rftoolscontrol.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static int processorMaxenergy = 100000;
    public static int processorReceivepertick = 1000;

    public static int processorMaxloglines = 100;

    public static int coreSpeed[] = new int[] { 1, 4, 16 };
    public static int coreRFPerTick[] = new int[] { 4, 14, 50 };

    public static int VARIABLEMODULE_RFPERTICK = 1;
    public static int INTERACTMODULE_RFPERTICK = 2;

    public static void init(Configuration cfg) {
        processorMaxenergy = cfg.get(CATEGORY_GENERAL, "processorMaxRF", processorMaxenergy,
                "Maximum RF storage that the processor can hold").getInt();
        processorReceivepertick = cfg.get(CATEGORY_GENERAL, "processorRFPerTick", processorReceivepertick,
                "RF per tick that the processor can receive").getInt();
        processorMaxloglines = cfg.get(CATEGORY_GENERAL, "processorMaxLogLines", processorMaxloglines,
                "Maximum number of lines to keep in the log").getInt();
        coreSpeed[0] = cfg.get(CATEGORY_GENERAL, "speedB500", coreSpeed[0],
                "Amount of instructions per tick for the CPU Core B500").getInt();
        coreSpeed[1] = cfg.get(CATEGORY_GENERAL, "speedS1000", coreSpeed[1],
                "Amount of instructions per tick for the CPU Core S1000").getInt();
        coreSpeed[2] = cfg.get(CATEGORY_GENERAL, "speedEX2000", coreSpeed[2],
                "Amount of instructions per tick for the CPU Core EX2000").getInt();
        coreRFPerTick[0] = cfg.get(CATEGORY_GENERAL, "rfB500", coreRFPerTick[0],
                "RF per tick for the CPU Core B500").getInt();
        coreRFPerTick[1] = cfg.get(CATEGORY_GENERAL, "rfS1000", coreRFPerTick[1],
                "RF per tick for the CPU Core S1000").getInt();
        coreRFPerTick[2] = cfg.get(CATEGORY_GENERAL, "rfEX2000", coreRFPerTick[2],
                "RF per tick for the CPU Core EX2000").getInt();
        VARIABLEMODULE_RFPERTICK = cfg.get(CATEGORY_GENERAL, "variableModuleRFPerTick", VARIABLEMODULE_RFPERTICK,
                "RF per tick/per block for the variable screen module").getInt();
        INTERACTMODULE_RFPERTICK = cfg.get(CATEGORY_GENERAL, "interactionModuleRFPerTick", INTERACTMODULE_RFPERTICK,
                "RF per tick/per block for the interaction screen module").getInt();
    }

}

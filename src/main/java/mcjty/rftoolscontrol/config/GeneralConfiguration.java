package mcjty.rftoolscontrol.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static int processorMaxenergy = 100000;
    public static int processorReceivepertick = 1000;

    public static int processorMaxloglines = 100;

	public static void init(Configuration cfg) {
        processorMaxenergy = cfg.get(CATEGORY_GENERAL, "processorMaxRF", processorMaxenergy,
                "Maximum RF storage that the processor can hold").getInt();
        processorReceivepertick = cfg.get(CATEGORY_GENERAL, "processorRFPerTick", processorReceivepertick,
                "RF per tick that the processor can receive").getInt();
        processorMaxloglines = cfg.get(CATEGORY_GENERAL, "processorMaxLogLines", processorMaxloglines,
                "Maximum number of lines to keep in the log").getInt();
    }

}

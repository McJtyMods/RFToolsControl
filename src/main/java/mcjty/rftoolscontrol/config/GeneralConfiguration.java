package mcjty.rftoolscontrol.config;

import net.minecraftforge.common.config.Configuration;

public class GeneralConfiguration {
    public static final String CATEGORY_GENERAL = "general";

    public static int PROCESSOR_MAXENERGY = 100000;
    public static int PROCESSOR_RECEIVEPERTICK = 1000;

	public static void init(Configuration cfg) {
        PROCESSOR_MAXENERGY = cfg.get(CATEGORY_GENERAL, "processorMaxRF", PROCESSOR_MAXENERGY,
                "Maximum RF storage that the processor can hold").getInt();
        PROCESSOR_RECEIVEPERTICK = cfg.get(CATEGORY_GENERAL, "processorRFPerTick", PROCESSOR_RECEIVEPERTICK,
                "RF per tick that the processor can receive").getInt();
    }

}

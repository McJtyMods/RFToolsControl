package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.running.CpuCore;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

public class Commands {

    static void executeCommand(ProcessorTileEntity processor, String cmd) {
        processor.markDirty();
        String[] splitted = StringUtils.split(cmd, ' ');
        if (splitted.length == 0) {
            return;
        }
        cmd = splitted[0].toLowerCase();
        System.out.println("cmd = " + cmd);
        if ("clear".equals(cmd)) {
            processor.clearLog();
        } else if ("stop".equals(cmd)) {
            int n = 0;
            for (CpuCore core : processor.getCpuCores()) {
                if (core.hasProgram()) {
                    n++;
                    core.stopProgram();
                }
            }
            processor.log("Stopped " + n + " programs!");
        } else if ("list".equals(cmd)) {
            processor.listStatus();
        } else if ("net".equals(cmd)) {
            handleNetworkCommand(processor, splitted);
        } else if ("reset".equals(cmd)) {
            processor.log(TextFormatting.YELLOW + "Reset the processor!");
            processor.reset();
        } else if ("help".equals(cmd)) {
            processor.log("Commands: clear/stop/list/net/help/reset");
        } else {
            processor.log("Unknown command!");
        }
    }

    private static void handleNetworkCommand(ProcessorTileEntity processor, String[] splitted) {
        System.out.println("splitted = " + splitted);
        if (processor.hasNetworkCard()) {
            if (splitted.length < 1) {
                processor.log("Use: net setup/net/list scan");
            } else {
                String sub = splitted[1].toLowerCase();
                if ("setup".equals(sub)) {
                    if (splitted.length > 2) {
                        StringBuilder name = new StringBuilder(splitted[2]);
                        for (int i = 3 ; i < splitted.length ; i++) {
                            name.append(' ');
                            name.append(splitted[i]);
                        }
                        processor.setupNetwork(name.toString());
                    } else {
                        processor.log("Missing channel name!");
                    }
                } else if ("scan".equals(sub)) {
                    processor.scanNodes();
                } else if ("list".equals(sub)) {
                    processor.listNodes();
                } else if ("info".equals(sub)) {
                    processor.showNetworkInfo();
                } else {
                    processor.log("Unknown 'net' command!");
                }
            }
        } else {
            processor.log("No network card!");
        }
    }
}

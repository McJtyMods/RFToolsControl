package mcjty.rftoolscontrol.blocks.processor;

import mcjty.rftoolscontrol.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.logic.running.CpuCore;
import mcjty.rftoolscontrol.logic.running.RunningProgram;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
            int n = processor.stopPrograms();
            processor.log(TextFormatting.YELLOW + "Stopped " + n + " programs!");
        } else if ("list".equals(cmd)) {
            processor.listStatus();
        } else if ("net".equals(cmd)) {
            handleNetworkCommand(processor, splitted);
        } else if ("reset".equals(cmd)) {
            processor.log(TextFormatting.YELLOW + "Reset the processor!");
            processor.reset();
        } else if ("help".equals(cmd)) {
            processor.log("Commands: clear/stop/list/net/help");
            processor.log("    reset/db");
        } else if ("db".equals(cmd)) {
            handleDebugCommand(processor, splitted);
        } else {
            processor.log("Unknown command!");
        }
    }

    private static void handleDebugCommand(ProcessorTileEntity processor, String[] splitted) {
        List<CpuCore> cores = processor.getCpuCores();
        String sub = splitted[1].toLowerCase();
        if ("debug".equals(sub)) {
            if (splitted.length > 2) {
                try {
                    int core = Integer.parseInt(splitted[2]);
                    cores.get(core).setDebug(true);
                    processor.log(TextFormatting.YELLOW + "Debug mode for core: " + core);
                } catch (Exception e) {
                    processor.log(TextFormatting.RED + "Bad core number");
                    return;
                }
            } else {
                for (CpuCore core : cores) {
                    core.setDebug(true);
                }
                processor.log(TextFormatting.YELLOW + "Debug mode for all cores");
            }
        } else if ("resume".equals(sub)) {
            if (splitted.length > 2) {
                try {
                    int core = Integer.parseInt(splitted[2]);
                    cores.get(core).setDebug(false);
                    processor.log(TextFormatting.YELLOW + "Resume core: " + core);
                } catch (Exception e) {
                    processor.log(TextFormatting.RED + "Bad core number");
                    return;
                }
            } else {
                for (CpuCore core : cores) {
                    core.setDebug(false);
                }
                processor.log(TextFormatting.YELLOW + "Resume all cores");
            }
        } else if ("info".equals(sub)) {
            for (int i = 0; i < cores.size(); i++) {
                CpuCore core = cores.get(i);
                if (core.isDebug()) {
                    RunningProgram program = core.getProgram();
                    if (program == null) {
                        processor.log("Core " + i + ": " + "not running");
                    } else {
                        showCurrent(processor, i, program);
                    }
                }
            }
        } else if ("step".equals(sub)) {
            int cnt = 0;
            for (CpuCore core : cores) {
                if (core.isDebug()) {
                    cnt++;
                }
            }
            int c = 0;
            if (cnt == 0) {
                processor.log(TextFormatting.RED + "Not debugging");
                return;
            } else if (cnt > 1) {
                if (splitted.length <= 2) {
                    processor.log(TextFormatting.RED + "Missing core number");
                    return;
                }
                try {
                    c = Integer.parseInt(splitted[2]);
                } catch (Exception e) {
                    processor.log(TextFormatting.RED + "Bad core number");
                    return;
                }
            }
            CpuCore core = cores.get(c);
            RunningProgram program = core.getProgram();
            if (program == null) {
                processor.log(TextFormatting.RED + "Core " + c + ": " + "not running");
                return;
            }
            core.step(processor);
            showCurrent(processor, c, program);
        } else {
            processor.log("Unknown 'db' command!");
        }
    }

    private static void showCurrent(ProcessorTileEntity processor, int i, RunningProgram program) {
        CompiledOpcode currentOpcode = program.getCurrentOpcode(processor);
        int x = currentOpcode.getGridX();
        int y = currentOpcode.getGridY();
        String id = currentOpcode.getOpcode().getId();
        processor.log("Core " + i + ": [" + x + "," + y + "] " + id);
    }

    private static void handleNetworkCommand(ProcessorTileEntity processor, String[] splitted) {
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

package mcjty.rftoolscontrol.modules.processor.util;

import mcjty.rftoolsbase.api.control.parameters.IParameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.logic.TypeConverters;
import mcjty.rftoolscontrol.modules.processor.logic.compiled.CompiledOpcode;
import mcjty.rftoolscontrol.modules.processor.logic.running.CpuCore;
import mcjty.rftoolscontrol.modules.processor.logic.running.ExceptionType;
import mcjty.rftoolscontrol.modules.processor.logic.running.RunningProgram;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class Commands {

    public static void executeCommand(ProcessorTileEntity processor, String cmd) {
        try {
            exec(processor, cmd);
        } catch (ArrayIndexOutOfBoundsException e) {
            processor.exception(ExceptionType.EXCEPT_BADCOMMAND, null);
        }
    }

    private static void exec(ProcessorTileEntity processor, String cmd) {
        processor.setChanged();
        String[] splitted = StringUtils.split(cmd, ' ');
        if (splitted.length == 0) {
            return;
        }
        cmd = splitted[0].toLowerCase();
        if ("clear".equals(cmd)) {
            processor.clearLog();
        } else if ("stop".equals(cmd)) {
            int n = processor.stopPrograms();
            processor.log(TextFormatting.YELLOW + "Stopped " + n + " programs!");
        } else if ("list".equals(cmd)) {
            processor.listStatus();
        } else if ("reset".equals(cmd)) {
            processor.log(TextFormatting.YELLOW + "Reset the processor!");
            processor.reset();
        } else if ("signal".equals(cmd)) {
            String signal = splitted[1].toLowerCase();
            int cnt = processor.signal(signal);
            processor.log("Signal was handled " + cnt + " time(s)");
        } else if ("net".equals(cmd)) {
            handleNetworkCommand(processor, splitted);
        } else if ("db".equals(cmd)) {
            handleDebugCommand(processor, splitted);
        } else if ("watch".equals(cmd)) {
            handleWatchCommand(processor, splitted);
        } else {
            processor.log("Commands: clear/stop/reset/list");
            processor.log("    signal <name>");
            processor.log("    net setup/list/info");
            processor.log("    watch set/break/list/clear");
            processor.log("    db debug/s/info/last/resume");
        }
    }

    private static void handleWatchCommand(ProcessorTileEntity processor, String[] splitted) {
        String sub = splitted[1].toLowerCase();
        if ("set".equals(sub)) {
            if (splitted.length <= 2) {
                processor.log(TextFormatting.RED + "Missing variable index!");
                return;
            }
            int index = Integer.parseInt(splitted[2]);
            if (index < 0 || index >= ProcessorTileEntity.MAXVARS) {
                processor.log(TextFormatting.RED + "Wrong variable index!");
                return;
            }
            processor.setWatch(index, false);
        } else if ("list".equals(sub)) {
            boolean hasWatches = false;
            for (int i = 0 ; i < ProcessorTileEntity.MAXVARS ; i++) {
                WatchInfo info = processor.getWatchInfos()[i];
                if (info != null) {
                    processor.log("Watch " + i + (info.isBreakOnChange() ? " (break)" : ""));
                    hasWatches = true;
                }
            }
            if (!hasWatches) {
                processor.log("No watches");
            }
        } else if ("break".equals(sub)) {
            if (splitted.length <= 2) {
                processor.log(TextFormatting.RED + "Missing variable index!");
                return;
            }
            int index = Integer.parseInt(splitted[2]);
            if (index < 0 || index >= ProcessorTileEntity.MAXVARS) {
                processor.log(TextFormatting.RED + "Wrong variable index!");
                return;
            }
            processor.setWatch(index, true);
        } else if ("clear".equals(sub)) {
            if (splitted.length <= 2) {
                processor.log(TextFormatting.RED + "Missing variable index!");
                return;
            }
            int index = Integer.parseInt(splitted[2]);
            if (index < 0 || index >= ProcessorTileEntity.MAXVARS) {
                processor.log(TextFormatting.RED + "Wrong variable index!");
                return;
            }
            processor.clearWatch(index);
        } else {
            processor.log("Unknown 'watch' command!");
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
        } else if ("last".equals(sub)) {
            if (splitted.length > 2) {
                try {
                    int i = Integer.parseInt(splitted[2]);
                    CpuCore core = cores.get(i);
                    if (core.hasProgram()) {
                        IParameter value = core.getProgram().getLastValue();
                        if (value == null || value.getParameterValue() == null) {
                            processor.log(TextFormatting.YELLOW + "Last value not set");
                        } else {
                            ParameterType type = value.getParameterType();
                            processor.log(TextFormatting.YELLOW + "Last " + type.getName() + ": " + TypeConverters.convertToString(value));
                        }
                    } else {
                        processor.log(TextFormatting.YELLOW + "No program!");
                    }
                } catch (Exception e) {
                    processor.log(TextFormatting.RED + "Bad core number");
                    return;
                }
            } else {
                int i = 0;
                for (CpuCore core : cores) {
                    if (core.hasProgram()) {
                        IParameter value = core.getProgram().getLastValue();
                        if (value == null || value.getParameterValue() == null) {
                            processor.log(TextFormatting.YELLOW + "" + i + ": Last value not set");
                        } else {
                            ParameterType type = value.getParameterType();
                            processor.log(TextFormatting.YELLOW + "" + i + ": Last " + type.getName() + ": " + TypeConverters.convertToString(value));
                        }
                    }
                    i++;
                }
            }
        } else if ("step".equals(sub) || "s".equals(sub)) {
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
            core.step(processor, core);
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
        if (program.getLock() != null) {
            processor.log(TextFormatting.YELLOW + "[LOCKED on " + program.getLock() + "]!");
        }
    }

    private static void handleNetworkCommand(ProcessorTileEntity processor, String[] splitted) {
        if (processor.hasNetworkCard()) {
            if (splitted.length < 1) {
                processor.log("Use: net setup/list/info");
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

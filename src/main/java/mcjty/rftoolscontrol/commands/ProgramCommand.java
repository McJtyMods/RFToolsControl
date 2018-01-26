package mcjty.rftoolscontrol.commands;

import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.network.PacketItemNBTToServer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Client side command
 */
public class ProgramCommand extends CommandBase {
    public static boolean canUseCommand(ICommandSender sender, int permLevel, String name) {
        return sender.canUseCommand(permLevel, name);
    }

    public static String getCommandName(ICommand command) {
        return command.getName();
    }

    @Override
    public String getName() {
        return "rfctrl";
    }  

    @Override
    public String getUsage(ICommandSender sender) {
        return "rfctrl save | load";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            ItemStack item = Minecraft.getMinecraft().player.getHeldItemMainhand();
            if (item.isEmpty() || !(item.getItem() instanceof ProgramCardItem)) {
                ITextComponent component = new TextComponentString(TextFormatting.RED + "You need a program card in your hand!");
                if (sender instanceof EntityPlayer) {
                    ((EntityPlayer) sender).sendStatusMessage(component, false);
                } else {
                    sender.sendMessage(component);
                }
                return;
            }
            if ("save".equals(args[0])) {
                saveProgram(sender, args[1], item);
            } else if ("load".equals(args[0])) {
                loadProgram(sender, args[1], item);
            }
        } else {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Missing parameter (save <file> or load <file>)!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
        }
    }

    private void loadProgram(ICommandSender sender, String arg, ItemStack item) {
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        String json;
        try(FileInputStream stream = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            stream.read(data);
            json = new String(data, "UTF-8");
        } catch (IOException e) {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Error opening file for reading!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }
        ProgramCardInstance program = ProgramCardInstance.readFromJson(json);
        program.writeToNBT(item);
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketItemNBTToServer(item.getTagCompound()));
        ITextComponent component = new TextComponentString("Loaded program!");
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }

    private void saveProgram(ICommandSender sender, String arg, ItemStack item) {
        ProgramCardInstance program = ProgramCardInstance.parseInstance(item);
        String json = program.writeToJson();
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        if (file.exists()) {
            file.delete();
        }
        try(PrintWriter writer = new PrintWriter(file)) {
            writer.print(json);
        } catch (FileNotFoundException e) {
            ITextComponent component = new TextComponentString(TextFormatting.RED + "Error opening file for writing!");
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).sendStatusMessage(component, false);
            } else {
                sender.sendMessage(component);
            }
            return;
        }
        ITextComponent component = new TextComponentString("Saved program!");
        if (sender instanceof EntityPlayer) {
            ((EntityPlayer) sender).sendStatusMessage(component, false);
        } else {
            sender.sendMessage(component);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length > 0) {
            return getListOfStringsMatchingLastWord(args, "save", "load");
        }
        return Collections.emptyList();
    }
}

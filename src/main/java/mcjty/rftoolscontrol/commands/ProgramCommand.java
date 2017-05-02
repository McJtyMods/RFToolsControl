package mcjty.rftoolscontrol.commands;

import mcjty.lib.compat.CompatCommandBase;
import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.tools.MinecraftTools;
import mcjty.rftoolscontrol.items.ProgramCardItem;
import mcjty.rftoolscontrol.logic.grid.ProgramCardInstance;
import mcjty.rftoolscontrol.network.PacketItemNBTToServer;
import mcjty.rftoolscontrol.network.RFToolsCtrlMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Client side command
 */
public class ProgramCommand extends CompatCommandBase {
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
            ItemStack item = MinecraftTools.getPlayer(Minecraft.getMinecraft()).getHeldItemMainhand();
            if (ItemStackTools.isEmpty(item) || !(item.getItem() instanceof ProgramCardItem)) {
                ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "You need a program card in your hand!"));
                return;
            }
            if ("save".equals(args[0])) {
                saveProgram(sender, args[1], item);
            } else if ("load".equals(args[0])) {
                loadProgram(sender, args[1], item);
            }
        } else {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Missing parameter (save <file> or load <file>)!"));
        }
    }

    private void loadProgram(ICommandSender sender, String arg, ItemStack item) {
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        FileInputStream stream;
        String json;
        try {
            stream = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            stream.read(data);
            json = new String(data, "UTF-8");
        } catch (IOException e) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Error opening file for reading!"));
            return;
        }
        ProgramCardInstance program = ProgramCardInstance.readFromJson(json);
        program.writeToNBT(item);
        RFToolsCtrlMessages.INSTANCE.sendToServer(new PacketItemNBTToServer(item.getTagCompound()));
        ChatTools.addChatMessage(sender, new TextComponentString("Loaded program!"));
    }

    private void saveProgram(ICommandSender sender, String arg, ItemStack item) {
        ProgramCardInstance program = ProgramCardInstance.parseInstance(item);
        String json = program.writeToJson();
//        File file = new File("." + File.separator + "rftoolscontrol" + File.separator + arg);
        File file = new File(arg);
        if (file.exists()) {
            file.delete();
        }
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            ChatTools.addChatMessage(sender, new TextComponentString(TextFormatting.RED + "Error opening file for writing!"));
            return;
        }
        writer.print(json);
        writer.close();
        ChatTools.addChatMessage(sender, new TextComponentString("Saved program!"));
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

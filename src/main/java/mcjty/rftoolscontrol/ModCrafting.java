package mcjty.rftoolscontrol;

import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ModCrafting {

    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsControlManualItem), "rrr", " b ", "   ", 'r', Items.REDSTONE, 'b', Items.BOOK);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.cardBaseItem),
                "rrr", "ppp", "nnn", 'r', Items.REDSTONE, 'p', "dyeGreen", 'n', Items.GOLD_NUGGET));
        GameRegistry.addRecipe(new ItemStack(ModItems.programCardItem), "pp", "np", 'n', ModItems.cardBaseItem, 'p', Items.PAPER);
        GameRegistry.addRecipe(new ItemStack(ModItems.ramChipItem), "rrr", "pnp", "rrr", 'n', ModItems.cardBaseItem, 'p', Items.PAPER, 'r', Items.REDSTONE);
        GameRegistry.addRecipe(new ItemStack(ModItems.networkCardItem), "rer", "pnp", "rrr", 'n', ModItems.cardBaseItem, 'p', Items.GOLD_NUGGET, 'r', Items.REDSTONE, 'e', Items.ENDER_PEARL);
        GameRegistry.addRecipe(new ItemStack(ModItems.cpuCoreEX2000Item), "rgr", "pnp", "rgr", 'n', ModItems.cardBaseItem, 'p', Items.PAPER, 'r', Items.REDSTONE, 'g', Items.GOLD_NUGGET);
//        GameRegistry.addRecipe(new ItemStack(ModItems.cpuCoreEX2000Item), "rrr", "pnp", "rrr", 'n', ModItems.cardBaseItem, 'p', Items.PAPER, 'r', Items.REDSTONE);

        Block machineFrame = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("rftools", "machine_frame"));

        GameRegistry.addRecipe(new ItemStack(ModBlocks.programmerBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
//        GameRegistry.addRecipe(new ItemStack(ModBlocks.processorBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
//        GameRegistry.addRecipe(new ItemStack(ModBlocks.nodeBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
    }
}

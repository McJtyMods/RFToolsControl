package mcjty.rftoolscontrol;

import mcjty.rftoolscontrol.blocks.ModBlocks;
import mcjty.rftoolscontrol.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModCrafting {

    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModItems.rfToolsControlManualItem), "rrr", " b ", "   ", 'r', Items.REDSTONE, 'b', Items.BOOK);
        GameRegistry.addRecipe(new ItemStack(ModItems.programCardItem), "rrr", "ppp", "nnn", 'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
//        GameRegistry.addRecipe(new ItemStack(ModItems.ramChipItem), "rrr", "ppp", "nnn", 'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
//        GameRegistry.addRecipe(new ItemStack(ModItems.networkCardItem), "rrr", "ppp", "nnn", 'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);
//        GameRegistry.addRecipe(new ItemStack(ModItems.cpu), "rrr", "ppp", "nnn", 'r', Items.REDSTONE, 'p', Items.PAPER, 'n', Items.GOLD_NUGGET);

        Block machineFrame = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("rftools", "machine_frame"));

        GameRegistry.addRecipe(new ItemStack(ModBlocks.programmerBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
//        GameRegistry.addRecipe(new ItemStack(ModBlocks.processorBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
//        GameRegistry.addRecipe(new ItemStack(ModBlocks.nodeBlock), "rqr", "pMp", "rqr", 'M', machineFrame, 'r', Items.REDSTONE, 'q', Items.QUARTZ, 'p', Items.PAPER);
    }
}

package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousSetup;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup;
import mcjty.rftoolscontrol.modules.multitank.MultiTankSetup;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', VariousSetup.MACHINE_FRAME.get());
        add('s', VariousSetup.DIMENSIONALSHARD.get());
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.ADVANCED_NETWORK_CARD.get())
                        .key('M', ProcessorSetup.NETWORK_CARD.get())
                        .addCriterion("network_card", hasItem(ProcessorSetup.NETWORK_CARD.get())),
                "ror", "eMe", "ror");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('n', Tags.Items.DYES_GREEN)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("redstone", hasItem(Items.REDSTONE)),
                "rrr", "nnn", "ggg");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.CONSOLE_MODULE.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('P', Tags.Items.GLASS_PANES)
                        .key('z', Tags.Items.DYES_BLACK)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "PMP", "rir", "PzP");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.CPU_CORE_500.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "rgr", "pMp", "rgr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.CPU_CORE_1000.get())
                        .key('M', ProcessorSetup.CPU_CORE_500.get())
                        .addCriterion("core500", hasItem(ProcessorSetup.CPU_CORE_500.get())),
                "rdr", "eMe", "rdr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.CPU_CORE_2000.get())
                        .key('M', ProcessorSetup.CPU_CORE_1000.get())
                        .addCriterion("core1000", hasItem(ProcessorSetup.CPU_CORE_1000.get())),
                "rsr", "sMs", "rsr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.GRAPHICS_CARD.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .key('g', Tags.Items.DUSTS_GLOWSTONE)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "qqq", "rMr", "qgq");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.INTERACTION_MODULE.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .key('P', Items.STONE_PRESSURE_PLATE)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "PMP", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.NETWORK_CARD.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "ror", "gMg", "rrr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.NETWORK_IDENTIFIER.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('X', Items.REPEATER)
                        .key('C', Items.COMPARATOR)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                " C ", " M ", " X ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.PROGRAM_CARD.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "pp", "Mp");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.RAM_CHIP.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "rrr", "pMp", "rrr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.TOKEN.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "ppp", "pMp", "ppp");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.VARIABLE_MODULE.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                " M ", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.VECTORART_MODULE.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .key('P', Tags.Items.GLASS_PANES)
                        .key('I', Tags.Items.INGOTS_GOLD)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "PMP", "rIr", "PzP");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(CraftingStationSetup.CRAFTING_STATION.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('C', Items.CRAFTING_TABLE)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "rMr", "CFC", "rMr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.NODE.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "ror", "rFr", "rMr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProcessorSetup.PROCESSOR.get())
                        .key('M', mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .addCriterion("cardbase", hasItem(mcjty.rftoolscontrol.modules.various.VariousSetup.CARD_BASE.get())),
                "rqr", "MFM", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(ProgrammerSetup.PROGRAMMER.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .addCriterion("frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "rqr", "pFp", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(MultiTankSetup.MULTITANK.get())
                        .addCriterion("frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                "Fii", "iGG", "iGG");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(mcjty.rftoolscontrol.modules.various.VariousSetup.WORKBENCH.get())
                        .key('C', Items.CRAFTING_TABLE)
                        .key('X', Items.CHEST)
                        .addCriterion("frame", hasItem(VariousSetup.MACHINE_FRAME.get())),
                " C ", " F ", " X ");
    }
}
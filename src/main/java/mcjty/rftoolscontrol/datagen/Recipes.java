package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationModule;
import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import mcjty.rftoolscontrol.modules.processor.ProcessorModule;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', VariousModule.MACHINE_FRAME.get());
        add('s', VariousModule.DIMENSIONALSHARD.get());
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.ADVANCED_NETWORK_CARD.get())
                        .define('M', ProcessorModule.NETWORK_CARD.get())
                        .unlockedBy("network_card", has(ProcessorModule.NETWORK_CARD.get())),
                "ror", "eMe", "ror");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('n', Tags.Items.DYES_GREEN)
                        .define('g', Tags.Items.NUGGETS_GOLD)
                        .unlockedBy("redstone", has(Items.REDSTONE)),
                "rrr", "nnn", "ggg");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.CONSOLE_MODULE.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('P', Tags.Items.GLASS_PANES)
                        .define('z', Tags.Items.DYES_BLACK)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "PMP", "rir", "PzP");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.CPU_CORE_500.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('g', Tags.Items.NUGGETS_GOLD)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "rgr", "pMp", "rgr");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.CPU_CORE_1000.get())
                        .define('M', ProcessorModule.CPU_CORE_500.get())
                        .unlockedBy("core500", has(ProcessorModule.CPU_CORE_500.get())),
                "rdr", "eMe", "rdr");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.CPU_CORE_2000.get())
                        .define('M', ProcessorModule.CPU_CORE_1000.get())
                        .unlockedBy("core1000", has(ProcessorModule.CPU_CORE_1000.get())),
                "rsr", "sMs", "rsr");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.GRAPHICS_CARD.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('q', Tags.Items.GEMS_QUARTZ)
                        .define('g', Tags.Items.DUSTS_GLOWSTONE)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "qqq", "rMr", "qgq");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.INTERACTION_MODULE.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('z', Tags.Items.DYES_BLACK)
                        .define('P', Items.STONE_PRESSURE_PLATE)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "PMP", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.NETWORK_CARD.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('g', Tags.Items.NUGGETS_GOLD)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "ror", "gMg", "rrr");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.NETWORK_IDENTIFIER.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('X', Items.REPEATER)
                        .define('C', Items.COMPARATOR)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                " C ", " M ", " X ");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.PROGRAM_CARD.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "pp", "Mp");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.RAM_CHIP.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "rrr", "pMp", "rrr");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.TOKEN.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "ppp", "pMp", "ppp");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.VARIABLE_MODULE.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('z', Tags.Items.DYES_BLACK)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                " M ", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.VECTORART_MODULE.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('z', Tags.Items.DYES_BLACK)
                        .define('P', Tags.Items.GLASS_PANES)
                        .define('I', Tags.Items.INGOTS_GOLD)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "PMP", "rIr", "PzP");

        build(consumer, ShapedRecipeBuilder.shaped(CraftingStationModule.CRAFTING_STATION.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('C', Items.CRAFTING_TABLE)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "rMr", "CFC", "rMr");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.NODE.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "ror", "rFr", "rMr");
        build(consumer, ShapedRecipeBuilder.shaped(ProcessorModule.PROCESSOR.get())
                        .define('M', mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())
                        .define('q', Tags.Items.GEMS_QUARTZ)
                        .unlockedBy("cardbase", has(mcjty.rftoolscontrol.modules.various.VariousModule.CARD_BASE.get())),
                "rqr", "MFM", "rqr");
        build(consumer, ShapedRecipeBuilder.shaped(ProgrammerModule.PROGRAMMER.get())
                        .define('q', Tags.Items.GEMS_QUARTZ)
                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                "rqr", "pFp", "rqr");
        build(consumer, ShapedRecipeBuilder.shaped(MultiTankModule.MULTITANK.get())
                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                "Fii", "iGG", "iGG");
        build(consumer, ShapedRecipeBuilder.shaped(mcjty.rftoolscontrol.modules.various.VariousModule.WORKBENCH.get())
                        .define('C', Items.CRAFTING_TABLE)
                        .define('X', Items.CHEST)
                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                " C ", " F ", " X ");
    }
}

package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousSetup;
import mcjty.rftoolscontrol.setup.Registration;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
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
        group("rftools");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.ADVANCED_NETWORK_CARD.get())
                        .key('M', Registration.NETWORK_CARD.get())
                        .addCriterion("network_card", InventoryChangeTrigger.Instance.forItems(Registration.NETWORK_CARD.get())),
                "ror", "eMe", "ror");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CARD_BASE.get())
                        .key('n', Tags.Items.DYES_GREEN)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("redstone", InventoryChangeTrigger.Instance.forItems(Items.REDSTONE)),
                "rrr", "nnn", "ggg");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CONSOLE_MODULE.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('P', Tags.Items.GLASS_PANES)
                        .key('z', Tags.Items.DYES_BLACK)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "PMP", "rir", "PzP");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CPU_CORE_500.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "rgr", "pMp", "rgr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CPU_CORE_1000.get())
                        .key('M', Registration.CPU_CORE_500.get())
                        .addCriterion("core500", InventoryChangeTrigger.Instance.forItems(Registration.CPU_CORE_500.get())),
                "rdr", "eMe", "rdr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CPU_CORE_2000.get())
                        .key('M', Registration.CPU_CORE_1000.get())
                        .addCriterion("core1000", InventoryChangeTrigger.Instance.forItems(Registration.CPU_CORE_1000.get())),
                "rsr", "sMs", "rsr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.GRAPHICS_CARD.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .key('g', Tags.Items.DUSTS_GLOWSTONE)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "qqq", "rMr", "qgq");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.INTERACTION_MODULE.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .key('P', Items.STONE_PRESSURE_PLATE)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "PMP", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.NETWORK_CARD.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "ror", "gMg", "rrr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.NETWORK_IDENTIFIER.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('X', Items.REPEATER)
                        .key('C', Items.COMPARATOR)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                " C ", " M ", " X ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.PROGRAM_CARD.get())
                        .key('M', Registration.CARD_BASE.get())
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "pp", "Mp");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.RAM_CHIP.get())
                        .key('M', Registration.CARD_BASE.get())
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "rrr", "pMp", "rrr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.TOKEN.get())
                        .key('M', Registration.CARD_BASE.get())
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "ppp", "pMp", "ppp");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.VARIABLE_MODULE.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                " M ", "rir", " z ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.VECTORART_MODULE.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('z', Tags.Items.DYES_BLACK)
                        .key('P', Tags.Items.GLASS_PANES)
                        .key('g', Tags.Items.DUSTS_GLOWSTONE)
                        .key('I', Tags.Items.INGOTS_GOLD)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "PMP", "rIr", "PzP");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CRAFTING_STATION.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('C', Items.CRAFTING_TABLE)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "rMr", "CFC", "rMr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.NODE.get())
                        .key('M', Registration.CARD_BASE.get())
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "ror", "rFr", "rMr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.PROCESSOR.get())
                        .key('M', Registration.CARD_BASE.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .addCriterion("cardbase", InventoryChangeTrigger.Instance.forItems(Registration.CARD_BASE.get())),
                "rqr", "MFM", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.PROGRAMMER.get())
                        .key('q', Tags.Items.GEMS_QUARTZ)
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
                "rqr", "pFp", "rqr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.MULTITANK.get())
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
                "Fii", "iGG", "iGG");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.WORKBENCH.get())
                        .key('C', Items.CRAFTING_TABLE)
                        .key('X', Items.CHEST)
                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
                " C ", " F ", " X ");
    }
}

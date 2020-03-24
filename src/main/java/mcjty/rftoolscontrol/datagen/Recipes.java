package mcjty.rftoolscontrol.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.modules.various.VariousSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;

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
//        build(consumer, ShapedRecipeBuilder.shapedRecipe(BuilderSetup.BUILDER.get())
//                        .addCriterion("machine_frame", InventoryChangeTrigger.Instance.forItems(VariousSetup.MACHINE_FRAME.get())),
//                "BoB", "rFr", "BrB");
    }
}

package mcjty.rftoolscontrol.datagen;

import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class Languages extends LanguageProvider {

    public Languages(DataGenerator gen, String locale) {
        super(gen, RFToolsControl.MODID, locale);
    }

    @Override
    protected void addTranslations() {

        add("itemGroup.rftoolscontrol", "RFTools Control");

//        add(BuilderSetup.BUILDER.get(), "Builder");
//        add("message.rftoolsbuilder.shiftmessage", "<Press Shift>");
//        add("message.rftoolsbuilder.builder", "@fThis block can quarry areas, pump liquids,\n"
//                + "@fmove/copy/swap structures, collect items\n"
//                + "@fand XP, move entities, build structures, ...\n"
//                + "@eInfusing bonus: reduced power consumption\n"
//                + "@eand increased speed.");
    }
}

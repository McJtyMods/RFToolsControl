package mcjty.rftoolscontrol.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.node.GuiNode;
import mcjty.rftoolscontrol.blocks.processor.GuiProcessor;
import mcjty.rftoolscontrol.blocks.programmer.GuiProgrammer;
import mcjty.rftoolscontrol.blocks.workbench.GuiWorkbench;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        GenericGuiContainer.register(Registration.PROGRAMMER_CONTAINER.get(), GuiProgrammer::new);
        GenericGuiContainer.register(Registration.PROCESSOR_CONTAINER.get(), GuiProcessor::new);
        GenericGuiContainer.register(Registration.WORKBENCH_CONTAINER.get(), GuiWorkbench::new);
        GenericGuiContainer.register(Registration.NODE_CONTAINER.get(), GuiNode::new);
        //        ModelLoaderRegistry.registerLoader(new ResourceLocation(RFToolsControl.MODID, "tankloader"), new TankModelLoader());
    }

//    @SubscribeEvent
//    public static void onTextureStitch(TextureStitchEvent.Pre event) {
//        if (!event.getMap().getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)) {
//            return;
//        }
//        event.addSprite(BEAM_OK);
//        event.addSprite(BEAM_WARN);
//        event.addSprite(BEAM_UNKNOWN);
//    }

//    @SubscribeEvent
//    public static void onModelBake(ModelBakeEvent event) {
//        TankBakedModel model = new TankBakedModel();
//        event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(RFToolsUtility.MODID, "tank"), ""), model);
//    }
}

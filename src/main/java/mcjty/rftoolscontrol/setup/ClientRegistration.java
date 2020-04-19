package mcjty.rftoolscontrol.setup;


import mcjty.lib.McJtyLib;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.Tools;
import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.modules.craftingstation.CraftingStationSetup;
import mcjty.rftoolscontrol.modules.craftingstation.client.GuiCraftingStation;
import mcjty.rftoolscontrol.modules.multitank.MultiTankSetup;
import mcjty.rftoolscontrol.modules.multitank.client.GuiMultiTank;
import mcjty.rftoolscontrol.modules.processor.ProcessorSetup;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorContainer;
import mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity;
import mcjty.rftoolscontrol.modules.processor.client.GuiProcessor;
import mcjty.rftoolscontrol.modules.processor.client.ProcessorRenderer;
import mcjty.rftoolscontrol.modules.programmer.ProgrammerSetup;
import mcjty.rftoolscontrol.modules.programmer.client.GuiProgrammer;
import mcjty.rftoolscontrol.modules.various.VariousSetup;
import mcjty.rftoolscontrol.modules.various.client.GuiNode;
import mcjty.rftoolscontrol.modules.various.client.GuiWorkbench;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RFToolsControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        GenericGuiContainer.register(ProgrammerSetup.PROGRAMMER_CONTAINER.get(), GuiProgrammer::new);
        GenericGuiContainer.register(ProcessorSetup.PROCESSOR_CONTAINER.get(), GuiProcessor::new);
        GenericGuiContainer.register(VariousSetup.WORKBENCH_CONTAINER.get(), GuiWorkbench::new);
        GenericGuiContainer.register(VariousSetup.NODE_CONTAINER.get(), GuiNode::new);
        GenericGuiContainer.register(CraftingStationSetup.CRAFTING_STATION_CONTAINER.get(), GuiCraftingStation::new);
        GenericGuiContainer.register(MultiTankSetup.MULTITANK_CONTAINER.get(), GuiMultiTank::new);

        ScreenManager.IScreenFactory<ProcessorContainer, GuiProcessor> factory = (container, inventory, title) -> {
            TileEntity te = new ProcessorTileEntity();  // Dummy tile
            return Tools.safeMap(te, (mcjty.rftoolscontrol.modules.processor.blocks.ProcessorTileEntity tile) -> new GuiProcessor(tile, container, inventory), "Invalid tile entity!");
        };
        ScreenManager.registerFactory(ProcessorSetup.PROCESSOR_CONTAINER_REMOTE.get(), factory);


        RenderTypeLookup.setRenderLayer(MultiTankSetup.MULTITANK.get(), RenderType.getTranslucent());
        ProcessorRenderer.register();
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

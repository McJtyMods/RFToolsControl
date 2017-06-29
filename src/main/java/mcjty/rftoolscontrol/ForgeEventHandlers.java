package mcjty.rftoolscontrol;

import mcjty.lib.McJtyRegister;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        McJtyRegister.registerBlocks(RFToolsControl.instance, event.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(RFToolsControl.instance, event.getRegistry());
    }
}

package mcjty.rftoolscontrol.modules.multitank.client;

import mcjty.rftoolscontrol.modules.multitank.MultiTankModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(MultiTankModule.MULTITANK.get(), RenderType.translucent());
    }
}

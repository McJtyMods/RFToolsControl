package mcjty.rftoolscontrol.items;

import mcjty.lib.McJtyRegister;
import mcjty.rftoolscontrol.RFToolsControl;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;



public class GenericRFToolsItem extends Item {

    public GenericRFToolsItem(String name) {
        setUnlocalizedName(RFToolsControl.MODID + "." + name);
        setRegistryName(name);
        setCreativeTab(RFToolsControl.setup.getTab());
        McJtyRegister.registerLater(this, RFToolsControl.instance);
    }


    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }


}

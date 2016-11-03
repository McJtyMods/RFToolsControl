package mcjty.rftoolscontrol.mcmpsupport;

import mcjty.rftoolscontrol.RFToolsControl;
import mcjty.rftoolscontrol.blocks.nodemcmp.NodePart;
import mcjty.rftoolscontrol.blocks.nodemcmp.NodePartItem;
import mcmultipart.multipart.MultipartRegistry;

public class MCMPSetup {

    public static void init() {
        NodePartItem itemNode = new NodePartItem();
        MultipartRegistry.registerPart(NodePart.class, RFToolsControl.MODID + ":nodepart");
    }

}

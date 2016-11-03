package mcjty.rftoolscontrol.mcmpsupport;

import mcjty.rftoolscontrol.blocks.nodemcmp.NodePart;
import mcmultipart.multipart.MultipartRegistry;

public class MCMPSetup {

    public static void init() {
        MultipartRegistry.registerPart(NodePart.class, "node");
    }

}

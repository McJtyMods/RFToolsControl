package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.rftoolscontrol.logic.registry.editors.FloatEditor;
import mcjty.rftoolscontrol.logic.registry.editors.IntegerEditor;
import mcjty.rftoolscontrol.logic.registry.editors.SideEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ParameterEditors {

    private static final Map<ParameterType, ParameterEditor> EDITORS = new HashMap<>();

    public static void init() {
        EDITORS.put(ParameterType.PAR_FLOAT, new FloatEditor());
        EDITORS.put(ParameterType.PAR_INTEGER, new IntegerEditor());
        EDITORS.put(ParameterType.PAR_SIDE, new SideEditor());
    }

    public static ParameterEditor getEditor(ParameterType type) {
        return EDITORS.get(type);
    }

}

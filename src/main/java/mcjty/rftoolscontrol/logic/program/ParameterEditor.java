package mcjty.rftoolscontrol.logic.program;

import mcjty.lib.gui.widgets.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.Map;

public interface ParameterEditor {

    void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback,
               Parameter parameter, Map<String, Object> data);
}

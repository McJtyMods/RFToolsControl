package mcjty.rftoolscontrol.logic.registry;

import mcjty.lib.gui.widgets.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.util.Map;

public interface ParameterEditor {

    void build(Minecraft mc, Gui gui, Panel panel, ParameterEditorCallback callback,
               ParameterDescription parameter, Map<String, Object> data);
}

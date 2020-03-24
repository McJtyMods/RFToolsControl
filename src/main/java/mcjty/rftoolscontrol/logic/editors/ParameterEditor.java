package mcjty.rftoolscontrol.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public interface ParameterEditor {

    void build(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback);

    ParameterValue readValue();

    void writeValue(ParameterValue value);

    default int getHeight() { return 20; }

    // Call this to set the editor in 'constant only' mode
    void constantOnly();

    // Set initial focus when this gui is opened
    void initialFocus(Window window);

    // Set an 'on-close' action
    void setOnClose(Runnable onClose);
}

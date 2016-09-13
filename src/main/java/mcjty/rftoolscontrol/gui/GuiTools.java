package mcjty.rftoolscontrol.gui;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.function.Consumer;

public class GuiTools {

    public static void askSomething(Minecraft mc, Gui gui, WindowManager windowManager, int x, int y, String title, String initialValue, Consumer<String> callback) {
        Panel ask = new Panel(mc, gui)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        ask.setBounds(new Rectangle(x, y, 100, 60));
        Window askWindow = windowManager.createModalWindow(ask);
        ask.addChild(new Label(mc, gui).setText(title));
        TextField input = new mcjty.lib.gui.widgets.TextField(mc, gui).addTextEnterEvent(((parent, newText) -> {
            windowManager.closeWindow(askWindow);
            callback.accept(newText);
        }));
        input.setText(initialValue);
        ask.addChild(input);
        Panel buttons = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredWidth(100).setDesiredHeight(18);
        buttons.addChild(new Button(mc, gui).setText("Ok").addButtonEvent((parent -> {
            windowManager.closeWindow(askWindow);
            callback.accept(input.getText());
        })));
        buttons.addChild(new Button(mc, gui).setText("Cancel").addButtonEvent((parent -> {
            windowManager.closeWindow(askWindow);
        })));
        ask.addChild(buttons);
    }

    public static void showMessage(Minecraft mc, Gui gui, WindowManager windowManager, int x, int y, String title) {
        Panel ask = new Panel(mc, gui)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        ask.setBounds(new Rectangle(x, y, 200, 40));
        Window askWindow = windowManager.createModalWindow(ask);
        ask.addChild(new Label(mc, gui).setText(title));
        Panel buttons = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredWidth(100).setDesiredHeight(18);
        buttons.addChild(new Button(mc, gui).setText("Cancel").addButtonEvent((parent -> {
            windowManager.closeWindow(askWindow);
        })));
        ask.addChild(buttons);
    }

}

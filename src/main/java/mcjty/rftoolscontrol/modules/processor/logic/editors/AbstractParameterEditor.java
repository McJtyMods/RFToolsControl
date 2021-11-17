package mcjty.rftoolscontrol.modules.processor.logic.editors;

import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.widgets.*;
import mcjty.rftoolsbase.api.control.code.Function;
import mcjty.rftoolsbase.api.control.parameters.ParameterType;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

import static mcjty.lib.gui.widgets.Widgets.horizontal;
import static mcjty.lib.gui.widgets.Widgets.label;

public abstract class AbstractParameterEditor implements ParameterEditor {

    public static final String PAGE_CONSTANT = "Constant";
    public static final String PAGE_VARIABLE = "Variable";
    public static final String PAGE_FUNCTION = "Function";

    private TextField variableIndex;
    private TabbedPanel tabbedPanel;
    private Panel buttonPanel;
    private ChoiceLabel functionLabel;
    private ToggleButton variableButton;
    private ToggleButton functionButton;

    private Runnable onClose;

    // Parent window is only set if 'initialFocus' is called. So this can be null
    private Window parentWindow;

    @Override
    public void constantOnly() {
        variableButton.enabled(false);
        functionButton.enabled(false);
    }

    @Override
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @Override
    public void initialFocus(Window window) {
        parentWindow = window;
        if (PAGE_CONSTANT.equals(tabbedPanel.getCurrentName())) {
            initialFocusInternal(window);
        } else if (PAGE_VARIABLE.equals(tabbedPanel.getCurrentName())) {
            initialFocusVariable(window);
        }
    }

    protected void closeWindow() {
        if (parentWindow != null) {
            parentWindow.getWindowManager().closeWindow(parentWindow);
        }
        if (onClose != null) {
            onClose.run();
        }
    }

    protected void initialFocusInternal(Window window) {
    }

    private void initialFocusVariable(Window window) {
        window.setTextFocus(variableIndex);
    }

    public static Integer parseIntSafe(String newText) {
        if (newText == null || newText.isEmpty()) {
            return null;
        }
        Integer f;
        try {
            if (newText.startsWith("$")) {
                f = (int) Long.parseLong(newText.substring(1), 16);
            } else {
                f = Integer.parseInt(newText);
            }
        } catch (NumberFormatException e) {
            f = null;
        }
        return f;
    }

    public static Long parseLongSafe(String newText) {
        if (newText == null || newText.isEmpty()) {
            return null;
        }
        Long f;
        try {
            if (newText.startsWith("$")) {
                f = Long.parseLong(newText.substring(1), 16);
            } else {
                f = Long.parseLong(newText);
            }
        } catch (NumberFormatException e) {
            f = null;
        }
        return f;
    }

    public static Float parseFloatSafe(String newText) {
        Float f;
        try {
            f = Float.parseFloat(newText);
        } catch (NumberFormatException e) {
            f = null;
        }
        return f;
    }

    public static Double parseDoubleSafe(String newText) {
        Double f;
        try {
            f = Double.parseDouble(newText);
        } catch (NumberFormatException e) {
            f = null;
        }
        return f;
    }

    protected abstract ParameterValue readConstantValue();

    protected abstract void writeConstantValue(ParameterValue value);

    @Override
    public ParameterValue readValue() {
        if (PAGE_CONSTANT.equals(tabbedPanel.getCurrentName())) {
            return readConstantValue();
        } else if (PAGE_VARIABLE.equals(tabbedPanel.getCurrentName())) {
            Integer var = parseIntSafe(variableIndex.getText());
            if (var != null) {
                return ParameterValue.variable(var);
            } else {
                return ParameterValue.variable(0);
            }
        } else if (PAGE_FUNCTION.equals(tabbedPanel.getCurrentName())) {
            String currentChoice = functionLabel.getCurrentChoice();
            return ParameterValue.function(Functions.FUNCTIONS.get(currentChoice));
        }
        return null;
    }

    @Override
    public void writeValue(ParameterValue value) {
        if (value == null || value.isConstant()) {
            switchPage(PAGE_CONSTANT, null);
            writeConstantValue(value);
        } else if (value.isVariable()) {
            switchPage(PAGE_VARIABLE, null);
            variableIndex.text(Integer.toString(value.getVariableIndex()));
        } else if (value.isFunction()) {
            switchPage(PAGE_FUNCTION, null);
            String id = value.getFunction().getId();
            functionLabel.choice(id);
        }
    }

    protected Panel createLabeledPanel(Minecraft mc, Screen gui, String label, Widget<?> object, String... tooltips) {
        object.tooltips(tooltips);
        return horizontal()
                .children(object, Widgets.label(label)
                        .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .tooltips(tooltips)
                        .desiredWidth(60));
    }

    void createEditorPanel(Minecraft mc, Screen gui, Panel panel, ParameterEditorCallback callback, Panel constantPanel,
                           ParameterType type) {
        Panel variablePanel = horizontal().desiredHeight(18);
        variableIndex = new TextField()
                .desiredHeight(14)
                .tooltips("Index (in the processor)", "of the variable", "(first variable has index 0)")
                .event((newText) -> callback.valueChanged(readValue()));
        variablePanel.children(variableIndex, label("Index:"))
                .tooltips("Index (in the processor)", "of the variable", "(first variable has index 0)")
                .desiredHeight(14);

        Panel functionPanel = horizontal();
        functionLabel = new ChoiceLabel()
                .desiredWidth(120);
        List<Function> functions = Functions.getFunctionsByType(type);
        for (Function function : functions) {
            functionLabel.choices(function.getId());
            functionLabel.choiceTooltip(function.getId(), function.getDescription().toArray(new String[function.getDescription().size()]));
        }
        if (type == ParameterType.PAR_NUMBER) {
            functions = Functions.getFunctionsByType(ParameterType.PAR_INTEGER);
            for (Function function : functions) {
                functionLabel.choices(function.getId());
                functionLabel.choiceTooltip(function.getId(), function.getDescription().toArray(new String[function.getDescription().size()]));
            }
        }

        functionPanel.children(functionLabel);
        functionLabel.event(((newChoice) -> callback.valueChanged(readValue())));

        tabbedPanel = new TabbedPanel()
                .page(PAGE_CONSTANT, constantPanel)
                .page(PAGE_VARIABLE, variablePanel)
                .page(PAGE_FUNCTION, functionPanel);
        tabbedPanel.hint(5, 5 + 18, 190-10, 60 + getHeight() -5-18 -40);


        buttonPanel = horizontal().hint(5, 5, 190-10, 18);
        ToggleButton constantButton = new ToggleButton().text(PAGE_CONSTANT)
                .event(() -> switchPage(PAGE_CONSTANT, callback));
        variableButton = new ToggleButton().text(PAGE_VARIABLE)
                .event(() -> switchPage(PAGE_VARIABLE, callback));
        functionButton = new ToggleButton().text(PAGE_FUNCTION)
                .event(() -> switchPage(PAGE_FUNCTION, callback));
        buttonPanel.children(constantButton, variableButton, functionButton);

        panel.children(buttonPanel, tabbedPanel);
    }

    private void switchPage(String page, ParameterEditorCallback callback) {
        for (int i = 0 ; i < buttonPanel.getChildCount() ; i++) {
            ToggleButton button = buttonPanel.getChild(i);
            if (!page.equals(button.getText())) {
                button.pressed(false);
            } else {
                button.pressed(true);
            }
            tabbedPanel.current(page);
            if (callback != null) {
                callback.valueChanged(readValue());
            }
        }
        if (parentWindow != null) {
            if (PAGE_CONSTANT.equals(page)) {
                initialFocusInternal(parentWindow);
            } else if (PAGE_VARIABLE.equals(page)) {
                initialFocusVariable(parentWindow);
            }
        }
    }
}

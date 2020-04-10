package mcjty.rftoolscontrol.modules.processor.logic.running;

import java.util.HashMap;
import java.util.Map;

public enum ExceptionType {
    EXCEPT_NORF("no_rf", "No RF support"),
    EXCEPT_NOLIQUID("no_liquid", "No liquid support"),
    EXCEPT_NOINTERNALSLOT("no_internal_slot", "Missing internal slot"),
    EXCEPT_MISSINGNODE("missing_node", "Missing node"),
    EXCEPT_MISSINGCRAFTINGCARD("missing_crafting_card", "Missing crafting card"),
    EXCEPT_MISSINGNETWORKCARD("missing_network_card", "Missing network card"),
    EXCEPT_MISSINGSTORAGECARD("missing_storage_card", "Missing storage card"),
    EXCEPT_MISSINGSTORAGE("missing_storage", "Missing or invalid storage"),
    EXCEPT_INVALIDINVENTORY("invalid_inventory", "Invalid inventory"),
    EXCEPT_MISSINGCRAFTTICKET("missing_craft_ticket", "Missing crafting ticket"),
    EXCEPT_MISSINGCRAFTRESULT("missing_craft_result", "Missing craft result"),
    EXCEPT_MISSINGVARIABLE("missing_variable", "Missing variable"),
    EXCEPT_NOTENOUGHVARIABLES("not_enough_variables", "Not enough variables"),
    EXCEPT_INTERNALERROR("internal_error", "Internal error"),
    EXCEPT_BADPARAMETERS("bad_parameters", "Bad parameters"),
    EXCEPT_MISSINGCRAFTINGSTATION("missing_crafting_station", "Missing crafting station"),
    EXCEPT_BADCOMMAND("bad_command", "Bad command"),
    EXCEPT_MISSINGITEM("missing_item", "Missing item"),
    EXCEPT_MISSINGPARAMETER("missing_parameter", "Missing parameter"),
    EXCEPT_NOTATOKEN("not_a_token", "Not a token"),
    EXCEPT_NOTANIDENTIFIER("not_an_identifier", "Not an identifier"),
    EXCEPT_INVALIDDESTINATION("invalid_destination", "Invalid destination"),
    EXCEPT_NEEDSADVANCEDNETWORK("needs_advanced_network", "This needs an advanced network card"),
    EXCEPT_MISSINGGRAPHICSCARD("missing_graphics_card", "Missing graphics card"),
    EXCEPT_TOOMANYGRAPHICS("too_many_graphics_opcodes", "Too many graphics opcodes"),
    EXCEPT_TOOMANYEVENTS("too_many_events", "Too many events"),
    EXCEPT_MISSINGLASTVALUE("missing_last_value", "Missing last value"),
    EXCEPT_NOTANITEM("not_an_item", "This is not an item"),
    EXCEPT_NOINTERNALFLUIDSLOT("no_internal_fluid_slot", "Missing internal fluid slot"),
    EXCEPT_NOTAWORKBENCH("not_a_workbench", "Inventory must be a workbench"),
    EXCEPT_NOTAGRID("not_a_grid", "Crafting does not match a 3x3 grid"),
    EXCEPT_MISSINGSIGNAL("missing_signal", "Signal is missing"),
    EXCEPT_STACKOVERFLOW("stack_overflow", "Stack overflow (recursing too deep?)"),
    EXCEPT_BADINDEX("bad_index", "Bad index for vector access"),
    EXCEPT_NOTAVECTOR("not_a_vector", "Expected a vector. Got something else"),
    EXCEPT_INVALIDMACHINE("invalid_machine", "Machine does not support this"),
    EXCEPT_INVALIDMACHINE_INDEX("invalid_machine_index", "Wrong index for machine data"),
    EXCEPT_UNKNOWN_TAG("unknown_tag", "Unknown tag!"),
    ;

    private final String code;
    private final String description;

    private static final Map<String, ExceptionType> EXCEPTION_MAP = new HashMap<>();

    static {
        for (ExceptionType exception : ExceptionType.values()) {
            EXCEPTION_MAP.put(exception.getCode(), exception);
        }
    }

    ExceptionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static ExceptionType getExceptionForCode(String code) {
        return EXCEPTION_MAP.get(code);
    }
}

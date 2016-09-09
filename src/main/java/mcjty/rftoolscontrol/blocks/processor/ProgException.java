package mcjty.rftoolscontrol.blocks.processor;

import java.util.HashMap;
import java.util.Map;

public enum ProgException {
    EXCEPT_NORF("no_rf", "No RF support"),
    EXCEPT_NOINTERNALSLOT("no_internal_slot", "Missing internal slot"),
    EXCEPT_MISSINGNODE("missing_node", "Missing node"),
    EXCEPT_MISSINGCRAFTINGCARD("missing_crafting_card", "Missing crafting card"),
    EXCEPT_MISSINGNETWORKCARD("missing_network_card", "Missing network card"),
    EXCEPT_MISSINGSTORAGECARD("missing_storage_card", "Missing storage card"),
    EXCEPT_MISSINGSTORAGE("missing_storage", "Missing or invalid storage"),
    EXCEPT_INVALIDINVENTORY("invalid_inventory", "Invalid inventory"),
    EXCEPT_MISSINGCRAFTCONTEXT("missing_craft_context", "Missing crafting context"),
    EXCEPT_MISSINGCRAFTRESULT("missing_craft_result", "Missing craft result"),
    EXCEPT_MISSINGVARIABLE("missing_variable", "Missing variable"),
    EXCEPT_NOTENOUGHVARIABLES("not_enough_variables", "Not enough variables")
    ;

    private final String code;
    private final String description;

    private static final Map<String, ProgException> EXCEPTION_MAP = new HashMap<>();

    static {
        for (ProgException exception : ProgException.values()) {
            EXCEPTION_MAP.put(exception.getCode(), exception);
        }
    }

    ProgException(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static ProgException getExceptionForCode(String code) {
        return EXCEPTION_MAP.get(code);
    }
}

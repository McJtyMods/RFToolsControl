package mcjty.rftoolscontrol.logic.program;

import net.minecraft.util.EnumFacing;

public enum ParameterType {
    PAR_STRING() {
        @Override
        public String stringRepresentation(Object value) {
            return (String) value;
        }

        @Override
        public Object convertToObject(String input) {
            return input;
        }
    },
    PAR_INTEGER() {
        @Override
        public String stringRepresentation(Object value) {
            if (value == null) {
                return "null";
            }
            return Integer.toString((Integer) value);
        }

        @Override
        public Object convertToObject(String input) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    },
    PAR_FLOAT() {
        @Override
        public String stringRepresentation(Object value) {
            if (value == null) {
                return "null";
            }
            return Float.toString((Float) value);
        }

        @Override
        public Object convertToObject(String input) {
            try {
                return Float.parseFloat(input);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }
    },
    PAR_SIDE() {
        @Override
        public String stringRepresentation(Object value) {
            if (value == null) {
                return "null";
            } else {
                return ((EnumFacing) value).getName();
            }
        }

        @Override
        public Object convertToObject(String input) {
            if (input.trim().isEmpty()) {
                return null;
            }
            // @todo
            return EnumFacing.NORTH;
        }
    };

    public String stringRepresentation(Object value) {
        return "?";
    }

    public Object convertToObject(String input) {
        return input;
    }
}

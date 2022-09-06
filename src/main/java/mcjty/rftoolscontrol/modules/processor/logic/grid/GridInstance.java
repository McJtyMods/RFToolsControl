package mcjty.rftoolscontrol.modules.processor.logic.grid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.rftoolsbase.api.control.code.Opcode;
import mcjty.rftoolsbase.api.control.parameters.Parameter;
import mcjty.rftoolsbase.api.control.parameters.ParameterDescription;
import mcjty.rftoolsbase.api.control.parameters.ParameterValue;
import mcjty.rftoolscontrol.modules.processor.logic.Connection;
import mcjty.rftoolscontrol.modules.processor.logic.ParameterTools;
import mcjty.rftoolscontrol.modules.processor.logic.registry.Opcodes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class GridInstance {

    private final String id;
    private final Connection primaryConnection;
    private final Connection secondaryConnection;
    private final List<Parameter> parameters;

    private GridInstance(Builder builder) {
        this.id = builder.id;
        this.primaryConnection = builder.primaryConnection;
        this.secondaryConnection = builder.secondaryConnection;
        this.parameters = builder.parameters;
    }

    public String getId() {
        return id;
    }

    public Connection getPrimaryConnection() {
        return primaryConnection;
    }

    public Connection getSecondaryConnection() {
        return secondaryConnection;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public JsonElement getJsonElement() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("id", new JsonPrimitive(getId()));
        if (primaryConnection != null) {
            jsonObject.add("primary", new JsonPrimitive(primaryConnection.getId()));
        }
        if (secondaryConnection != null) {
            jsonObject.add("secondary", new JsonPrimitive(secondaryConnection.getId()));
        }
        JsonArray array = new JsonArray();
        for (Parameter parameter : getParameters()) {
            array.add(ParameterTools.getJsonElement(parameter));
        }
        jsonObject.add("parameters", array);

        return jsonObject;
    }

    public static GridInstance readFromJson(JsonElement element) {
        JsonObject gridObject = element.getAsJsonObject();
        String id = gridObject.get("id").getAsString();
        GridInstance.Builder builder = GridInstance.builder(id);
        if (gridObject.has("primary")) {
            String primary = gridObject.get("primary").getAsString();
            builder.primaryConnection(Connection.getConnection(primary));
        }
        if (gridObject.has("secondary")) {
            String secondary = gridObject.get("secondary").getAsString();
            builder.secondaryConnection(Connection.getConnection(secondary));
        }

        Opcode opcode = Opcodes.OPCODES.get(id);
        if (opcode == null) {
            // Sanity check in case an opcode got removed
            return null;
        }
        List<ParameterDescription> parameters = opcode.getParameters();

        JsonArray parameterArray = gridObject.get("parameters").getAsJsonArray();
        for (int i = 0 ; i < parameterArray.size() ; i++) {
            JsonObject parObject = parameterArray.get(i).getAsJsonObject();
            Parameter parameter = ParameterTools.readFromJson(parObject);
            if (parameter.getParameterType() != parameters.get(i).getType()) {
                // Sanity check
                builder.parameter(Parameter.builder().type(parameters.get(i).getType()).value(ParameterValue.constant(null)).build());
            } else {
                builder.parameter(parameter);
            }
        }

        return builder.build();
    }

    public CompoundTag writeToNBT(int x, int y) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putString("id", getId());
        if (primaryConnection != null) {
            tag.putString("prim", primaryConnection.getId());
        }
        if (secondaryConnection != null) {
            tag.putString("sec", secondaryConnection.getId());
        }

        ListTag parList = new ListTag();
        for (Parameter parameter : getParameters()) {
            CompoundTag nbt = ParameterTools.writeToNBT(parameter);

            parList.add(nbt);
        }
        tag.put("pars", parList);
        return tag;
    }

    public static GridInstance readFromNBT(CompoundTag tag) {
        String opcodeid = tag.getString("id");
        GridInstance.Builder builder = GridInstance.builder(opcodeid);
        if (tag.contains("prim")) {
            builder.primaryConnection(Connection.getConnection(tag.getString("prim")));
        }
        if (tag.contains("sec")) {
            builder.secondaryConnection(Connection.getConnection(tag.getString("sec")));
        }

        Opcode opcode = Opcodes.OPCODES.get(opcodeid);
        if (opcode == null) {
            // Sanity check in case an opcode got removed
            return null;
        }
        List<ParameterDescription> parameters = opcode.getParameters();

        ListTag parList = tag.getList("pars", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < parList.size() ; i++) {
            CompoundTag parTag = (CompoundTag) parList.get(i);
            Parameter parameter = ParameterTools.readFromNBT(parTag);
            if (parameter.getParameterType() != parameters.get(i).getType()) {
                // Sanity check
                builder.parameter(Parameter.builder().type(parameters.get(i).getType()).value(ParameterValue.constant(null)).build());
            } else {
                builder.parameter(parameter);
            }
        }

        return builder.build();
    }

    public static class Builder {

        private final String id;
        private Connection primaryConnection;
        private Connection secondaryConnection;
        private final List<Parameter> parameters = new ArrayList<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder primaryConnection(Connection primaryConnection) {
            this.primaryConnection = primaryConnection;
            return this;
        }

        public Builder secondaryConnection(Connection secondaryConnection) {
            this.secondaryConnection = secondaryConnection;
            return this;
        }

        public Builder parameter(Parameter parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public GridInstance build() {
            return new GridInstance(this);
        }
    }
}

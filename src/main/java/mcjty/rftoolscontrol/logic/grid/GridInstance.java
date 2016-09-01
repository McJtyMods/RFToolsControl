package mcjty.rftoolscontrol.logic.grid;

import mcjty.rftoolscontrol.logic.Connection;
import mcjty.rftoolscontrol.logic.Parameter;
import mcjty.rftoolscontrol.logic.registry.ParameterDescription;
import mcjty.rftoolscontrol.logic.registry.ParameterType;
import mcjty.rftoolscontrol.logic.registry.ParameterValue;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class GridInstance {

    private final String id;
    private final List<Connection> connections;
    private final List<Parameter> parameters;

    private GridInstance(Builder builder) {
        this.id = builder.id;
        this.connections = new ArrayList<>(builder.connections);
        this.parameters = builder.parameters;
    }

    public String getId() {
        return id;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public NBTTagCompound writeToNBT(int x, int y) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("id", getId());

        StringBuilder c = new StringBuilder();
        for (Connection connection : getConnections()) {
            c.append(connection.getId());
        }
        tag.setString("con", c.toString());

        NBTTagList parList = new NBTTagList();
        for (Parameter parameter : getParameters()) {
            String name = parameter.getParameterDescription().getName();
            ParameterType type = parameter.getParameterDescription().getType();
            ParameterValue value = parameter.getParameterValue();
            NBTTagCompound parTag = new NBTTagCompound();
            parTag.setString("name", name);
            parTag.setInteger("type", type.ordinal());
            type.writeToNBT(parTag, value);
            parList.appendTag(parTag);
        }
        tag.setTag("pars", parList);
        return tag;
    }

    public static GridInstance readFromNBT(NBTTagCompound tag) {
        GridInstance.Builder builder = GridInstance.builder(tag.getString("id"));
        String con = tag.getString("con");
        for (int i = 0 ; i < con.length() ; i++) {
            String c = con.substring(i, i + 1);
            Connection connection = Connection.getConnection(c);
            if (connection != null) {
                builder.connection(connection);
            }
        }
        NBTTagList parList = tag.getTagList("pars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < parList.tagCount() ; i++) {
            NBTTagCompound parTag = (NBTTagCompound) parList.get(i);
            String name = parTag.getString("name");
            ParameterType type = ParameterType.values()[parTag.getInteger("type")];
            ParameterDescription description = ParameterDescription.builder().name(name).type(type).build();
            ParameterValue value = type.readFromNBT(parTag);
            Parameter parameter = Parameter.builder().description(description).value(value).build();
            builder.parameter(parameter);
        }

        return builder.build();
    }

    public static class Builder {

        private final String id;
        private final List<Connection> connections = new ArrayList<>();
        private final List<Parameter> parameters = new ArrayList<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder connection(Connection connection) {
            connections.add(connection);
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

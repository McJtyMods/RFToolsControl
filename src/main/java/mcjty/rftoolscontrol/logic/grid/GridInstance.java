package mcjty.rftoolscontrol.logic.grid;

import mcjty.rftoolscontrol.logic.Connection;
import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

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

    public NBTTagCompound writeToNBT(int x, int y) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", x);
        tag.setInteger("y", y);
        tag.setString("id", getId());
        if (primaryConnection != null) {
            tag.setString("prim", primaryConnection.getId());
        }
        if (secondaryConnection != null) {
            tag.setString("sec", secondaryConnection.getId());
        }

        NBTTagList parList = new NBTTagList();
        for (Parameter parameter : getParameters()) {
            parList.appendTag(Parameter.writeToNBT(parameter));
        }
        tag.setTag("pars", parList);
        return tag;
    }

    public static GridInstance readFromNBT(NBTTagCompound tag) {
        GridInstance.Builder builder = GridInstance.builder(tag.getString("id"));
        if (tag.hasKey("prim")) {
            builder.primaryConnection(Connection.getConnection(tag.getString("prim")));
        }
        if (tag.hasKey("sec")) {
            builder.secondaryConnection(Connection.getConnection(tag.getString("sec")));
        }
        NBTTagList parList = tag.getTagList("pars", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < parList.tagCount() ; i++) {
            NBTTagCompound parTag = (NBTTagCompound) parList.get(i);
            builder.parameter(Parameter.readFromNBT(parTag));
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

package mcjty.rftoolscontrol.logic.registry;

import mcjty.rftoolscontrol.blocks.processor.ProgException;
import mcjty.rftoolscontrol.logic.Parameter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public enum ParameterType {
    PAR_STRING() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return (String) value;
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            tag.setString("v", (String) value);
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            return ParameterValue.constant(tag.getString("v"));
        }
    },
    PAR_INTEGER() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return Integer.toString((Integer) value);
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            tag.setInteger("v", (Integer) value);
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            return ParameterValue.constant(tag.getInteger("v"));
        }
    },
    PAR_FLOAT() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return Float.toString((Float) value);
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            tag.setFloat("v", (Float) value);
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            return ParameterValue.constant(tag.getFloat("v"));
        }
    },
    PAR_SIDE() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            BlockSide side = (BlockSide) value;
            EnumFacing facing = side.getSide();
            return StringUtils.capitalize(facing == null ? "*" : facing.getName());
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            BlockSide side = (BlockSide) value;
            tag.setInteger("v", side.getSide() == null ? -1 : side.getSide().ordinal());
            tag.setString("node", side.getNodeName() == null ? "" : side.getNodeName());
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            int v = tag.getInteger("v");
            EnumFacing facing = v == -1 ? null : EnumFacing.values()[v];
            String node = tag.getString("node");
            return ParameterValue.constant(new BlockSide(node, facing));
        }
    },
    PAR_BOOLEAN() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return ((Boolean) value) ? "true" : "false";
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            tag.setBoolean("v", (Boolean) value);
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            return ParameterValue.constant(tag.getBoolean("v"));
        }
    },
    PAR_INVENTORY() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return "inv";
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            Inventory inv = (Inventory) value;
            if (inv.getNodeName() != null) {
                tag.setString("nodeName", inv.getNodeName());
            }
            tag.setInteger("side", inv.getSide().ordinal());
            if (inv.getIntSide() != null) {
                tag.setInteger("intSide", inv.getIntSide().ordinal());
            }
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            EnumFacing side = EnumFacing.values()[tag.getInteger("side")];
            String name = null;
            EnumFacing intSide = null;
            if (tag.hasKey("nodeName")) {
                name = tag.getString("nodeName");
            }
            if (tag.hasKey("intSide")) {
                intSide = EnumFacing.values()[tag.getInteger("intSide")];
            }
            return ParameterValue.constant(new Inventory(name, side, intSide));
        }
    },
    PAR_ITEM() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            // @todo, nicer
            return "item";
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            ItemStack inv = (ItemStack) value;
            NBTTagCompound tc = new NBTTagCompound();
            inv.writeToNBT(tc);
            tag.setTag("item", tc);
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            if (tag.hasKey("item")) {
                NBTTagCompound tc = (NBTTagCompound) tag.getTag("item");
                ItemStack stack = ItemStack.loadItemStackFromNBT(tc);
                return ParameterValue.constant(stack);
            }
            return ParameterValue.constant(null);
        }
    },
    PAR_EXCEPTION() {
        @Override
        protected String stringRepresentationInternal(Object value) {
            ProgException exception = (ProgException) value;
            return exception.getCode();
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            ProgException exception = (ProgException) value;
            tag.setString("code", exception.getCode());
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            String code = tag.getString("code");
            return ParameterValue.constant(ProgException.getExceptionForCode(code));
        }
    };

    public String stringRepresentation(ParameterValue value) {
        if (value.isVariable()) {
            return "V:" + value.getVariableIndex();
        } else if (value.isFunction()) {
            return "F:" + value.getFunction().getName();
        } else if (value.getValue() == null) {
            return "NULL";
        } else {
            return stringRepresentationInternal(value.getValue());
        }
    }

    protected String stringRepresentationInternal(Object value) {
        return "?";
    }

    public void writeToNBT(NBTTagCompound tag, ParameterValue value) {
        if (value.isVariable()) {
            tag.setInteger("varIdx", value.getVariableIndex());
        } else if (value.isFunction()) {
            tag.setString("funId", value.getFunction().getId());
            NBTTagList parList = new NBTTagList();
            for (Parameter parameter : value.getFunctionParameters()) {
                parList.appendTag(Parameter.writeToNBT(parameter));
            }
            tag.setTag("funPars", parList);
        } else if (value.getValue() == null) {
            // No value
            tag.setBoolean("null", true);
        } else {
            writeToNBTInternal(tag, value.getValue());
        }
    }

    public ParameterValue readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("varIdx")) {
            return ParameterValue.variable(tag.getInteger("varIdx"));
        } else if (tag.hasKey("funId")) {
            NBTTagList parList = tag.getTagList("funPars", Constants.NBT.TAG_COMPOUND);
            List<Parameter> parameters = new ArrayList<>();
            for (int i = 0 ; i < parList.tagCount() ; i++) {
                NBTTagCompound parTag = (NBTTagCompound) parList.get(i);
                parameters.add(Parameter.readFromNBT(parTag));
            }
            return ParameterValue.function(Functions.FUNCTIONS.get(tag.getString("funId")), parameters);
        } else if (tag.hasKey("null")) {
            return ParameterValue.constant(null);
        } else {
            return readFromNBTInternal(tag);
        }
    }

    protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
        return ParameterValue.constant(null);
    }

    protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
    }
}

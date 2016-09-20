package mcjty.rftoolscontrol.logic.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import mcjty.rftoolscontrol.logic.running.ExceptionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum ParameterType {
    PAR_STRING("string") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return (String) value;
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            object.add("v", new JsonPrimitive((String) value));
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            return ParameterValue.constant(object.get("v").getAsString());
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
    PAR_INTEGER("integer") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return Integer.toString((Integer) value);
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            object.add("v", new JsonPrimitive((Integer) value));
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            return ParameterValue.constant(object.get("v").getAsInt());
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
    PAR_FLOAT("float") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return Float.toString((Float) value);
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            object.add("v", new JsonPrimitive((Float) value));
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            return ParameterValue.constant(object.get("v").getAsFloat());
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
    PAR_SIDE("side") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            BlockSide side = (BlockSide) value;
            EnumFacing facing = side.getSide();

            String s = facing == null ? "" : StringUtils.left(facing.getName().toUpperCase(), 1);
            if (side.getNodeName() == null) {
                return s;
            } else {
                return StringUtils.left(side.getNodeName(), 7) + " " + s;
            }
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            BlockSide side = (BlockSide) value;
            if (side.getSide() != null) {
                object.add("side", new JsonPrimitive(side.getSide().getName()));
            }
            if (side.getNodeName() != null) {
                object.add("node", new JsonPrimitive(side.getNodeName()));
            }
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            EnumFacing side = object.has("side") ? EnumFacing.byName(object.get("side").getAsString()) : null;
            String node = object.has("node") ? object.get("node").getAsString() : null;
            return ParameterValue.constant(new BlockSide(node, side));
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
    PAR_BOOLEAN("boolean") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            return ((Boolean) value) ? "true" : "false";
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            object.add("v", new JsonPrimitive((Boolean) value));
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            return ParameterValue.constant(object.get("v").getAsBoolean());
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
    PAR_INVENTORY("inventory") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            Inventory inv = (Inventory) value;
            String s = StringUtils.left(inv.getSide().getName().toUpperCase(), 1);
            if (inv.getIntSide() == null) {
                s += "/*";
            } else {
                String is = StringUtils.left(inv.getIntSide().getName().toUpperCase(), 1);
                s += "/" + is;
            }
            if (inv.getNodeName() == null) {
                return s;
            } else {
                return StringUtils.left(inv.getNodeName(), 6) + " " + s;
            }
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            Inventory inv = (Inventory) value;
            object.add("side", new JsonPrimitive(inv.getSide().getName()));
            if (inv.getIntSide() != null) {
                object.add("intside", new JsonPrimitive(inv.getIntSide().getName()));
            }
            if (inv.getNodeName() != null) {
                object.add("node", new JsonPrimitive(inv.getNodeName()));
            }
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            EnumFacing side = EnumFacing.byName(object.get("side").getAsString());
            EnumFacing intSide = object.has("intside") ? EnumFacing.byName(object.get("intside").getAsString()) : null;
            String node = object.has("node") ? object.get("node").getAsString() : null;
            return ParameterValue.constant(new Inventory(node, side, intSide));
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
    PAR_ITEM("item") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            ItemStack inv = (ItemStack) value;
            return StringUtils.left(inv.getDisplayName(), 10);
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            ItemStack item = (ItemStack) value;
            object.add("item", new JsonPrimitive(item.getItem().getRegistryName().toString()));
            object.add("meta", new JsonPrimitive(item.getItemDamage()));
            if (item.hasTagCompound()) {
                object.add("nbt", new JsonPrimitive(item.getTagCompound().toString()));
            }
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            String itemReg = object.get("item").getAsString();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemReg));
            int meta = object.get("meta").getAsInt();
            ItemStack stack = new ItemStack(item, meta);
            if (object.has("nbt")) {
                String nbt = object.get("nbt").getAsString();
                NBTTagCompound tagCompound = null;
                try {
                    tagCompound = JsonToNBT.getTagFromJson(nbt);
                } catch (NBTException e) {
                    // @todo What to do?
                }
                stack.setTagCompound(tagCompound);
            }
            return ParameterValue.constant(stack);
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            ItemStack item = (ItemStack) value;
            NBTTagCompound tc = new NBTTagCompound();
            item.writeToNBT(tc);
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
    PAR_EXCEPTION("exception") {
        @Override
        protected String stringRepresentationInternal(Object value) {
            ExceptionType exception = (ExceptionType) value;
            return exception.getCode();
        }

        @Override
        protected void writeToJsonInternal(JsonObject object, Object value) {
            ExceptionType exception = (ExceptionType) value;
            object.add("code", new JsonPrimitive(exception.getCode()));
        }

        @Override
        protected ParameterValue readFromJsonInternal(JsonObject object) {
            String code = object.get("code").getAsString();
            return ParameterValue.constant(ExceptionType.getExceptionForCode(code));
        }

        @Override
        protected void writeToNBTInternal(NBTTagCompound tag, Object value) {
            ExceptionType exception = (ExceptionType) value;
            tag.setString("code", exception.getCode());
        }

        @Override
        protected ParameterValue readFromNBTInternal(NBTTagCompound tag) {
            String code = tag.getString("code");
            return ParameterValue.constant(ExceptionType.getExceptionForCode(code));
        }
    };

    public String stringRepresentation(ParameterValue value) {
        if (value.isVariable()) {
            return "V:" + value.getVariableIndex();
        } else if (value.isFunction()) {
            return "F:" + value.getFunction().getName();
        } else if (value.getValue() == null) {
            return "";
        } else {
            return stringRepresentationInternal(value.getValue());
        }
    }

    protected String stringRepresentationInternal(Object value) {
        return "?";
    }

    public JsonElement writeToJson(ParameterValue value) {
        JsonObject jsonObject = new JsonObject();
        if (value.isVariable()) {
            jsonObject.add("var", new JsonPrimitive(value.getVariableIndex()));
        } else if (value.isFunction()) {
            jsonObject.add("fun", new JsonPrimitive(value.getFunction().getId()));
        } else if (value.getValue() == null) {
            jsonObject.add("null", new JsonPrimitive(true));
        } else {
            writeToJsonInternal(jsonObject, value.getValue());
        }

        return jsonObject;
    }

    public ParameterValue readFromJson(JsonObject object) {
        if (object.has("var")) {
            return ParameterValue.variable(object.get("var").getAsInt());
        } else if (object.has("fun")) {
            return ParameterValue.function(Functions.FUNCTIONS.get(object.get("fun").getAsString()));
        } else if (object.has("null")) {
            return ParameterValue.constant(null);
        } else {
            return readFromJsonInternal(object);
        }
    }

    public void writeToNBT(NBTTagCompound tag, ParameterValue value) {
        if (value.isVariable()) {
            tag.setInteger("varIdx", value.getVariableIndex());
        } else if (value.isFunction()) {
            tag.setString("funId", value.getFunction().getId());
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
            return ParameterValue.function(Functions.FUNCTIONS.get(tag.getString("funId")));
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

    protected void writeToJsonInternal(JsonObject object, Object value) {
    }

    protected ParameterValue readFromJsonInternal(JsonObject object) {
        return null;
    }

    private final String name;

    private static final Map<String, ParameterType> TYPE_MAP = new HashMap<>();

    static {
        for (ParameterType type : values()) {
            TYPE_MAP.put(type.getName(), type);
        }

    }

    ParameterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ParameterType getByName(String name) {
        return TYPE_MAP.get(name);
    }
}

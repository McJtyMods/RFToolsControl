package mcjty.rftoolscontrol.logic.grid;

import com.google.gson.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class ProgramCardInstance {

    private Map<GridPos, GridInstance> gridInstances = new HashMap<>();

    public Map<GridPos, GridInstance> getGridInstances() {
        return gridInstances;
    }

    /**
     * NBT Structure:
     * "grid": [
     *     "[
     *         "x": 3.
     *         "y": 4,
     *         "id": "rs.if",
     *         "con": "uW",
     *         "pars": ...
     *     ],
     *     [
     *     ]
     * ]
     */

    private ProgramCardInstance() {

    }

    public static ProgramCardInstance newInstance() {
        return new ProgramCardInstance();
    }

    public static ProgramCardInstance parseInstance(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        ProgramCardInstance instance = new ProgramCardInstance();

        NBTTagList grid = tagCompound.getTagList("grid", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < grid.tagCount() ; i++) {
            NBTTagCompound gridElement = (NBTTagCompound) grid.get(i);
            parseElement(gridElement, instance);
        }
        return instance;
    }

    private static void parseElement(NBTTagCompound tag, ProgramCardInstance instance) {
        int x = tag.getInteger("x");
        int y = tag.getInteger("y");
        GridInstance gi = GridInstance.readFromNBT(tag);
        if (gi != null) {
            instance.putGridInstance(x, y, gi);
        }
    }

    public void putGridInstance(int x, int y, GridInstance gridInstance) {
        gridInstances.put(GridPos.pos(x, y), gridInstance);
    }

    public static ProgramCardInstance readFromJson(String json) {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(json);
        ProgramCardInstance instance = new ProgramCardInstance();
        for (JsonElement entry : root.getAsJsonArray()) {
            JsonElement posElement = entry.getAsJsonObject().get("pos");
            JsonElement gridElement = entry.getAsJsonObject().get("opcode");
            int x = posElement.getAsJsonObject().get("x").getAsInt();
            int y = posElement.getAsJsonObject().get("y").getAsInt();
            GridInstance gi = GridInstance.readFromJson(gridElement);
            if (gi != null) {
                instance.putGridInstance(x, y, gi);
            }
        }
        return instance;
    }

    public String writeToJson() {
        JsonArray array = new JsonArray();
        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos coordinate = entry.getKey();
            GridInstance gridInstance = entry.getValue();

            JsonObject ruleObject = new JsonObject();
            ruleObject.add("pos", buildCoordinateElement(coordinate));
            ruleObject.add("opcode", gridInstance.getJsonElement());
            array.add(ruleObject);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(array);
    }

    private JsonElement buildCoordinateElement(GridPos pos) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("x", new JsonPrimitive(pos.getX()));
        jsonObject.add("y", new JsonPrimitive(pos.getY()));
        return jsonObject;
    }

    public void writeToNBT(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            card.setTagCompound(tagCompound);
        }
        NBTTagList grid = new NBTTagList();

        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos coordinate = entry.getKey();
            int x = coordinate.getX();
            int y = coordinate.getY();
            GridInstance gridInstance = entry.getValue();
            NBTTagCompound tag = gridInstance.writeToNBT(x, y);
            grid.appendTag(tag);
        }

        tagCompound.setTag("grid", grid);
    }

}

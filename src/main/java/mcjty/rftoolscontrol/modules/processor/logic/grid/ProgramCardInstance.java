package mcjty.rftoolscontrol.modules.processor.logic.grid;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

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
        CompoundTag tagCompound = card.getTag();
        if (tagCompound == null) {
            return null;
        }
        ProgramCardInstance instance = new ProgramCardInstance();

        ListTag grid = tagCompound.getList("grid", Tag.TAG_COMPOUND);
        for (Tag inbt : grid) {
            CompoundTag gridElement = (CompoundTag) inbt;
            parseElement(gridElement, instance);
        }
        return instance;
    }

    private static void parseElement(CompoundTag tag, ProgramCardInstance instance) {
        int x = tag.getInt("x");
        int y = tag.getInt("y");
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
        CompoundTag tagCompound = card.getOrCreateTag();
        ListTag grid = new ListTag();

        for (Map.Entry<GridPos, GridInstance> entry : gridInstances.entrySet()) {
            GridPos coordinate = entry.getKey();
            int x = coordinate.getX();
            int y = coordinate.getY();
            GridInstance gridInstance = entry.getValue();
            CompoundTag tag = gridInstance.writeToNBT(x, y);
            grid.add(tag);
        }

        tagCompound.put("grid", grid);
    }

}

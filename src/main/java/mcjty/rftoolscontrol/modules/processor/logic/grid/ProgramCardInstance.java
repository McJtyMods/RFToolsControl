package mcjty.rftoolscontrol.modules.processor.logic.grid;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolscontrol.modules.various.VariousModule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ProgramCardInstance {

    private final Map<GridPos, GridInstance> gridInstances = new HashMap<>();

    public Map<GridPos, GridInstance> getGridInstances() {
        return gridInstances;
    }

    // Codec/StreamCodec for persisting on ItemStack
    private record Entry(GridPos pos, GridInstance opcode) {}

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            GridPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
            GridInstance.CODEC.fieldOf("opcode").forGetter(Entry::opcode)
    ).apply(inst, Entry::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Entry> ENTRY_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, e -> e.pos().x(),
            ByteBufCodecs.VAR_INT, e -> e.pos().y(),
            GridInstance.STREAM_CODEC, Entry::opcode,
            (x, y, op) -> new Entry(new GridPos(x, y), op)
    );

    public static final Codec<ProgramCardInstance> CODEC = ENTRY_CODEC.listOf().xmap(
            list -> {
                ProgramCardInstance inst = new ProgramCardInstance();
                for (Entry e : list) {
                    inst.putGridInstance(e.pos().x(), e.pos().y(), e.opcode());
                }
                return inst;
            },
            inst -> {
                List<Entry> list = new ArrayList<>();
                for (Map.Entry<GridPos, GridInstance> me : inst.gridInstances.entrySet()) {
                    list.add(new Entry(me.getKey(), me.getValue()));
                }
                return list;
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgramCardInstance> STREAM_CODEC = StreamCodec.of(
            (buf, inst) -> {
                ArrayList<Entry> list = new ArrayList<>();
                for (Map.Entry<GridPos, GridInstance> me : inst.gridInstances.entrySet()) {
                    list.add(new Entry(me.getKey(), me.getValue()));
                }
                ByteBufCodecs.VAR_INT.encode(buf, list.size());
                for (Entry e : list) {
                    ENTRY_STREAM_CODEC.encode(buf, e);
                }
            },
            buf -> {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                ProgramCardInstance inst = new ProgramCardInstance();
                for (int i = 0; i < size; i++) {
                    Entry e = ENTRY_STREAM_CODEC.decode(buf);
                    inst.putGridInstance(e.pos().x(), e.pos().y(), e.opcode());
                }
                return inst;
            }
    );

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
        jsonObject.add("x", new JsonPrimitive(pos.x()));
        jsonObject.add("y", new JsonPrimitive(pos.y()));
        return jsonObject;
    }

    public void writeToNBT(ItemStack card) {
        card.set(VariousModule.PROGRAM_CARD_DATA.get(), this);
    }

}

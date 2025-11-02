package mcjty.rftoolscontrol.modules.processor.logic.grid;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;

public record ProgramCardInstance(Map<GridPos, GridInstance> gridInstances) {

    private static final ProgramCardInstance EMPTY = new ProgramCardInstance(Collections.emptyMap());

    public ProgramCardInstance {
        Objects.requireNonNull(gridInstances, "gridInstances cannot be null");
        gridInstances = Collections.unmodifiableMap(new LinkedHashMap<>(gridInstances));
    }

    public static ProgramCardInstance empty() {
        return EMPTY;
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
                Map<GridPos, GridInstance> map = new HashMap<>();
                for (Entry e : list) {
                    map.put(e.pos(), e.opcode());
                }
                return new ProgramCardInstance(map);
            },
            inst -> {
                List<Entry> list = new ArrayList<>();
                for (Map.Entry<GridPos, GridInstance> me : inst.gridInstances().entrySet()) {
                    list.add(new Entry(me.getKey(), me.getValue()));
                }
                return list;
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgramCardInstance> STREAM_CODEC = StreamCodec.of(
            (buf, inst) -> {
                ArrayList<Entry> list = new ArrayList<>();
                for (Map.Entry<GridPos, GridInstance> me : inst.gridInstances().entrySet()) {
                    list.add(new Entry(me.getKey(), me.getValue()));
                }
                ByteBufCodecs.VAR_INT.encode(buf, list.size());
                for (Entry e : list) {
                    ENTRY_STREAM_CODEC.encode(buf, e);
                }
            },
            buf -> {
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                Map<GridPos, GridInstance> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    Entry e = ENTRY_STREAM_CODEC.decode(buf);
                    map.put(e.pos(), e.opcode());
                }
                return new ProgramCardInstance(map);
            }
    );

    public ProgramCardInstance withGridInstance(int x, int y, GridInstance gridInstance) {
        Objects.requireNonNull(gridInstance, "gridInstance cannot be null");
        Map<GridPos, GridInstance> mutable = new HashMap<>(gridInstances());
        mutable.put(GridPos.pos(x, y), gridInstance);
        return new ProgramCardInstance(mutable);
    }

    public ProgramCardInstance withGridInstance(GridPos pos, GridInstance gridInstance) {
        Objects.requireNonNull(pos, "pos cannot be null");
        return withGridInstance(pos.x(), pos.y(), gridInstance);
    }

    public ProgramCardInstance withoutGridInstance(GridPos pos) {
        Objects.requireNonNull(pos, "pos cannot be null");
        if (!gridInstances.containsKey(pos)) {
            return this;
        }
        Map<GridPos, GridInstance> mutable = new HashMap<>(gridInstances());
        mutable.remove(pos);
        return new ProgramCardInstance(mutable);
    }

    public GridInstance gridInstanceAt(GridPos pos) {
        return gridInstances.get(pos);
    }

    public static ProgramCardInstance readFromJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        Map<GridPos, GridInstance> map = new HashMap<>();
        for (JsonElement entry : root.getAsJsonArray()) {
            JsonElement posElement = entry.getAsJsonObject().get("pos");
            JsonElement gridElement = entry.getAsJsonObject().get("opcode");
            int x = posElement.getAsJsonObject().get("x").getAsInt();
            int y = posElement.getAsJsonObject().get("y").getAsInt();
            GridInstance gi = GridInstance.readFromJson(gridElement);
            if (gi != null) {
                map.put(GridPos.pos(x, y), gi);
            }
        }
        return new ProgramCardInstance(map);
    }

    public String writeToJson() {
        JsonArray array = new JsonArray();
        for (Map.Entry<GridPos, GridInstance> entry : gridInstances().entrySet()) {
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

    private static JsonElement buildCoordinateElement(GridPos pos) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("x", new JsonPrimitive(pos.x()));
        jsonObject.add("y", new JsonPrimitive(pos.y()));
        return jsonObject;
    }
}

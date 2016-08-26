package mcjty.rftoolscontrol.logic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class ProgramCardInstance {

    private Map<Pair<Integer, Integer>, GridInstance> gridInstances = new HashMap<>();

    public Map<Pair<Integer, Integer>, GridInstance> getGridInstances() {
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
        GridInstance gridInstance = new GridInstance(tag.getString("id"));
        String con = tag.getString("con");
        for (int i = 0 ; i < con.length() ; i++) {
            String c = con.substring(i, i + 1);
            Connection connection = Connection.getConnection(c);
            if (connection != null) {
                gridInstance.addConnection(connection);
            }
        }
        instance.putGridInstance(x, y, gridInstance);
    }

    public void putGridInstance(int x, int y, GridInstance gridInstance) {
        gridInstances.put(Pair.of(x, y), gridInstance);
    }

    public void writeToNBT(ItemStack card) {
        NBTTagCompound tagCompound = card.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            card.setTagCompound(tagCompound);
        }
        NBTTagList grid = new NBTTagList();

        for (Map.Entry<Pair<Integer, Integer>, GridInstance> entry : gridInstances.entrySet()) {
            int x = entry.getKey().getLeft();
            int y = entry.getKey().getRight();
            GridInstance gridInstance = entry.getValue();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", x);
            tag.setInteger("y", y);
            tag.setString("id", gridInstance.getId());
            StringBuilder c = new StringBuilder();
            for (Connection connection : gridInstance.getConnections()) {
                c.append(connection.getId());
            }
            tag.setString("con", c.toString());

            grid.appendTag(tag);
        }

        tagCompound.setTag("grid", grid);
    }

}

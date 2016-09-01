package mcjty.rftoolscontrol.logic.grid;

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
        instance.putGridInstance(x, y, GridInstance.readFromNBT(tag));
    }

    public void putGridInstance(int x, int y, GridInstance gridInstance) {
        gridInstances.put(GridPos.pos(x, y), gridInstance);
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

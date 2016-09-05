package mcjty.rftoolscontrol.items;

public class CPUCoreItem extends GenericRFToolsItem {

    private final int tier;

    public CPUCoreItem(String name, int tier) {
        super(name);
        setMaxStackSize(1);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }
}

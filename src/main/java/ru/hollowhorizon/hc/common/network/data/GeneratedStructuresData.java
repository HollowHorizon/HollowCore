package ru.hollowhorizon.hc.common.network.data;

import net.minecraft.util.math.BlockPos;

public class GeneratedStructuresData implements HollowDataForServer {

    public static final GeneratedStructuresData INSTANCE = new GeneratedStructuresData();

    @Override
    public String getFileName() {
        return "hollow_structures";
    }

    public BlockPos getStructurePos(String string) {
        for(String s : this.getAll()) {
            String[] data = s.split(":");
            if((data[0]+data[1]).equals(string)) {
                
                return new BlockPos(Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]));
            }
        }
        return null;
    }
}

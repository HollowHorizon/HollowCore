package ru.hollowhorizon.hc.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

public class VoxelShapeHelper {
    public static VoxelShape loadFromLocation(ResourceLocation location) {
        JsonObject object = SaveJsonHelper.readObject(HollowJavaUtils.getResource(location));

        List<VoxelShape> shapes = new ArrayList<>();
        object.getAsJsonArray("elements").forEach(element -> {
            JsonObject o = element.getAsJsonObject();
            JsonArray from = o.getAsJsonArray("from");
            int x1 = from.get(0).getAsInt();
            int y1 = from.get(1).getAsInt();
            int z1 = from.get(2).getAsInt();
            JsonArray to = o.getAsJsonArray("to");
            int x2 = to.get(0).getAsInt();
            int y2 = to.get(1).getAsInt();
            int z2 = to.get(2).getAsInt();
            shapes.add(Block.box(x1, y1, z1, x2, y2, z2));
        });
        VoxelShape root = shapes.get(0);
        shapes.remove(0);
        return VoxelShapes.or(root, shapes.toArray(new VoxelShape[0]));
    }
}

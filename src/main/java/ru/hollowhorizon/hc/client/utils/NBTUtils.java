package ru.hollowhorizon.hc.client.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilities;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NBTUtils {
    public static final Map<String, HollowNBTSerializer<?>> SERIALIZERS = new HashMap<>();

    public static final HollowNBTSerializer<HollowCapability<?>> HOLLOW_CAPABILITY_SERIALIZER = new HollowNBTSerializer<HollowCapability<?>>("hollow_capability_serializer") {
        @Override
        public HollowCapability<?> fromNBT(CompoundNBT nbt) {
            String nbts = nbt.getString("cap");
            System.out.println(nbts);
            String[] s = nbts.split(":");
            ResourceLocation location = new ResourceLocation(s[0], s[1]);

            Capability<HollowCapability<?>> capability = HollowCapabilities.CAPABILITIES.get(location);
            HollowCapability<?> capabilityInstance = capability.getDefaultInstance();
            capabilityInstance.readNBT(nbt.getCompound("nbt"));
            return capabilityInstance;
        }

        @Override
        public CompoundNBT toNBT(HollowCapability<?> value) {
            CompoundNBT compound = new CompoundNBT();
            System.out.println(value.getRegistryName().toString());
            compound.putString("cap", value.getRegistryName().toString());
            compound.put("nbt", value.writeNBT());
            return compound;
        }
    };
    public static final HollowNBTSerializer<Integer> INTEGER_SERIALIZER = new HollowNBTSerializer<Integer>("int") {
        @Override
        public Integer fromNBT(CompoundNBT nbt) {
            return nbt.getInt("value");
        }

        @Override
        public CompoundNBT toNBT(Integer value) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("value", value);
            return nbt;
        }
    };
    public static final HollowNBTSerializer<Float> FLOAT_SERIALIZER = new HollowNBTSerializer<Float>("float") {
        @Override
        public Float fromNBT(CompoundNBT nbt) {
            return nbt.getFloat("value");
        }

        @Override
        public CompoundNBT toNBT(Float value) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("value", value);
            return nbt;
        }
    };
    public static final HollowNBTSerializer<AxisAlignedBB> AXISABB_SERIALIZER = new HollowNBTSerializer<AxisAlignedBB>("axisabb") {
        @Override
        public AxisAlignedBB fromNBT(CompoundNBT nbt) {
            return new AxisAlignedBB(
                    nbt.getDouble("min_x"),
                    nbt.getDouble("min_y"),
                    nbt.getDouble("min_z"),
                    nbt.getDouble("max_x"),
                    nbt.getDouble("max_y"),
                    nbt.getDouble("max_z")
            );
        }

        @Override
        public CompoundNBT toNBT(AxisAlignedBB value) {
            CompoundNBT nbt = new CompoundNBT();

            nbt.putDouble("min_x", value.minX);
            nbt.putDouble("min_y", value.minY);
            nbt.putDouble("min_z", value.minZ);

            nbt.putDouble("max_x", value.maxX);
            nbt.putDouble("max_y", value.maxY);
            nbt.putDouble("max_z", value.maxZ);
            return nbt;
        }
    };
    public static final HollowNBTSerializer<String> STRING_SERIALIZER = new HollowNBTSerializer<String>("string") {
        @Override
        public String fromNBT(CompoundNBT nbt) {
            return nbt.getString("value");
        }

        @Override
        public CompoundNBT toNBT(String value) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("value", value);
            return nbt;
        }
    };
    public static final HollowNBTSerializer<BlockPos> BLOCKPOS_SERIALIZER = new HollowNBTSerializer<BlockPos>("block_pos") {
        @Override
        public BlockPos fromNBT(CompoundNBT nbt) {
            return new BlockPos(nbt.getInt("value_x"), nbt.getInt("value_y"), nbt.getInt("value_z"));
        }

        @Override
        public CompoundNBT toNBT(BlockPos value) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("value_x", value.getX());
            nbt.putInt("value_y", value.getY());
            nbt.putInt("value_z", value.getZ());
            return nbt;
        }
    };

    public static void init() {

    }

    public static <T> void saveList(CompoundNBT nbt, String name, ArrayList<T> data, HollowNBTSerializer<T> serializer) {
        nbt.putInt(name + "_size", data.size());
        int i = 0;
        for (T value : data) {
            nbt.put(name + "_val_" + i, serializer.toNBT(value));
            i++;
        }
    }

    public static <T> ArrayList<T> loadList(CompoundNBT nbt, String name, HollowNBTSerializer<T> serializer) {
        int size = nbt.getInt(name + "_size");
        ArrayList<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(serializer.fromNBT(nbt.getCompound(name + "_val_" + i)));
        }
        return list;
    }

    public static <T> void saveValue(CompoundNBT nbt, String name, T value, HollowNBTSerializer<T> serializer) {
        nbt.put(name, serializer.toNBT(value));
    }

    public static <T> void saveValue(PacketBuffer buffer, String name, T value, HollowNBTSerializer<T> serializer) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(name, serializer.toNBT(value));
        buffer.writeNbt(nbt);
    }

    public static <T> T loadValue(CompoundNBT nbt, String name, HollowNBTSerializer<T> serializer) {
        return serializer.fromNBT(nbt.getCompound(name));
    }

    public static <T> void addSerializer(HollowNBTSerializer<T> tHollowNBTSerializer, String s) {
        SERIALIZERS.put(s, tHollowNBTSerializer);
    }

    public static String getName(HollowNBTSerializer<?> serializer) {
        for (Map.Entry<String, HollowNBTSerializer<?>> ser : SERIALIZERS.entrySet()) {
            if (ser.getValue().equals(serializer)) {
                return ser.getKey();
            }
        }
        return "null";
    }
}

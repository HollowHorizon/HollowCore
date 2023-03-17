package ru.hollowhorizon.hc.client.models.core.model;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.BoneTownConstants;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFArmorModel;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModelLoader;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFSkeleton;
import ru.hollowhorizon.hc.common.registry.BTMaterials;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class BTAnimatedModel extends BTModel {

    private BakedAnimatedMesh[] animatedMeshes;
    private BakedAnimatedMesh combinedAnimatedMesh;
    private BoneMFArmorModel defaultArmor;
    private final Map<IArmorMaterial, BoneMFArmorModel> armorOverrides;
    private BakedArmorMeshes bakedDefaultArmor;
    private final Map<IArmorMaterial, BakedArmorMeshes> bakedArmorOverrides;

    public BTAnimatedModel(ResourceLocation programName,
                           BoneTownConstants.MeshTypes meshType) {
        super(programName, meshType);
        this.armorOverrides = new HashMap<>();
        this.bakedArmorOverrides = new HashMap<>();
        this.defaultArmor = null;
        this.bakedDefaultArmor = null;
    }

    public BTAnimatedModel(BoneTownConstants.MeshTypes meshType){
        this(BTMaterials.DEFAULT_ANIMATED_LOC, meshType);
    }

    private BakedArmorMeshes bakeArmor(BoneMFArmorModel model){
        BakedAnimatedMesh headArmor = model.getCombinedMeshForSlot(EquipmentSlotType.HEAD);
        BakedAnimatedMesh chestArmor = model.getCombinedMeshForSlot(EquipmentSlotType.CHEST);
        BakedAnimatedMesh legArmor = model.getCombinedMeshForSlot(EquipmentSlotType.LEGS);
        BakedAnimatedMesh feetArmor = model.getCombinedMeshForSlot(EquipmentSlotType.FEET);
        return new BakedArmorMeshes(model.getName(), headArmor, chestArmor, legArmor, feetArmor);
    }

    public void bakeArmors(){
        if (hasDefaultArmor()){
            bakedDefaultArmor = bakeArmor(defaultArmor);
        }
        for (IArmorMaterial mat : armorOverrides.keySet()){
            BoneMFArmorModel armorModel = armorOverrides.get(mat);
            BakedArmorMeshes baked = bakeArmor(armorModel);
            bakedArmorOverrides.put(mat, baked);
        }
    }

    public boolean hasDefaultArmor(){
        return this.defaultArmor != null;
    }

    public Optional<BoneMFArmorModel> getArmorForMaterial(IArmorMaterial material){
        if (armorOverrides.containsKey(material)){
            return Optional.of(armorOverrides.get(material));
        } else {
            return Optional.ofNullable(defaultArmor);
        }
    }

    public Optional<BakedArmorMeshes> getBakedArmorForMaterial(IArmorMaterial material){
        if (bakedArmorOverrides.containsKey(material)){
            return Optional.of(bakedArmorOverrides.get(material));
        } else {
            return Optional.ofNullable(bakedDefaultArmor);
        }
    }

    public void addArmorOverride(IArmorMaterial material, @Nonnull BoneMFArmorModel model){
        armorOverrides.put(material, model);
    }

    public void addDefaultArmor(BoneMFArmorModel model){
        this.defaultArmor = model;
    }

    @Override
    public BakedMesh[] getMeshes(){
        return animatedMeshes;
    }

    public Optional<BoneMFSkeleton> getSkeleton(){
        return this.model.getSkeleton();
    }

    public BakedAnimatedMesh[] getAnimatedMeshes() { return animatedMeshes; }

    @Override
    public BakedMesh getCombinedMesh() {
        return combinedAnimatedMesh;
    }

    public BakedAnimatedMesh getCombinedAnimatedMesh(){
        return combinedAnimatedMesh;
    }

    @Override
    public void load() {
        String meshExt = BoneTownConstants.stringFromMeshType(meshType);
        ResourceLocation name = getRegistryName();
        ResourceLocation meshLocation = new ResourceLocation(name.getNamespace(),
                BoneTownConstants.BONETOWN_MODELS_DIR +
                        "/" + name.getPath() + "." + meshExt);
        try {
            InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResource(meshLocation)
                    .getInputStream();
            byte[] _data = IOUtils.toByteArray(stream);
            ByteBuffer data = MemoryUtil.memCalloc(_data.length + 1);
            data.put(_data);
            data.put((byte) 0);
            data.flip();
            stream.close();
            try {
                model = BoneMFModelLoader.load(data, name);
                this.animatedMeshes = model.getBakeAsAnimatedMeshes()
                        .toArray(new BakedAnimatedMesh[0]);
                this.combinedAnimatedMesh = model.getCombinedAnimatedMesh();
                HollowCore.LOGGER.info("Loaded {} animated meshes for {}", animatedMeshes.length, name.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            MemoryUtil.memFree(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

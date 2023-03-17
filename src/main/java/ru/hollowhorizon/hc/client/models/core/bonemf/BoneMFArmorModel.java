package ru.hollowhorizon.hc.client.models.core.bonemf;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.models.core.model.BakedAnimatedMesh;

import java.util.List;
import java.util.stream.Collectors;

public class BoneMFArmorModel extends BoneMFModel {
    private final List<String> headMeshes;
    private final List<String> bodyMeshes;
    private final List<String> legMeshes;
    private final List<String> feetMeshes;

    public BoneMFArmorModel(ResourceLocation name, BoneMFNode rootNode,
                            List<String> headMeshes, List<String> bodyMeshes, List<String> legMeshes,
                            List<String> feetMeshes) {
        super(name, rootNode);
        this.headMeshes = headMeshes;
        this.bodyMeshes = bodyMeshes;
        this.legMeshes = legMeshes;
        this.feetMeshes = feetMeshes;
    }

    private List<String> getSubMeshesForSlot(EquipmentSlotType slotType){
        switch (slotType){
            case FEET:
                return getFeetMeshes();
            case HEAD:
                return getHeadMeshes();
            case LEGS:
                return getLegMeshes();
            case CHEST:
                return getBodyMeshes();
            default:
                return null;
        }
    }

    public BakedAnimatedMesh getCombinedMeshForSlot(EquipmentSlotType slotType){
        if (slotType == EquipmentSlotType.MAINHAND || slotType == EquipmentSlotType.OFFHAND){
            return null;
        }
        return getCombinedMeshForSubMeshes(getSubMeshesForSlot(slotType));
    }

    public BakedAnimatedMesh getCombinedMeshForSubMeshes(List<String> subMeshes){
        List<BoneMFNode> meshNodes = getRootNode().getNodesOfType(BoneMFAttribute.AttributeTypes.MESH);
        return (BakedAnimatedMesh) bakeCombinedMesh(meshNodes.stream().filter(
                (x) -> subMeshes.contains(x.getName())).collect(Collectors.toList()));
    }

    public List<String> getFeetMeshes() {
        return feetMeshes;
    }

    public List<String> getBodyMeshes() {
        return bodyMeshes;
    }

    public List<String> getHeadMeshes() {
        return headMeshes;
    }

    public List<String> getLegMeshes() {
        return legMeshes;
    }
}

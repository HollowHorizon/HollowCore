package ru.hollowhorizon.hc.client.models.core.bonemf;


import java.lang.ref.WeakReference;

public class BoneMFAttribute {
    private final WeakReference<BoneMFNode> owner;

    public enum AttributeTypes {
        NULL,
        MESH,
        SKELETON
    }

    public static AttributeTypes getAttributeTypeFromString(String attrType){
        switch (attrType){
            case "mesh":
                return AttributeTypes.MESH;
            case "skeleton":
                return AttributeTypes.SKELETON;
            default:
                return AttributeTypes.NULL;
        }
    }

    public static String getAttributeNameFromType(AttributeTypes type){
        switch (type){
            case MESH:
                return "mesh";
            case SKELETON:
                return "skeleton";
            case NULL:
            default:
                return "null";
        }
    }

    private final AttributeTypes type;

    public AttributeTypes getType() {
        return type;
    }

    public BoneMFNode getOwner() {
        return owner.get();
    }

    @Override
    public String toString() {
        return String.format("<BoneMFAttribute type='%s'>", getAttributeNameFromType(type));
    }

    public BoneMFAttribute(AttributeTypes type, BoneMFNode owner){
        this.type = type;
        this.owner = new WeakReference<>(owner);
    }

}

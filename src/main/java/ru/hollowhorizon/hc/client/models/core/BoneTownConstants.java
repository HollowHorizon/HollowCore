package ru.hollowhorizon.hc.client.models.core;


public class BoneTownConstants {

    public static String BASE_DIR = "bonetown";
    public static String MODELS_DIR_NAME = "models";
    public static String ANIMATIONS_DIR_NAME = "animations";
    public static String BONETOWN_MODELS_DIR = MODELS_DIR_NAME;
    public static String BONETOWN_ANIMATIONS_DIR = ANIMATIONS_DIR_NAME;



    public enum MeshTypes {
        INVALID,
        BONEMF
    }

    public static String stringFromMeshType(MeshTypes type){
        switch (type) {
            case BONEMF:
                return "bonemf";
            case INVALID:
            default:
                return "invalid";
        }
    }
}

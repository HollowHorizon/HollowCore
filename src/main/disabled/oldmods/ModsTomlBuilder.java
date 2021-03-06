package ru.hollowhorizon.hc.common.oldmods;

public class ModsTomlBuilder {
    public static String buildToml(String modid, String displayName, String author) {
        return "modLoader = \"hollowforge\"\n" +
                "loaderVersion = \"[36,)\"\n" +
                "license = \"All rights reserved\"\n" +
                "credits = \""+author+"\"\n" +
                "[[mods]]\n" +
                "modId = \""+modid+"\"\n" +
                "version = \"${file.jarVersion}\"\n" +
                "displayName = \""+displayName+"\"\n" +
                "description = '''\n" +
                "Autogenerated description for "+displayName+" mod from 1.7.10-1.12.2" +
                "'''\n" +
                "[[dependencies."+modid+"]]\n" +
                "modId = \"forge\"\n" +
                "mandatory = true\n" +
                "versionRange = \"[36,)\"\n" +
                "ordering = \"NONE\"\n" +
                "side = \"BOTH\"\n" +
                "[[dependencies."+modid+"]]\n" +
                "modId = \"minecraft\"\n" +
                "mandatory = true\n" +
                "versionRange = \"[1.16.5,1.17)\"\n" +
                "ordering = \"NONE\"\n" +
                "side = \"BOTH\"\n";
    }
}

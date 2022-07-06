package ru.hollowhorizon.hc.common.oldmods;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.StringUtils;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class OldModsLocator extends AbstractJarFileLocator {
    private final Path modFolder;
    private final String customName;

    public OldModsLocator() {
        this(FMLPaths.GAMEDIR.get().resolve("old_mods"));
    }

    OldModsLocator(Path modFolder) {
        this(modFolder, "old mods folder");
    }

    OldModsLocator(Path modFolder, String name) {
        this.modFolder = modFolder;
        this.customName = name;

    }

    public List<IModFile> scanMods() {
        HollowCore.LOGGER.info("Scanning mods dir {} for mods", this.modFolder);

        List<Path> excluded = ModDirTransformerDiscoverer.allExcluded();

        List<IModFile> MODS = LamdbaExceptionUtils.uncheck(() -> Files.list(this.modFolder))
                .filter((p) -> !excluded.contains(p))
                .sorted(Comparator.comparing((path) -> StringUtils.toLowerCase(path.getFileName().toString())))
                .filter((p) -> StringUtils.toLowerCase(p.getFileName().toString()).endsWith(".jar") || StringUtils.toLowerCase(p.getFileName().toString()).endsWith(".zip"))
                .map((p) -> {
                    System.out.println(p.getFileName());
                    return ModFile.newFMLInstance(p, this);
                })
                .peek((f) -> this.modJars.compute(f, (mf, fs) -> this.createFileSystem(mf))).collect(Collectors.toList());

        preInit(MODS);

        return MODS;
    }

    @Override
    public Path findPath(IModFile modFile, String... path) {
        if (path[0].equals("META-INF") && path[1].equals("mods.toml")) {
            try {
                File tomlFile = modFile.getFilePath().getParent().resolve("mod-data").resolve(modFile.getFileName() + "@mods.toml").toFile();

                if (!tomlFile.exists()) {
                    String modInfo = IOUtils.toString(HollowJavaUtils.getFileFromJar(modFile.getFilePath()), StandardCharsets.UTF_8);
                    JsonObject jsonObject = new JsonParser().parse(modInfo).getAsJsonArray().get(0).getAsJsonObject();

                    String modid = jsonObject.get("modid").getAsString().toLowerCase(Locale.ROOT);
                    if (modid.equals("")) modid = randomString();
                    String modName = jsonObject.get("name").getAsString();
                    if (modName.equals("")) modName = "Unnamed Mod";
                    String author = jsonObject.get("credits").getAsString();
                    if (author.equals("")) author = "Unnamed";
                    String modToml = ModsTomlBuilder.buildToml(modid, modName, author);

                    FileUtils.writeStringToFile(tomlFile, modToml, StandardCharsets.UTF_8);
                }
                return tomlFile.toPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.findPath(modFile, path);
    }

    private void preInit(List<IModFile> MODS) {
    }

    public String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    public String name() {
        return this.customName;
    }

    public String toString() {
        return "{" + this.customName + " locator at " + this.modFolder + "}";
    }

    public void initArguments(Map<String, ?> arguments) {
    }
}

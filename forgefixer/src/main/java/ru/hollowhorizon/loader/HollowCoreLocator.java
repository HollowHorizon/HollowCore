package ru.hollowhorizon.loader;

import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class HollowCoreLocator extends AbstractJarFileModLocator {

    @Override
    public Stream<Path> scanCandidates() {
        return Stream.of(Path.of("C:\\Users\\Artem\\Modding\\HollowCore\\merged\\HollowCore-forge-1.20.1-2.1.2b.jar"));
    }

    @Override
    public String name() {
        return "HollowCore Loader";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }
}

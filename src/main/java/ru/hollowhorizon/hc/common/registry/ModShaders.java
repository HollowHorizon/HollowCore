package ru.hollowhorizon.hc.common.registry;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.IOException;
import java.util.function.Predicate;

public class ModShaders implements ISelectiveResourceReloadListener {

    public static void init(IResourceManager mgr) throws IOException {

    }


    public void clear() {
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        this.clear();
        try {
            init(resourceManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

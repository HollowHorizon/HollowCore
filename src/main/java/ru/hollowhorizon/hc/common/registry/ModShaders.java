package ru.hollowhorizon.hc.common.registry;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import ru.hollowhorizon.hc.client.render.shader.ShaderProgram;

import java.io.IOException;
import java.util.function.Predicate;

public class ModShaders implements ISelectiveResourceReloadListener {
    public static ShaderProgram BLOOM;

    public static void init(IResourceManager mgr) throws IOException {
        //BLOOM = new ShaderProgram("hc:shaders/null.vert", "hc:shaders/bloom.frag");
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

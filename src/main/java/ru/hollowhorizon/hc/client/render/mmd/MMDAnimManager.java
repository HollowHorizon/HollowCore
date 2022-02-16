package ru.hollowhorizon.hc.client.render.mmd;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.dll.HollowRenderManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class MMDAnimManager {
    static HollowRenderManager renderer;
    static Map<String, Long> animStatic;
    static Map<IHollowModel, Map<String, Long>> animModel;

    public static void init() {
        renderer = HollowRenderManager.getInstance();
        animStatic = new HashMap<>();
        animModel = new HashMap<>();

        HollowJavaUtils.unpackZipFromJar(new ResourceLocation(MODID, "models/animations/default.zip"));
    }

    public static long getAnimStatic(IHollowModel model, String animName) {
        String filename = getAnimationFilename(model.getModelDir(), animName);
        Long result = animStatic.get(filename);
        if (result == null) {
            long anim = renderer.LoadAnimation(model.getModelLong(), filename);
            if (anim == 0)
                return 0;
            result = anim;
            animStatic.put(filename, result);
        }
        return result;
    }

    public static void addModel(IHollowModel model) {
        animModel.put(model, new HashMap<>());
    }

    public static void deleteModel(IHollowModel model) {
        Collection<Long> arr = animModel.get(model).values();
        for (Long i : arr)
            renderer.DeleteAnimation(i);
        animModel.remove(model);
    }

    public static long getAnimModel(IHollowModel model, String animName) {
        String filename = getAnimationFilename(model.getModelDir(), animName);
        Map<String, Long> sub = animModel.get(model);
        Long result = sub.get(filename);
        if (result == null) {
            long anim = renderer.LoadAnimation(model.getModelLong(), filename);
            if (anim == 0)
                return 0;
            result = anim;
            sub.put(filename, result);
        }
        return result;
    }

    public static void deleteAll() {
        for (Long i : animStatic.values())
            renderer.DeleteAnimation(i);
    }

    static String getAnimationFilename(String modelDir, String animName) {
        modelDir = Paths.get(modelDir).getParent().toAbsolutePath().toString().concat("\\animations\\");
        File animFilename = new File(modelDir, animName + ".vmd");

        if (!animFilename.isFile()) {
            animFilename = new File(Minecraft.getInstance().gameDirectory, "config\\hollowcore\\cache\\default\\animations\\" + animName + ".vmd");
        }

        return animFilename.getAbsolutePath();
    }
}

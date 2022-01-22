package ru.hollowhorizon.hc.client.render.mmd;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import ru.hollowhorizon.hc.client.render.models.IHollowModel;

import java.io.File;
import java.util.*;

public class MMDModelManager {
    static Map<Entity, Model> models;
    static Map<String, Stack<IHollowModel>> modelPool;
    static long prevTime;

    public static void init() {
        models = new HashMap<>();
        modelPool = new HashMap<>();
        prevTime = System.currentTimeMillis();
    }

    public static IHollowModel loadModel(String modelName, long layerCount) {
        //Model path
        File modelDir = new File(Minecraft.getInstance().gameDirectory, "config\\hollowcore\\cache\\" + modelName + "\\model\\");
        String modelDirStr = modelDir.getAbsolutePath();

        String modelFilenameStr;
        boolean isPMD;
        File pmxModelFilename = new File(modelDir, "model.pmx");
        if (pmxModelFilename.isFile()) {
            modelFilenameStr = pmxModelFilename.getAbsolutePath();
            isPMD = false;
        } else {
            File pmdModelFilename = new File(modelDir, "model.pmd");
            if (pmdModelFilename.isFile()) {
                modelFilenameStr = pmdModelFilename.getAbsolutePath();
                isPMD = true;
            } else {
                return null;
            }
        }

        return MMDModel.create(modelFilenameStr, modelDirStr, isPMD, layerCount);
    }

    public static MMDModelManager.Model getModelOrInPool(Entity entity, String modelName, boolean isPlayer) {
        Model model = MMDModelManager.getModel(entity);
        if (model == null) {
            //First check if modelPool has model.
            IHollowModel m = getModelFromPool(modelName);
            if (m != null) {
                addModel(entity, m, modelName);
                model = getModel(entity);
                return model;
            }

            //Load model from file.
            m = loadModel(modelName, isPlayer ? 3 : 1);
            if (m == null)
                return null;

            //Regist Animation user because its a new model
            MMDAnimManager.addModel(m);

            addModel(entity, m, modelName);
            model = getModel(entity);
        }
        return model;
    }

    public static Model getModel(Entity entity) {
        return models.get(entity);
    }

    public static IHollowModel getModelFromPool(String modelName) {
        Stack<IHollowModel> pool = modelPool.get(modelName);
        if (pool == null)
            return null;
        if (pool.empty())
            return null;
        else
            return pool.pop();
    }

    public static void addModel(Entity entity, IHollowModel model, String modelName) {

        ModelWithEntityState m = new ModelWithEntityState();
        m.entity = entity;
        m.model = model;
        m.playCustomAnim = false;
        m.modelName = modelName;
        m.unuseTime = 0;
        m.state = MMDModelManager.EntityState.Idle;
        model.resetPhysics();
        model.changeAnim(MMDAnimManager.getAnimModel(model, "idle"), 0);
        models.put(entity, m);

    }

    public static void update() {
        long deltaTime = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();

        List<Entity> waitForDelete = new LinkedList<>();
        for (Model i : models.values()) {
            i.unuseTime += deltaTime;
            if (i.unuseTime > 10000) {
                tryModelToPool(i);
                waitForDelete.add(i.entity);
            }
        }

        for (Entity i : waitForDelete)
            models.remove(i);
    }

    public static void reloadModel() {
        for (Model i : models.values())
            deleteModel(i);
        models = new HashMap<>();
        for (Stack<IHollowModel> i : modelPool.values()) {
            for (IHollowModel j : i) {
                MMDModel.delete((MMDModel) j);

                MMDAnimManager.deleteModel(j);
            }
        }
        modelPool = new HashMap<>();
    }

    static void deleteModel(Model model) {
        MMDModel.delete((MMDModel) model.model);

        //Unregist animation user
        MMDAnimManager.deleteModel(model.model);
    }

    static void tryModelToPool(Model model) {
        if (modelPool.size() > 20) {
            deleteModel(model);
        } else {
            Stack<IHollowModel> pool = modelPool.computeIfAbsent(model.modelName, k -> new Stack<>());
            pool.push(model.model);
        }
    }

    public enum EntityState {Idle, Walk, Swim, Ridden}

    public static class Model {
        public Entity entity;
        public IHollowModel model;
        public String modelName;
        public long unuseTime;
    }

    public static class ModelWithEntityState extends Model {
        public EntityState state;
        public boolean playCustomAnim;
    }
}

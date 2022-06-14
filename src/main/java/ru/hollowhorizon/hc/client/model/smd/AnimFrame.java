package ru.hollowhorizon.hc.client.model.smd;

import net.minecraft.util.math.vector.Matrix4f;
import ru.hollowhorizon.hc.client.utils.math.MatrixUtils;
import ru.hollowhorizon.hc.client.utils.math.VectorHelper;

import java.util.ArrayList;

public class AnimFrame {
    public final int ID;
    public ArrayList<Matrix4f> transforms = new ArrayList<>();
    public ArrayList<Matrix4f> invertTransforms = new ArrayList<>();
    public SmdAnimation owner;

    public AnimFrame(AnimFrame anim, SmdAnimation parent) {
        this.owner = parent;
        this.ID = anim.ID;
        this.transforms = anim.transforms;
        this.invertTransforms = anim.invertTransforms;
    }

    public AnimFrame(SmdAnimation parent) {
        this.owner = parent;
        this.ID = parent.requestFrameID();
    }

    public void addTransforms(int index, Matrix4f invertedData) {
        this.transforms.add(index, invertedData);

        this.invertTransforms.add(index, MatrixUtils.invert(invertedData, null));
    }

    public void fixUp(int id, float degrees) {
        float radians = (float) Math.toRadians(degrees);
        Matrix4f rotator = VectorHelper.matrix4FromLocRot(0.0F, 0.0F, 0.0F, radians, 0.0F, 0.0F);
        MatrixUtils.mul(rotator, this.transforms.get(id), this.transforms.get(id));
        MatrixUtils.mul(MatrixUtils.invert(rotator, null), this.invertTransforms.get(id), this.invertTransforms.get(id));
    }

    public void reform() {
        for (int i = 0; i < this.transforms.size(); ++i) {
            Bone bone = this.owner.bones.get(i);
            if (bone.parent != null) {
                Matrix4f temp = MatrixUtils.mul(this.transforms.get(bone.parent.ID), this.transforms.get(i), null);
                this.transforms.set(i, temp);
                this.invertTransforms.set(i, MatrixUtils.invert(temp, null));
            }
        }

    }
}

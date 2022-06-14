package ru.hollowhorizon.hc.client.model.smd;

import net.minecraft.util.math.vector.Matrix4f;
import ru.hollowhorizon.hc.client.model.animations.EnumGeomData;
import ru.hollowhorizon.hc.client.model.animations.IModulized;
import ru.hollowhorizon.hc.client.utils.math.MatrixUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bone implements IModulized {
    public Bone copy = null;
    public String name;
    public int ID;
    public Bone parent;
    public SmdModel owner;
    private Boolean isDummy;
    public Matrix4f rest;
    public Matrix4f restInverted;
    public Matrix4f modified = new Matrix4f();
    public Matrix4f difference = new Matrix4f();
    public Matrix4f prevInverted = new Matrix4f();
    public ArrayList<Bone> children = new ArrayList<>();
    public HashMap<DeformVertex, Float> verts = new HashMap<>();
    public HashMap<String, HashMap<Integer, Matrix4f>> animatedTransforms = new HashMap<>();

    public Bone(String name, int ID, Bone parent, SmdModel owner) {
        this.name = name;
        this.ID = ID;
        this.parent = parent;
        this.owner = owner;
    }

    public Bone(Bone b, Bone parent, SmdModel owner) {
        this.name = b.name;
        this.ID = b.ID;
        this.owner = owner;
        this.parent = parent;

        for (Map.Entry<DeformVertex, Float> vertices : b.verts.entrySet()) {

            this.verts.put(owner.verts.get(vertices.getKey().ID), vertices.getValue());
        }

        this.animatedTransforms = new HashMap<>(b.animatedTransforms);
        this.restInverted = b.restInverted;
        this.rest = b.rest;
        b.copy = this;
    }

    public void setChildren(Bone b, ArrayList<Bone> bones) {
        for(int i = 0; i < b.children.size(); ++i) {
            Bone child = b.children.get(i);
            this.children.add(bones.get(child.ID));
            bones.get(child.ID).parent = this;
        }

    }

    public boolean isDummy() {
        return this.isDummy == null ? (this.isDummy = this.parent == null && this.children.isEmpty()) : this.isDummy;
    }

    public void setRest(Matrix4f resting) {
        this.rest = resting;
    }

    public void addChild(Bone child) {
        this.children.add(child);
    }

    public void addVertex(DeformVertex v, float weight) {
        if (this.name.equals("blender_implicit")) {
            throw new UnsupportedOperationException("NO.");
        } else {
            this.verts.put(v, weight);
        }
    }

    private void reform(Matrix4f parentMatrix) {
        this.rest = MatrixUtils.mul(parentMatrix, this.rest, null);
        if (ValveStudioModel.debugModel) {
            System.out.println(this.name + ' ' + this.rest);
        }

        this.reformChildren();
    }

    public void reformChildren() {

        for (Bone child : this.children) {
            child.reform(this.rest);
        }

    }

    public void invertRestMatrix() {
        this.restInverted = MatrixUtils.invert(this.rest, null);
    }

    public void reset() {
        this.modified.setIdentity();
    }

    public void preloadAnimation(AnimFrame key, Matrix4f animated) {
        HashMap<Integer, Matrix4f> precalcArray;
        if (this.animatedTransforms.containsKey(key.owner.animationName)) {
            precalcArray = this.animatedTransforms.get(key.owner.animationName);
        } else {
            precalcArray = new HashMap<>();
        }

        precalcArray.put(key.ID, animated);
        this.animatedTransforms.put(key.owner.animationName, precalcArray);
    }

    public void setModified() {
        Matrix4f realInverted;
        Matrix4f real;
        if (this.owner.owner.hasAnimations() && this.owner.currentAnim != null) {
            AnimFrame currentFrame = this.owner.currentAnim.frames.get(this.owner.currentAnim.currentFrameIndex);
            realInverted = currentFrame.transforms.get(this.ID);
            real = currentFrame.invertTransforms.get(this.ID);
        } else {
            realInverted = this.rest;
            real = this.restInverted;
        }

        Matrix4f delta = new Matrix4f();
        Matrix4f absolute = new Matrix4f();
        MatrixUtils.mul(realInverted, real, delta);
        this.modified = this.parent != null ? MatrixUtils.mul(this.parent.modified, delta, this.initModified()) : delta;
        MatrixUtils.mul(real, this.modified, absolute);
        MatrixUtils.invert(absolute, this.prevInverted);
        this.children.forEach(Bone::setModified);
    }

    protected Matrix4f initModified() {
        return this.modified == null ? (this.modified = new Matrix4f()) : this.modified;
    }

    public void applyModified() {
        AnimFrame currentFrame = this.owner.currentFrame();
        if (currentFrame != null) {
            HashMap<Integer, Matrix4f> precalcArray = this.animatedTransforms.get(currentFrame.owner.animationName);
            Matrix4f animated = precalcArray.get(currentFrame.ID);
            Matrix4f animatedChange = new Matrix4f();
            MatrixUtils.mul(animated, this.restInverted, animatedChange);
            this.modified = this.modified == null ? animatedChange : MatrixUtils.mul(this.modified, animatedChange, this.modified);
        }

        for (Map.Entry<DeformVertex, Float> vertex : this.verts.entrySet()) {
            vertex.getKey().applyModified(this, vertex.getValue());
        }

        this.reset();
    }

    public float setValue(float value, EnumGeomData d) {
        return value;
    }

    public float getValue(EnumGeomData d) {
        return 0.0F;
    }
}

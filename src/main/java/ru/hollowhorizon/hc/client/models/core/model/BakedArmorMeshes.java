package ru.hollowhorizon.hc.client.models.core.model;

import net.minecraft.util.ResourceLocation;

public class BakedArmorMeshes {

    private final ResourceLocation name;
    private final BakedAnimatedMesh head;
    private final BakedAnimatedMesh body;
    private final BakedAnimatedMesh legs;
    private final BakedAnimatedMesh feet;

    public BakedArmorMeshes(ResourceLocation name,
                            BakedAnimatedMesh head,
                            BakedAnimatedMesh body,
                            BakedAnimatedMesh legs,
                            BakedAnimatedMesh feet){
        this.name = name;
        this.head = head;
        this.body = body;
        this.legs = legs;
        this.feet = feet;
    }

    public ResourceLocation getName() {
        return name;
    }

    public BakedAnimatedMesh getBody() {
        return body;
    }

    public BakedAnimatedMesh getFeet() {
        return feet;
    }

    public BakedAnimatedMesh getHead() {
        return head;
    }

    public BakedAnimatedMesh getLegs() {
        return legs;
    }
}

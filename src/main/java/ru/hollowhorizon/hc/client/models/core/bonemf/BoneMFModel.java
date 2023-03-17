package ru.hollowhorizon.hc.client.models.core.bonemf;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.model.BakedAnimatedMesh;
import ru.hollowhorizon.hc.client.models.core.model.BakedMesh;
import ru.hollowhorizon.hc.client.utils.math.Matrix4d;
import ru.hollowhorizon.hc.client.utils.math.Vector4d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BoneMFModel {
    public static int MAX_WEIGHTS = 4;
    private final BoneMFNode rootNode;
    private final boolean hasSkeleton;
    private final BoneMFSkeleton skeleton;
    private final ResourceLocation name;


    public BoneMFModel(ResourceLocation name, BoneMFNode rootNode) {
        this.rootNode = rootNode;
        this.name = name;
        BoneMFNode skeletonRoot = rootNode.getNodeWithAttributeType(BoneMFAttribute.AttributeTypes.SKELETON);
        if (skeletonRoot != null) {
            HollowCore.LOGGER.info("Found skeleton root for {}: {}",
                    rootNode.getName(), skeletonRoot.toString());
            hasSkeleton = true;
            skeleton = new BoneMFSkeleton(skeletonRoot);

        } else {
            hasSkeleton = false;
            HollowCore.LOGGER.warn("Found no skeleton for {}", rootNode.getName());
            skeleton = null;
        }
    }

    public ResourceLocation getName() {
        return name;
    }

    public Optional<BoneMFSkeleton> getSkeleton() {
        return Optional.ofNullable(skeleton);
    }

    public boolean hasSkeleton() {
        return hasSkeleton;
    }

    public List<BakedAnimatedMesh> getBakeAsAnimatedMeshes() {
        List<BakedMesh> meshes = bakeMeshes();
        List<BakedAnimatedMesh> animated = new ArrayList<>();
        for (BakedMesh mesh : meshes) {
            if (mesh instanceof BakedAnimatedMesh) {
                animated.add((BakedAnimatedMesh) mesh);
            }
        }
        return animated;
    }

    public BakedAnimatedMesh getCombinedAnimatedMesh() {
        BakedMesh mesh = bakeCombinedMesh();
        return (BakedAnimatedMesh) mesh;
    }

    public BakedMesh bakeCombinedMesh() {
        List<BoneMFNode> meshNodes = getRootNode().getNodesOfType(BoneMFAttribute.AttributeTypes.MESH);
        return bakeCombinedMesh(meshNodes);
    }

    public BakedMesh bakeCombinedMesh(List<BoneMFNode> meshNodes) {
        List<Float> positions = new ArrayList<>();
        List<Float> uvs = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> boneIds = new ArrayList<>();
        List<Float> boneWeights = new ArrayList<>();
        List<Integer> finalTriangles = new ArrayList<>();
        int offset = 0;
        for (BoneMFNode meshNode : meshNodes) {
            HollowCore.LOGGER.info("Baking Combined Mesh, Adding Mesh Node: {}", meshNode.getName());
            BoneMFMeshAttribute meshAttribute = meshNode.getMesh();
            if (meshAttribute == null) {
                HollowCore.LOGGER.warn("Failed to find mesh attribute for {}, skipping bake.",
                        meshNode.getName());
                continue;
            }
            List<Integer> triangles = meshAttribute.getTriangles();
            List<BoneMFVertex> vertices = meshAttribute.getVertices();
            for (int index : triangles) {
                finalTriangles.add(index + offset);
            }
            Matrix4d transform = meshNode.calculateGlobalTransform();
            for (BoneMFVertex vertex : vertices) {
                Vector4d posVec = new Vector4d(vertex.x, vertex.y, vertex.z, 1.0);
                posVec.mulAffine(transform, posVec);
                Vector4d normVec = new Vector4d(vertex.nX, vertex.nY, vertex.nZ, 0.0);
                normVec.mul(transform, normVec);
                positions.add((float) posVec.x());
                positions.add((float) posVec.y());
                positions.add((float) posVec.z());
                uvs.add((float) vertex.u);
                uvs.add((float) vertex.v);
                normals.add((float) normVec.x());
                normals.add((float) normVec.y());
                normals.add((float) normVec.z());
            }
            offset += vertices.size();
            if (getSkeleton().isPresent()) {
                BoneMFSkeleton skeleton = getSkeleton().get();

                for (BoneMFVertex vertex : vertices) {
                    int size = vertex.boneWeights.size();

                    for (int i = 0; i < MAX_WEIGHTS; i++) {
                        if (i < size) {
                            Tuple<String, Double> weight = vertex.boneWeights.get(i);
                            boneWeights.add(weight.getB().floatValue());
                            boneIds.add(skeleton.getBoneId(weight.getA()));
                        } else {
                            boneWeights.add(0.0f);
                            boneIds.add(0);
                        }
                    }
                }
            }
        }
        BakedMesh mesh;
        if (getSkeleton().isPresent()) {
            mesh = new BakedAnimatedMesh(name.toString(), positions, uvs, normals, finalTriangles, boneWeights, boneIds);
        } else {
            mesh = new BakedMesh(name.toString(), positions, uvs, normals, finalTriangles);
        }
        return mesh;
    }


    public List<BakedMesh> bakeMeshes() {
        List<BakedMesh> meshes = new ArrayList<>();
        List<BoneMFNode> meshNodes = getRootNode().getNodesOfType(BoneMFAttribute.AttributeTypes.MESH);
        for (BoneMFNode meshNode : meshNodes) {
            BoneMFMeshAttribute meshAttribute = meshNode.getMesh();
            if (meshAttribute == null) {
                HollowCore.LOGGER.warn("Failed to find mesh attribute for {}, skipping bake.",
                        meshNode.getName());
                continue;
            }
            List<Integer> triangles = meshAttribute.getTriangles();
            List<BoneMFVertex> vertices = meshAttribute.getVertices();
            List<Float> positions = new ArrayList<>();
            List<Float> uvs = new ArrayList<>();
            List<Float> normals = new ArrayList<>();
            Matrix4d transform = meshNode.calculateGlobalTransform();
            for (BoneMFVertex vertex : vertices) {
                Vector4d posVec = new Vector4d(vertex.x, vertex.y, vertex.z, 1.0);
                posVec.mulAffine(transform, posVec);
                Vector4d normVec = new Vector4d(vertex.nX, vertex.nY, vertex.nZ, 0.0);
                normVec.mul(transform, normVec);
                positions.add((float) posVec.x());
                positions.add((float) posVec.y());
                positions.add((float) posVec.z());
                uvs.add((float) vertex.u);
                uvs.add((float) vertex.v);
                normals.add((float) normVec.x());
                normals.add((float) normVec.y());
                normals.add((float) normVec.z());
            }
            BakedMesh mesh;
            if (getSkeleton().isPresent()) {
                BoneMFSkeleton skeleton = getSkeleton().get();
                List<Integer> boneIds = new ArrayList<>();
                List<Float> boneWeights = new ArrayList<>();
                for (BoneMFVertex vertex : vertices) {
                    int size = vertex.boneWeights.size();
                    for (int i = 0; i < MAX_WEIGHTS; i++) {
                        if (i < size) {
                            Tuple<String, Double> weight = vertex.boneWeights.get(i);
                            boneWeights.add(weight.getB().floatValue());
                            boneIds.add(skeleton.getBoneId(weight.getA()));
                        } else {
                            boneWeights.add(0.0f);
                            boneIds.add(0);
                        }
                    }
                }
                mesh = new BakedAnimatedMesh(meshNode.getName(), positions, uvs, normals, triangles, boneWeights, boneIds);
            } else {
                mesh = new BakedMesh(meshNode.getName(), positions, uvs, normals, triangles);
            }
            meshes.add(mesh);
        }
        return meshes;
    }

    public BoneMFNode getRootNode() {
        return rootNode;
    }
}

package engine.scene.model;

import org.joml.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import engine.graph.model.*;
import engine.graph.texture.TextureCache;
import engine.util.Util;

import java.io.File;
import java.io.IOException;
import java.lang.Math;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    public static final int MAX_BONES = 128;
    public static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    public static final int DEFAULT_FLAGS = aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices |
                                            aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace |
                                            aiProcess_LimitBoneWeights;

    public static Model loadModel(String modelID, String modelPath, TextureCache textureCache, boolean animation) throws IOException {
        return loadModel(modelID, modelPath, textureCache, DEFAULT_FLAGS, animation);
    }

    public static Model loadModel(String modelID, String modelPath, TextureCache textureCache, int flags, boolean animation) throws IOException {
        if (!animation) flags |= aiProcess_PreTransformVertices;

        File modelFile = new File(modelPath);
        if (!modelFile.exists()) throw new IOException("Failed to load model at path: \"" + modelPath + "\"");

        String modelDir = modelFile.getParent();

        AIScene aiScene = aiImportFile(modelPath, flags);
        if (aiScene == null) throw new IOException("Failed to load model: \"" + modelPath + "\"");

        int numMaterials = aiScene.mNumMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(Objects.requireNonNull(aiScene.mMaterials()).get(i));
            materials.add(processMaterial(aiMaterial, textureCache, modelDir));
        }

        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Material defaultMaterial = new Material();
        List<Bone> bones = new ArrayList<>();
        for (int i = 0; i < numMeshes; i++) {
            assert aiMeshes != null;
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh, bones);
            int materialIndex = aiMesh.mMaterialIndex();
            Material material;
            if (materialIndex >= 0 && materialIndex < materials.size()) material = materials.get(materialIndex);
            else material = defaultMaterial;
            material.getMeshes().add(mesh);
        }

        if (!defaultMaterial.getMeshes().isEmpty()) materials.add(defaultMaterial);

        List<Model.Animation> animations = new ArrayList<>();
        int numAnimations = aiScene.mNumAnimations();
        if (numAnimations > 0) {
            Node rootNode = buildNodesTree(aiScene.mRootNode(), null);
            Matrix4f globalInverseTransform = toMatrix(aiScene.mRootNode().mTransformation()).invert();
            animations = processAnimations(aiScene, bones, rootNode, globalInverseTransform);
        }

        aiReleaseImport(aiScene);

        return new Model(modelID, materials, animations);
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Bone> bones) {
        float[] vertices = processVertices(aiMesh);
        float[] texCoords = processTexCoords(aiMesh);
        int[] indices = processIndices(aiMesh);
        float[] normals = processNormals(aiMesh);
        float[] tangents = processTangents(aiMesh, normals);
        float[] bitangents = processBitangents(aiMesh, normals);
        AnimMeshData animMeshData = processBones(aiMesh, bones);

        // texture coordinates may not be populated
        if (texCoords.length == 0) {
            int numElements = (vertices.length / 3) * 2;
            texCoords = new float[numElements];
        }

        return new Mesh(vertices, texCoords, indices, normals, tangents, bitangents, animMeshData.boneIDs, animMeshData.weights);
    }

    private static float[] processVertices(AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mVertices();
        float[] data = new float[buffer.remaining() * 3];
        int pos = 0;
        while (buffer.remaining() > 0) {
            AIVector3D textCoord = buffer.get();
            data[pos++] = textCoord.x();
            data[pos++] = textCoord.y();
            data[pos++] = textCoord.z();
        }
        return data;
    }

    private static float[] processTexCoords(AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mTextureCoords(0);
        if (buffer == null) return new float[] {};

        float[] data = new float[buffer.remaining() * 2];
        int pos = 0;
        while (buffer.remaining() > 0) {
            AIVector3D textCoord = buffer.get();
            data[pos++] = textCoord.x();
            data[pos++] = 1 - textCoord.y();
        }
        return data;
    }

    private static int[] processIndices(AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    private static float[] processNormals(AIMesh aiMesh) {
        AIVector3D.Buffer buf = aiMesh.mNormals();
        float[] data = new float[buf.remaining() * 3];
        int pos = 0;
        while (buf.remaining() > 0) {
            AIVector3D normal = buf.get();
            data[pos++] = normal.x();
            data[pos++] = normal.y();
            data[pos++] = normal.z();
        }
        return data;
    }

    private static float[] processTangents(AIMesh aiMesh, float[] normals) {
        AIVector3D.Buffer buf = aiMesh.mTangents();
        float[] data = new float[buf.remaining() * 3];
        int pos = 0;
        while(buf.remaining() > 0) {
            AIVector3D aiTangent = buf.get();
            data[pos++] = aiTangent.x();
            data[pos++] = aiTangent.y();
            data[pos++] = aiTangent.z();
        }

        if (data.length == 0) data = new float[normals.length];
        return data;
    }

    private static float[] processBitangents(AIMesh aiMesh, float[] normals) {
        AIVector3D.Buffer buf = aiMesh.mBitangents();
        float[] data = new float[buf.remaining() * 3];
        int pos = 0;
        while (buf.remaining() > 0) {
            AIVector3D aiBitangent = buf.get();
            data[pos++] = aiBitangent.x();
            data[pos++] = aiBitangent.y();
            data[pos++] = aiBitangent.z();
        }

        if (data.length == 0) data = new float[normals.length];
        return data;
    }

    private static AnimMeshData processBones(AIMesh aiMesh, List<Bone> bones) {
        List<Integer> boneIDs = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        int numBones = aiMesh.mNumBones();
        PointerBuffer aiBones = aiMesh.mBones();
        for (int i = 0; i < numBones; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));
            int id = bones.size();
            Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
            bones.add(bone);
            int numWeights = aiBone.mNumWeights();
            AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                AIVertexWeight aiWeight = aiWeights.get(j);
                VertexWeight vw = new VertexWeight(bone.id(), aiWeight.mVertexId(), aiWeight.mWeight());
                List<VertexWeight> vws = weightSet.computeIfAbsent(vw.vertexID(), k -> new ArrayList<>());
                vws.add(vw);
            }
        }

        int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            List<VertexWeight> vws = weightSet.get(i);
            int size = vws != null ? vws.size() : 0;
            for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
                if (j < size) {
                    VertexWeight vw = vws.get(j);
                    weights.add(vw.weight());
                    boneIDs.add(vw.boneID());
                }
                else {
                    weights.add(0.0f);
                    boneIDs.add(0);
                }
            }
        }

        return new AnimMeshData(Util.listToFloatArray(weights), Util.listToIntArray(boneIDs));
    }

    private static List<Model.Animation> processAnimations(AIScene aiScene, List<Bone> bones, Node root, Matrix4f globalInverseTransform) {
        List<Model.Animation> animations = new ArrayList<>();

        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));
            int maxFrames = calcAnimationMaxFrames(aiAnimation);

            List<Model.AnimatedFrame> frames = new ArrayList<>();
            Model.Animation animation = new Model.Animation(aiAnimation.mName().dataString(), aiAnimation.mDuration(), frames);
            animations.add(animation);

            for (int j = 0; j < maxFrames; j++) {
                Matrix4f[] boneMatrices = new Matrix4f[MAX_BONES];
                Arrays.fill(boneMatrices, IDENTITY_MATRIX);
                Model.AnimatedFrame frame = new Model.AnimatedFrame(boneMatrices);
                buildFrameMatrices(aiAnimation, bones, frame, j, root, root.getTransformation(), globalInverseTransform);
                frames.add(frame);
            }
        }

        return animations;
    }

    private static Material processMaterial(AIMaterial aiMaterial, TextureCache textureCache, String modelDir) {
        Material material = new Material();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            AIColor4D color = AIColor4D.create();

            // ambient color
            int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) material.setAmbientColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));

            // diffuse color
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));

            // specular color
            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
            if (result == aiReturn_SUCCESS) material.setSpecularColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));

            // specular exponent
            float reflectance = 0.0f;
            float[] glossinessFactor = new float[] { 0.0f };
            int[] pMax = new int[] { 1 };
            result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, glossinessFactor, pMax);
            if (result != aiReturn_SUCCESS) reflectance = glossinessFactor[0];
            material.setSpecular(reflectance);

            // normal map
            AIString aiNormalMapPath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, aiNormalMapPath, (IntBuffer) null, null, null, null, null, null);
            String normalMapPath = aiNormalMapPath.dataString();
            if (normalMapPath.length() > 0) {
                material.setNormalMapPath(modelDir + File.separator + new File(normalMapPath).getName());
                textureCache.createTexture(material.getNormalMapPath());
                material.setDiffuseColor(Material.DEFAULT_COLOR);
            }

            // texture
            AIString aiTexturePath = AIString.calloc(stack);
            aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, aiTexturePath, (IntBuffer) null, null, null, null, null, null);

            String texturePath = aiTexturePath.dataString();
            if (texturePath.length() > 0) {
                material.setTexturePath(modelDir + File.separator + new File(texturePath).getName());
                textureCache.createTexture(material.getTexturePath());
                material.setDiffuseColor(Material.DEFAULT_COLOR);
            }

            return material;
        }
    }

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());
        return result;
    }

    private static void buildFrameMatrices(AIAnimation aiAnimation, List<Bone> bones, Model.AnimatedFrame frame, int frameIndex, Node node, Matrix4f parentTransform, Matrix4f globalInverseTransform) {
        String nodeName = node.getName();
        AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
        Matrix4f nodeTransform = node.getTransformation();
        if (aiNodeAnim != null) nodeTransform = buildNodeTransformMatrix(aiNodeAnim, frameIndex);
        Matrix4f nodeGlobalTransform = new Matrix4f(parentTransform).mul(nodeTransform);

        List<Bone> affectedBones = bones.stream().filter(b -> b.name().equals(nodeName)).toList();
        for (Bone bone : affectedBones) {
            Matrix4f boneTransform = new Matrix4f(globalInverseTransform).mul(nodeGlobalTransform).mul(bone.offset());
            frame.boneMatrices()[bone.id()] = boneTransform;
        }

        for (Node child : node.getChildren()) {
            buildFrameMatrices(aiAnimation, bones, frame, frameIndex, child, nodeGlobalTransform, globalInverseTransform);
        }
    }

    private static Matrix4f buildNodeTransformMatrix(AINodeAnim aiNodeAnim, int frameIndex) {
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        AIVectorKey aiVectorKey;
        AIVector3D aiVectorValue;

        Matrix4f nodeTransform = new Matrix4f();
        int numPositions = aiNodeAnim.mNumPositionKeys();
        if (numPositions > 0) {
            aiVectorKey = positionKeys.get(Math.min(numPositions - 1, frameIndex));
            aiVectorValue = aiVectorKey.mValue();
            nodeTransform.translate(aiVectorValue.x(), aiVectorValue.y(), aiVectorValue.z());
        }

        int numRotations = aiNodeAnim.mNumRotationKeys();
        if (numRotations > 0) {
            AIQuatKey quaternionKey = rotationKeys.get(Math.min(numRotations - 1, frameIndex));
            AIQuaternion aiQuaternion = quaternionKey.mValue();
            Quaternionf quaternion = new Quaternionf(aiQuaternion.x(), aiQuaternion.y(), aiQuaternion.z(), aiQuaternion.w());
            nodeTransform.rotate(quaternion);
        }

        int numScalingKeys = aiNodeAnim.mNumScalingKeys();
        if (numScalingKeys > 0) {
            aiVectorKey = scalingKeys.get(Math.min(numScalingKeys - 1, frameIndex));
            aiVectorValue = aiVectorKey.mValue();
            nodeTransform.scale(aiVectorValue.x(), aiVectorValue.y(), aiVectorValue.z());
        }

        return nodeTransform;
    }

    private static Node buildNodesTree(AINode aiNode, Node parentNode) {
        String nodeName = aiNode.mName().dataString();
        Node node = new Node(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Node childNode = buildNodesTree(aiChildNode, node);
            node.addChild(childNode);
        }

        return node;
    }

    private static AINodeAnim findAIAnimNode(AIAnimation aiAnimation, String nodeName) {
        AINodeAnim result = null;
        int numAnimNodes = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i = 0; i < numAnimNodes; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            if (nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }
        return result;
    }

    private static int calcAnimationMaxFrames(AIAnimation aiAnimation) {
        int maxFrames = 0;
        int numNodeAnims = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i = 0; i < numNodeAnims; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            int numFrames = Math.max(Math.max(aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()), aiNodeAnim.mNumPositionKeys());
            maxFrames = Math.max(maxFrames, numFrames);
        }
        return maxFrames;
    }

    public record AnimMeshData(float[] weights, int[] boneIDs) {}
    private record Bone(int id, String name, Matrix4f offset) {}
    private record VertexWeight(int boneID, int vertexID, float weight) {}

}

package engine.scene.model;

import engine.graph.model.Material;
import engine.graph.model.Mesh;
import engine.graph.model.Model;
import engine.graph.render.TextureCache;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

public class ModelLoader {

    public static final int DEFAULT_FLAGS = aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices |
                                            aiProcess_Triangulate | aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace |
                                            aiProcess_LimitBoneWeights | aiProcess_PreTransformVertices;

    public static Model loadModel(String modelID, String modelPath, TextureCache textureCache) throws IOException {
        return loadModel(modelID, modelPath, textureCache, DEFAULT_FLAGS);
    }

    public static Model loadModel(String modelID, String modelPath, TextureCache textureCache, int flags) throws IOException {
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
        for (int i = 0; i < numMeshes; i++) {
            assert aiMeshes != null;
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh);
            int materialIndex = aiMesh.mMaterialIndex();
            Material material;
            if (materialIndex >= 0 && materialIndex < materials.size()) material = materials.get(materialIndex);
            else material = defaultMaterial;
            material.getMeshes().add(mesh);
        }

        if (!defaultMaterial.getMeshes().isEmpty()) materials.add(defaultMaterial);

        return new Model(modelID, materials);
    }

    private static Mesh processMesh(AIMesh aiMesh) {
        float[] vertices = processVertices(aiMesh);
        float[] texCoords = processTexCoords(aiMesh);
        int[] indices = processIndices(aiMesh);
        float[] normals = processNormals(aiMesh);

        // texture coordinates may not be populated
        if (texCoords.length == 0) {
            int numElements = (vertices.length / 3) * 2;
            texCoords = new float[numElements];
        }

        return new Mesh(vertices, texCoords, indices, normals);
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

            // specular
            float reflectance = 0.0f;
            float[] glossinessFactor = new float[] { 0.0f };
            int[] pMax = new int[] { 1 };
            result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, glossinessFactor, pMax);
            if (result != aiReturn_SUCCESS) reflectance = glossinessFactor[0];
            material.setSpecular(reflectance);

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

}

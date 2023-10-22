package engine.graph.model;

import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Material {

    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private Vector4f ambientColor;
    private Vector4f diffuseColor;
    private Vector4f specularColor;
    private float specular;
    private List<Mesh> meshes;
    private String texturePath;
    private String normalMapPath;

    public Material() {
        ambientColor = DEFAULT_COLOR;
        diffuseColor = DEFAULT_COLOR;
        specularColor = DEFAULT_COLOR;
        meshes = new ArrayList<>();
    }

    public void cleanup() {
        meshes.forEach(Mesh::cleanup);
    }

    public Vector4f getAmbientColor() { return ambientColor; }
    public Vector4f getDiffuseColor() { return diffuseColor; }
    public Vector4f getSpecularColor() { return specularColor; }
    public float getSpecular() { return specular; }
    public List<Mesh> getMeshes() { return meshes; }
    public String getTexturePath() { return texturePath; }
    public String getNormalMapPath() { return normalMapPath; }

    public void setAmbientColor(Vector4f color) { this.ambientColor = color; }
    public void setDiffuseColor(Vector4f color) { this.diffuseColor = color; }
    public void setSpecularColor(Vector4f color) { this.specularColor = color; }
    public void setSpecular(float reflectance) { this.specular = reflectance; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }
    public void setNormalMapPath(String normalMapPath) { this.normalMapPath = normalMapPath; }

}

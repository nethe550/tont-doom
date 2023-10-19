package engine.graph;

import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Material {

    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private Vector4f diffuseColor;
    private List<Mesh> meshes;
    private String texturePath;

    public Material() {
        diffuseColor = DEFAULT_COLOR;
        meshes = new ArrayList<>();
    }

    public void cleanup() {
        meshes.forEach(Mesh::cleanup);
    }

    public Vector4f getDiffuseColor() { return diffuseColor; }
    public List<Mesh> getMeshes() { return meshes; }
    public String getTexturePath() { return texturePath; }

    public void setDiffuseColor(Vector4f color) { this.diffuseColor = diffuseColor; }
    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }

}

package engine.graph;

import java.util.ArrayList;
import java.util.List;

public class Material {

    private List<Mesh> meshes;
    private String texturePath;

    public Material() {
        meshes = new ArrayList<>();
    }

    public void cleanup() {
        meshes.forEach(Mesh::cleanup);
    }

    public List<Mesh> getMeshes() { return meshes; }
    public String getTexturePath() { return texturePath; }

    public void setTexturePath(String texturePath) { this.texturePath = texturePath; }

}

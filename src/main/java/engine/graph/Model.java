package engine.graph;

import engine.scene.Entity;

import java.util.*;

public class Model {

    private final String id;
    private List<Entity> entities;
    private List<Mesh> meshes;

    public Model(String id, List<Mesh> meshes) {
        this.id = id;
        this.meshes = meshes;
        entities = new ArrayList<>();
    }

    public void cleanup() {
        meshes.forEach(Mesh::cleanup);
    }

    public List<Entity> getEntities() { return entities; }
    public String getID() { return id; }
    public List<Mesh> getMeshes() { return meshes; }

}

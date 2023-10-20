package engine.graph.model;

import engine.scene.model.Entity;

import java.util.*;

public class Model {

    private final String id;
    private List<Entity> entities;
    private List<Material> materials;

    public Model(String id, List<Material> materials) {
        this.id = id;
        entities = new ArrayList<>();
        this.materials = materials;
    }

    public void cleanup() {
        materials.forEach(Material::cleanup);
    }

    public List<Entity> getEntities() { return entities; }
    public String getID() { return id; }
    public List<Material> getMaterials() { return materials; }

}

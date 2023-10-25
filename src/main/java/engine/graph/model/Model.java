package engine.graph.model;

import engine.scene.model.Entity;
import org.joml.Matrix4f;

import java.util.*;

public class Model {

    private final String id;
    private List<Entity> entities;
    private List<Material> materials;
    private List<Animation> animations;

    public Model(String id, List<Material> materials, List<Animation> animations) {
        this.id = id;
        entities = new ArrayList<>();
        this.materials = materials;
        this.animations = animations;
    }

    public void cleanup() {
        materials.forEach(Material::cleanup);
    }

    public List<Entity> getEntities() { return entities; }
    public String getID() { return id; }
    public List<Material> getMaterials() { return materials; }
    public List<Animation> getAnimations() { return animations; }

    public record AnimatedFrame(Matrix4f[] boneMatrices) {}
    public record Animation(String name, double duration, List<AnimatedFrame> frames) {}

}

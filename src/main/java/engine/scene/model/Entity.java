package engine.scene.model;

import engine.graph.model.Model;
import engine.scene.Scene;
import org.joml.*;

public class Entity {

    private final String id;
    private final String modelID;
    private Matrix4f modelMatrix;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;
    private AnimationData animationData;

    public Entity(String id, String modelID) {
        this.id = id;
        this.modelID = modelID;
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
        scale = 1f;
    }

    public String getID() { return id; }
    public String getModelID() { return modelID; }
    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Vector3f getPosition() { return position; }
    public Quaternionf getRotation() { return rotation; }
    public float getScale() { return scale; }
    public AnimationData getAnimationData() { return animationData; }

    public final void setPosition(Vector3f position) {
        this.setPosition(position.x, position.y, position.z);
    }

    public final void setPosition(Vector3f position, boolean updateMatrix) {
        this.setPosition(position.x, position.y, position.z, updateMatrix);
    }

    public final void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
        updateModelMatrix();
    }

    public final void setPosition(float x, float y, float z, boolean updateMatrix) {
        position.x = x;
        position.y = y;
        position.z = z;
        if (updateMatrix) updateModelMatrix();
    }


    public final void setRotation(Vector3f axis, float angle) {
        this.setRotation(axis.x, axis.y, axis.z, angle);
    }

    public final void setRotation(Vector3f axis, float angle, boolean updateMatrix) {
        this.setRotation(axis.x, axis.y, axis.z, angle, updateMatrix);
    }

    public void setRotation(float x, float y, float z, float angle) {
        rotation.fromAxisAngleRad(x, y, z, angle);
        updateModelMatrix();
    }

    public void setRotation(float x, float y, float z, float angle, boolean updateMatrix) {
        rotation.fromAxisAngleRad(x, y, z, angle);
        if (updateMatrix) updateModelMatrix();
    }


    public void setScale(float scale) {
        this.scale = scale;
        updateModelMatrix();
    }

    public void setScale(float scale, boolean updateMatrix) {
        this.scale = scale;
        if (updateMatrix) updateModelMatrix();
    }


    public void setAnimationData(AnimationData animationData) { this.animationData = animationData; }

    public void setAnimation(Scene scene, int animationIndex) throws RuntimeException {
        Model model = scene.getModel(modelID);
        Model.Animation animation = model.getAnimations().get(animationIndex);
        if (animation == null) throw new RuntimeException("Failed to find animation at index \"" + animationIndex + "\".\n");
        this.setAnimationData(new AnimationData(animation));
    }

    public void updateModelMatrix() {
        modelMatrix.translationRotateScale(position, rotation, scale);
    }

}

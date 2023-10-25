package engine.scene.model;

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

    public final void setPosition(Vector3f position) { this.position.set(position.x, position.y, position.z); }

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

    public void updateModelMatrix() {
        modelMatrix.translationRotateScale(position, rotation, scale);
    }

}

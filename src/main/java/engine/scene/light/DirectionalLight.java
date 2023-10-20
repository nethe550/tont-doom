package engine.scene.light;

import org.joml.Vector3f;

public class DirectionalLight extends Light {

    private Vector3f direction;

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        super(intensity, color);

        this.direction = direction.normalize();
    }

    public Vector3f getDirection() { return direction; }

    public void setDirection(Vector3f direction) { this.direction = direction; }
    public void setDirection(float x, float y, float z) { direction.set(x, y, z); }

}

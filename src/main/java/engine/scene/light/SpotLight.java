package engine.scene.light;

import org.joml.Vector3f;

public class SpotLight extends PointLight {

    private Vector3f direction;
    private float innerCutoff;
    private float outerCutoff;

    public SpotLight(Vector3f color, Vector3f position, Vector3f direction, float intensity, float innerCutoff, float outerCutoff) {
        super(color, position, intensity);
        this.direction = direction;
        this.innerCutoff = innerCutoff;
        this.outerCutoff = outerCutoff;
    }

    public Vector3f getDirection() { return direction; }
    public float getInnerCutoff() { return innerCutoff; }
    public float getOuterCutoff() { return outerCutoff; }

    public void setDirection(Vector3f direction) { this.direction = direction; }
    public void setDirection(float x, float y, float z) { this.direction.set(x, y, z); }
    public void setInnerCutoff(float innerCutoff) { this.innerCutoff = innerCutoff; }
    public void setOuterCutoff(float outerCutoff) { this.outerCutoff = outerCutoff; }

}
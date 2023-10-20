package engine.scene.light;

import org.joml.Vector3f;

public class Light {

    protected float intensity;
    protected Vector3f color;

    public Light(float intensity, Vector3f color) {
        this.intensity = intensity;
        this.color = color;
    }

    public float getIntensity() { return intensity; }
    public Vector3f getColor() { return color; }

    public void setIntensity(float intensity) { this.intensity = intensity; }
    public void setColor(Vector3f color) { this.color = color; }
    public void setColor(float r, float g, float b) { this.color = new Vector3f(r, g, b); }

}

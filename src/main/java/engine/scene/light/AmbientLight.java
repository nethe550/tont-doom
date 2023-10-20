package engine.scene.light;

import org.joml.Vector3f;

public class AmbientLight extends Light {

    public AmbientLight(float intensity, Vector3f color) {
        super(intensity, color);
    }

    public AmbientLight() {
        this(1.0f, new Vector3f(1.0f, 1.0f, 1.0f));
    }

}

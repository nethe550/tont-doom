package engine.scene.light;

import org.joml.Vector3f;

import java.util.*;

public class SceneLights {

    private AmbientLight ambientLight;
    private DirectionalLight directionalLight;
    private List<PointLight> pointLights;

    public SceneLights() {
        ambientLight = new AmbientLight();
        directionalLight = new DirectionalLight(new Vector3f(1f, 1f, 1f), new Vector3f(0f, 1f, 0f), 1.0f);
        pointLights = new ArrayList<>();
    }

    public AmbientLight getAmbient() { return ambientLight; }
    public DirectionalLight getDirectional() { return directionalLight; }
    public List<PointLight> getPoints() { return pointLights; }

    public void setPoints(List<PointLight> points) { this.pointLights = points; }

}

package engine.level;

import engine.scene.Scene;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import engine.scene.model.ModelLoader;
import org.joml.Vector3f;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Level {

    private final String filePath;
    private Scene scene;

    public Level(String filePath) {
        this.filePath = filePath;
    }

    public void load(int windowWidth, int windowHeight) {
        this.scene = new Scene(windowWidth, windowHeight);
        this.loadLevel(this.filePath);
    }

    public Scene getScene() { return scene; }

    private void loadLevel(String filePath) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(filePath)));

            ObjectMapper mapper = new ObjectMapper();
            LevelData data = mapper.readValue(file, LevelData.class);

            applyLevelData(data);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to parse level data at \"" + filePath + "\".", e);
        }
    }

    private void applyLevelData(LevelData data) throws Exception {
        if (data.environment.skybox.active) {
            engine.scene.SkyBox sb = new engine.scene.SkyBox(data.environment.skybox.src, scene.getTextureCache());
            sb.getEntity().setScale((float) data.environment.skybox.scale);
            scene.setSkyBox(sb);
        }

        engine.scene.Fog fog = new engine.scene.Fog(data.environment.fog.active, doubleArrayToVector3f(data.environment.fog.color), (float) data.environment.fog.density);
        scene.setFog(fog);

        engine.scene.light.SceneLights sceneLights = new engine.scene.light.SceneLights();
        sceneLights.getAmbient().setIntensity((float) data.environment.lights.ambient.intensity);
        sceneLights.getAmbient().setColor(doubleArrayToVector3f(data.environment.lights.ambient.color));
        sceneLights.getDirectional().setIntensity((float) data.environment.lights.directional.intensity);
        sceneLights.getDirectional().setColor(doubleArrayToVector3f(data.environment.lights.directional.color));
        for (int i = 0; i < data.environment.lights.points.length; i++) {
            engine.scene.light.PointLight pl = new engine.scene.light.PointLight(
                    doubleArrayToVector3f(data.environment.lights.points[i].color),
                    doubleArrayToVector3f(data.environment.lights.points[i].position),
                    (float) data.environment.lights.points[i].intensity
            );
            sceneLights.getPoints().add(pl);
        }
        for (int i = 0; i < data.environment.lights.spots.length; i++) {
            engine.scene.light.SpotLight sl = new engine.scene.light.SpotLight(
                    doubleArrayToVector3f(data.environment.lights.spots[i].color),
                    doubleArrayToVector3f(data.environment.lights.spots[i].position),
                    doubleArrayToVector3f(data.environment.lights.spots[i].direction),
                    (float) data.environment.lights.spots[i].intensity,
                    (float) data.environment.lights.spots[i].innerCutoff,
                    (float) data.environment.lights.spots[i].outerCutoff
            );
            sceneLights.getSpots().add(sl);
        }
        scene.setSceneLights(sceneLights);

        for (int i = 0; i < data.environment.models.length; i++) {
            engine.graph.model.Model model = ModelLoader.loadModel(
                data.environment.models[i].id,
                data.environment.models[i].src,
                scene.getTextureCache(),
                data.environment.models[i].anim
            );
            scene.addModel(model);
        }

        for (int i = 0; i < data.environment.entities.length; i++) {
            engine.scene.model.Entity entity = new engine.scene.model.Entity(
                data.environment.entities[i].id,
                data.environment.entities[i].model
            );
            entity.setPosition(doubleArrayToVector3f(data.environment.entities[i].position), false);
            entity.setRotation(doubleArrayToVector3f(data.environment.entities[i].rotation), (float) Math.toRadians(data.environment.entities[i].rotation[3]), false);
            entity.setScale((float) data.environment.entities[i].scale, false);
            entity.updateModelMatrix();
            scene.addEntity(entity);
        }
    }

    private Vector3f doubleArrayToVector3f(double[] array) throws ArrayIndexOutOfBoundsException {
        if (array.length < 3) throw new ArrayIndexOutOfBoundsException("Not enough parameters passed to Vector3f.\n(provided: " + array.length + ", expected: 3+)");
        return new Vector3f((float) array[0], (float) array[1], (float) array[2]);
    }

    private static class SkyBox {
        @JsonProperty("active")
        public boolean active;
        @JsonProperty("src")
        public String src;
        @JsonProperty("scale")
        public double scale = 100.0f;
    }

    private static class Fog {
        @JsonProperty("active")
        public boolean active;
        @JsonProperty("color")
        public double[] color;
        @JsonProperty("density")
        public double density;
    }

    private static class Light {
        @JsonProperty("intensity")
        public double intensity;
        @JsonProperty("color")
        public double[] color;
    }

    private static class DirectionalLight extends Light {
        @JsonProperty("direction")
        public double[] direction;
    }

    private static class PointLight extends Light {
        @JsonProperty("position")
        public double[] position;
        @JsonProperty("color")
        public double[] color;
    }

    private static class SpotLight extends Light {
        @JsonProperty("position")
        public double[] position;
        @JsonProperty("direction")
        public double[] direction;
        @JsonProperty("innerCutoff")
        public double innerCutoff;
        @JsonProperty("outerCutoff")
        public double outerCutoff;
    }

    private static class Lights {
        @JsonProperty("ambient")
        public Light ambient;
        @JsonProperty("directional")
        public DirectionalLight directional;
        @JsonProperty("points")
        public PointLight[] points;
        @JsonProperty("spots")
        public SpotLight[] spots;
    }

    private static class Model {
        @JsonProperty("id")
        public String id;
        @JsonProperty("src")
        public String src;
        @JsonProperty("anim")
        public boolean anim;
    }

    private static class Entity {
        @JsonProperty("id")
        public String id;
        @JsonProperty("model")
        public String model;
        @JsonProperty("position")
        public double[] position = new double[3];
        @JsonProperty("rotation")
        public double[] rotation = new double[4];
        @JsonProperty("scale")
        public double scale = 1.0;
    }

    private static class Environment {
        @JsonProperty("skybox")
        public SkyBox skybox;
        @JsonProperty("fog")
        public Fog fog;
        @JsonProperty("lights")
        public Lights lights;
        @JsonProperty("models")
        public Model[] models;
        @JsonProperty("entities")
        public Entity[] entities;
    }

    private static class LevelData {
        @JsonProperty("name")
        public String name;
        @JsonProperty("environment")
        public Environment environment;
    }

}

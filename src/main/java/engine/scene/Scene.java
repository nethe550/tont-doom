package engine.scene;

import engine.graph.model.Model;
import engine.scene.model.Entity;
import engine.graph.render.TextureCache;
import engine.scene.light.SceneLights;
import engine.scene.view.*;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private final Map<String, Model> modelMap;
    private final TextureCache textureCache;

    private final Projection projection;
    private final Camera camera;

    private SceneLights sceneLights;
    private Fog fog;

    private SkyBox skyBox;

    public Scene(int width, int height) {
        modelMap = new HashMap<>();
        textureCache = new TextureCache();
        projection = new Projection(width, height);
        camera = new Camera();
        fog = new Fog();
    }

    public void addEntity(Entity entity) {
        String modelID = entity.getModelID();
        Model model = modelMap.get(modelID);
        if (model == null) throw new RuntimeException("Could not find model [" + modelID + "]");
        model.getEntities().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getID(), model);
    }

    public TextureCache getTextureCache() { return textureCache; }
    public Projection getProjection() { return projection; }
    public Camera getCamera() { return camera; }
    public SceneLights getSceneLights() { return sceneLights; }
    public Fog getFog() { return fog; }
    public SkyBox getSkyBox() { return skyBox; }

    public void setSceneLights(SceneLights sceneLights) { this.sceneLights = sceneLights; }
    public void setFog(Fog fog) { this.fog = fog; }
    public void setSkyBox(SkyBox skyBox) { this.skyBox = skyBox; }

    public void resize(int width, int height) {
        projection.updateProjection(width, height);
    }

    public void cleanup() {
            modelMap.values().forEach(Model::cleanup);
    }

    public Map<String, Model> getModelMap() { return modelMap; }

}

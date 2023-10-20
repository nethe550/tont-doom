package engine.scene;

import engine.graph.model.Model;
import engine.graph.render.TextureCache;
import engine.scene.light.SceneLights;
import engine.scene.model.Entity;
import engine.scene.view.Camera;
import engine.scene.view.Projection;
import engine.ui.IGUIInstance;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private final Map<String, Model> modelMap;
    private final TextureCache textureCache;

    private final Projection projection;
    private final Camera camera;

    private IGUIInstance guiInstance;

    private SceneLights sceneLights;

    public Scene(int width, int height) {
        modelMap = new HashMap<>();
        textureCache = new TextureCache();
        projection = new Projection(width, height);
        camera = new Camera();
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
    public IGUIInstance getGUIInstance() { return guiInstance; }
    public SceneLights getSceneLights() { return sceneLights; }

    public void setGUIInstance(IGUIInstance guiInstance) { this.guiInstance = guiInstance; }
    public void setSceneLights(SceneLights sceneLights) { this.sceneLights = sceneLights; }

    public void resize(int width, int height) {
        projection.updateProjection(width, height);
    }

    public void cleanup() {
            modelMap.values().forEach(Model::cleanup);
    }

    public Map<String, Model> getModelMap() { return modelMap; }

}

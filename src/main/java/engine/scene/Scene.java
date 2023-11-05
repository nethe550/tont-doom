package engine.scene;

import engine.graph.model.Model;
import engine.scene.model.Entity;
import engine.graph.render.texture.TextureCache;
import engine.scene.light.SceneLights;
import engine.scene.view.*;
import engine.sound.SoundListener;
import engine.sound.SoundManager;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private final Map<String, Model> modelMap;
    private final TextureCache textureCache;

    private final Projection projection;
    private final Camera camera;

    private final SoundManager soundManager;

    private SceneLights sceneLights;
    private Fog fog;

    private SkyBox skyBox;

    public Scene(int width, int height) {
        modelMap = new HashMap<>();
        textureCache = new TextureCache();
        projection = new Projection(width, height);
        camera = new Camera();
        fog = new Fog();
        soundManager = new SoundManager();
        soundManager.setListener(new SoundListener(camera.getPosition()));
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

    public Map<String, Model> getModelMap() { return modelMap; }
    public Model getModel(String id) { return modelMap.get(id); }

    public TextureCache getTextureCache() { return textureCache; }
    public Projection getProjection() { return projection; }
    public Camera getCamera() { return camera; }
    public SoundManager getSoundManager() { return soundManager; }
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
        soundManager.cleanup();
    }

    public void update(float diffTimeMillis) {
        for (Model model : modelMap.values()) {
            for (Entity entity : model.getEntities()) {
                entity.update();
            }
        }
        soundManager.updateListenerPosition(camera);
    }

}

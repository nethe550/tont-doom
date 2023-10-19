package engine.scene;

import engine.graph.Model;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private final Map<String, Model> modelMap;

    private Projection projection;

    public Scene(int width, int height) {
        modelMap = new HashMap<String, Model>();
        projection = new Projection(width, height);
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

    public Projection getProjection() { return projection; }

    public void resize(int width, int height) {
        projection.updateProjection(width, height);
    }

    public void cleanup() {
            modelMap.values().forEach(Model::cleanup);
    }

    public Map<String, Model> getModelMap() { return modelMap; }

}

package engine.scene;

import engine.graph.model.Model;
import engine.graph.texture.TextureCache;
import engine.scene.model.Entity;
import engine.scene.model.ModelLoader;

import java.io.IOException;

public class SkyBox {

    private Entity entity;
    private Model model;

    public SkyBox(String skyBoxModelPath, TextureCache textureCache) throws IOException {
        model = ModelLoader.loadModel("skybox-model", skyBoxModelPath, textureCache, false);
        entity = new Entity("skybox-entity", model.getID());
    }

    public Model getModel() { return model; }
    public Entity getEntity() { return entity; }

}

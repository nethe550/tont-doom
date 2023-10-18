package engine.scene;

import engine.graph.Mesh;

import java.util.HashMap;
import java.util.Map;

public class Scene {

    private final Map<String, Mesh> meshMap;

    public Scene() {
        meshMap = new HashMap<String, Mesh>();
    }

    public void addMesh(String meshID, Mesh mesh) {
        meshMap.put(meshID, mesh);
    }

    public void cleanup() {
            meshMap.values().forEach(Mesh::cleanup);
    }

    public Map<String, Mesh> getMeshMap() { return meshMap; }

}

package engine.graph.render;

import engine.scene.Scene;

public interface IRenderer {

    void cleanup();

    void update(float diffTimeMillis, int width, int height);

    void render(Scene scene);

}

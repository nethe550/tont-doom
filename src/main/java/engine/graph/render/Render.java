package engine.graph.render;

import engine.Window;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;

public class Render {

    public final SceneRender sceneRender;
    private final SkyBoxRender skyBoxRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

        sceneRender = new SceneRender();
        skyBoxRender = new SkyBoxRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
        skyBoxRender.cleanup();
    }

    public void resize(int width, int height) {}

    public void update(int width, int height, float diffTimeMillis) {
        sceneRender.update(diffTimeMillis, width, height);
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        skyBoxRender.render(scene);
        sceneRender.render(scene);
    }

}

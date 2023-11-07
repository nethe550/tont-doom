package engine.graph.render;

import engine.Window;
import engine.graph.render.scene.SceneRenderer;
import engine.graph.render.skybox.SkyBoxRender;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL13.*;

public class Render implements IRenderer {

    public final SceneRenderer sceneRenderer;
    private final SkyBoxRender skyBoxRender;

    private int width = 1;
    private int height = 1;

    public Render() {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRenderer = new SceneRenderer();
        skyBoxRender = new SkyBoxRender();
    }

    public void cleanup() {
        sceneRenderer.cleanup();
        skyBoxRender.cleanup();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update(float diffTimeMillis, int width, int height) {
        this.resize(width, height);
        sceneRenderer.update(diffTimeMillis, width, height);
    }

    public void render(Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, width, height);

        skyBoxRender.render(scene);
        sceneRenderer.render(scene);
    }

}

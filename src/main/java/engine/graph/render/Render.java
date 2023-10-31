package engine.graph.render;

import engine.Window;
import engine.graph.render.scene.SceneRender;
import engine.graph.render.shadow.ShadowRender;
import engine.graph.render.skybox.SkyBoxRender;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL13.*;

public class Render {

    public final SceneRender sceneRender;
    public final ShadowRender shadowRender;
    private final SkyBoxRender skyBoxRender;

    public Render() {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRender = new SceneRender();
        shadowRender = new ShadowRender();
        skyBoxRender = new SkyBoxRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
        shadowRender.cleanup();
        skyBoxRender.cleanup();
    }

    public void resize(int width, int height) {}

    public void update(int width, int height, float diffTimeMillis) {
        sceneRender.update(diffTimeMillis, width, height);
    }

    public void render(Window window, Scene scene) {
        shadowRender.render(scene);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        skyBoxRender.render(scene);
        sceneRender.render(scene, shadowRender);
    }

}

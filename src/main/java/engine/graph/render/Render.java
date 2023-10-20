package engine.graph.render;

import engine.Window;
import engine.graph.ui.GuiRender;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

public class Render {

    public final SceneRender sceneRender;
    private final GuiRender guiRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_BLEND);

        sceneRender = new SceneRender();
        guiRender = new GuiRender(window);
    }

    public void cleanup() {
        guiRender.cleanup();
    }

    public void resize(int width, int height) {
        guiRender.resize(width, height);
    }

    public void update(int width, int height, float diffTimeMillis) {
        sceneRender.update(diffTimeMillis, width, height);
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        sceneRender.render(scene);
        guiRender.render(scene);
    }

}

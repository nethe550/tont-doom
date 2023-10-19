package engine.graph;

import engine.Window;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

public class Render {

    public final SceneRender sceneRender;

    public Render() {
        GL.createCapabilities();
        sceneRender = new SceneRender();
    }

    public void cleanup() {

    }

    public void update(float diffTimeMillis) {
        sceneRender.update(diffTimeMillis);
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        sceneRender.render(scene);
    }

}

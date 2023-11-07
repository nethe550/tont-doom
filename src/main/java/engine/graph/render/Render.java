package engine.graph.render;

import engine.Window;
import engine.graph.render.scene.SceneRender;
import engine.graph.render.scene.lights.LightsRender;
import engine.graph.render.skybox.SkyBoxRender;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL30.*;

public class Render {

    private GBuffer gBuffer;

    public final SceneRender sceneRender;
    private final SkyBoxRender skyBoxRender;
    private final LightsRender lightsRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRender = new SceneRender();
        skyBoxRender = new SkyBoxRender();
        lightsRender = new LightsRender();
        gBuffer = new GBuffer(window);
    }

    public void cleanup() {
        sceneRender.cleanup();
        skyBoxRender.cleanup();
        lightsRender.cleanup();
        gBuffer.cleanup();
    }

    public void update(int width, int height, float diffTimeMillis) {
        sceneRender.update(diffTimeMillis, width, height);
    }

    public void resize(int width, int height) {}

    public void render(Window window, Scene scene) {
        sceneRender.render(scene, gBuffer);

        lightsRenderInit(window);
        lightsRender.render(scene, gBuffer);
        skyBoxRender.render(scene);
        lightsRenderCleanup();
    }

    private void lightsRenderInit(Window window) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferID());
    }

    private void lightsRenderCleanup() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

}

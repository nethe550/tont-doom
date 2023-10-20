package game;

import engine.*;
import engine.graph.model.Model;
import engine.graph.render.Render;
import engine.input.MouseInput;
import engine.scene.view.Camera;
import engine.scene.model.Entity;
import engine.scene.model.ModelLoader;
import engine.scene.Scene;
import engine.ui.IGUIInstance;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiCond;
import org.joml.Vector2f;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic, IGUIInstance {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.1f;

    private Entity turretEntity;
    private Entity testMapEntity;

    public static void main(String[] args) {
        Main main = new Main();
        Engine engine = new Engine("tont-doom", new Window.WindowOptions(1280, 720), main);
        engine.start();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void drawGUI() {
        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.showDemoWindow();
        ImGui.endFrame();
        ImGui.render();
    }

    @Override
    public boolean handleGUIInput(Scene scene, Window window) {
        ImGuiIO io = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPosition();
        io.setMousePos(mousePos.x, mousePos.y);
        io.setMouseDown(0, mouseInput.isLeftButtonPressed());
        io.setMouseDown(1, mouseInput.isRightButtonPressed());

        return io.getWantCaptureMouse() || io.getWantCaptureKeyboard();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        final String turretModelPath = "resources/models/turret/turret.obj";
        final String turretModelID = "turret-model";
        final String turretEntityID = "turret-entity";
        final String testMapModelPath = "resources/models/testmap/testmap.obj";
        final String testMapModelID = "testmap-model";
        final String testMapEntityID = "testmap-entity";

        try {
            Model turretModel = ModelLoader.loadModel(turretModelID, turretModelPath, scene.getTextureCache());
            scene.addModel(turretModel);
            turretEntity = new Entity(turretEntityID, turretModel.getID());
            turretEntity.setPosition(-10f, 0f, -3f);
            turretEntity.setRotation(0f, 1f, 0f, (float) Math.toRadians(225));
            scene.addEntity(turretEntity);

            Model testMapModel = ModelLoader.loadModel(testMapModelID, testMapModelPath, scene.getTextureCache());
            scene.addModel(testMapModel);
            testMapEntity = new Entity(testMapEntityID, testMapModel.getID());
            scene.addEntity(testMapEntity);

            scene.setGUIInstance(this);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create turret entity from path \"" + turretModelPath + "\"", e);
        }
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) return;

        float speed = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) camera.moveForward(speed);
        else if (window.isKeyPressed(GLFW_KEY_A)) camera.moveLeft(speed);
        else if (window.isKeyPressed(GLFW_KEY_S)) camera.moveBack(speed);
        else if (window.isKeyPressed(GLFW_KEY_D)) camera.moveRight(speed);
        else if (window.isKeyPressed(GLFW_KEY_Q)) camera.moveUp(speed);
        else if (window.isKeyPressed(GLFW_KEY_E)) camera.moveDown(speed);

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f deltaRotation = mouseInput.getDeltaRotation();
            camera.addRotation((float) Math.toRadians(-deltaRotation.y * MOUSE_SENSITIVITY), (float) Math.toRadians(-deltaRotation.x * MOUSE_SENSITIVITY));
        }
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

    }

}

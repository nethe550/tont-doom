package game;

import engine.*;
import engine.graph.model.Model;
import engine.graph.render.Render;
import engine.input.MouseInput;
import engine.scene.light.PointLight;
import engine.scene.light.SceneLights;
import engine.scene.light.SpotLight;
import engine.scene.view.Camera;
import engine.scene.model.Entity;
import engine.scene.model.ModelLoader;
import engine.scene.Scene;
import engine.ui.IGUIInstance;
import game.ui.LightControls;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic, IGUIInstance {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.1f;

    private Entity turretEntity;
    private Entity testMapEntity;

    private LightControls lightControls;

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
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create turret entity from path \"" + turretModelPath + "\"", e);
        }

        try {
            Model testMapModel = ModelLoader.loadModel(testMapModelID, testMapModelPath, scene.getTextureCache());
            scene.addModel(testMapModel);
            testMapEntity = new Entity(testMapEntityID, testMapModel.getID());
            scene.addEntity(testMapEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create test map entity from path \"" + testMapModelPath + "\"", e);
        }

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbient().setIntensity(0.1f);
        sceneLights.getDirectional().setDirection(0.0f, 0.65f, 0.75f);
        sceneLights.getDirectional().setColor(1.0f, 0.99f, 0.93f);
        scene.setSceneLights(sceneLights);

        sceneLights.getPoints().add(
            new PointLight(
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(-9.14f, 1.15f, -1.65f),
                1.0f
            )
        );
        sceneLights.getPoints().add(
            new PointLight(
                new Vector3f(0.5f, 0.0f, 1.0f),
                new Vector3f(-11.0f, 1.15f, -3.0f),
                1.0f
            )
        );
        sceneLights.getSpots().add(
            new SpotLight(
                new Vector3f(0.35f, 0.48f, 1.0f),
                new Vector3f(-9.8f, 1.5f, 1.25f),
                new Vector3f(0.0f, (float) -Math.sqrt(2) * 0.5f, (float) -Math.sqrt(2) * 0.5f),
                6.0f,
                20f,
                40f
            )
        );

        lightControls = new LightControls(scene);
        scene.setGUIInstance(lightControls);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) return;

        float speed = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) camera.moveForward(speed);
        else if (window.isKeyPressed(GLFW_KEY_S)) camera.moveBack(speed);
        if (window.isKeyPressed(GLFW_KEY_A)) camera.moveLeft(speed);
        else if (window.isKeyPressed(GLFW_KEY_D)) camera.moveRight(speed);
        if (window.isKeyPressed(GLFW_KEY_Q)) camera.moveUp(speed);
        else if (window.isKeyPressed(GLFW_KEY_E)) camera.moveDown(speed);

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f deltaRotation = mouseInput.getDeltaRotation();
            camera.addRotation((float) Math.toRadians(deltaRotation.y * MOUSE_SENSITIVITY), (float) Math.toRadians(deltaRotation.x * MOUSE_SENSITIVITY));
        }
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

    }

}

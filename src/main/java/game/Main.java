package game;

import engine.*;
import engine.graph.*;
import engine.scene.Camera;
import engine.scene.Entity;
import engine.scene.ModelLoader;
import engine.scene.Scene;
import engine.util.Util;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.1f;

    // = Entities ============== //

    private final String turretModelID = "turret-model";
    private final String turretEntityID = "turret-entity";
    private Entity turretEntity;

    // ========================= //

    private float rotation = 0.0f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine engine = new Engine("tont-doom", new Window.WindowOptions(640, 480), main);
        engine.start();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        final String turretModelPath = "resources/models/turret.obj";
        try {
            Model turretModel = ModelLoader.loadModel("turret-model", turretModelPath, scene.getTextureCache());
            scene.addModel(turretModel);

            turretEntity = new Entity(turretEntityID, turretModel.getID());
            turretEntity.setPosition(0f, -1f, -3f);
            turretEntity.setRotation(0f, 1f, 0f, (float) Math.toRadians(135));
            scene.addEntity(turretEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create turret entity from path \"" + turretModelPath + "\"", e);
        }
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {
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
        rotation += 1.5;
        if (rotation > 360) rotation = 0;
        turretEntity.setRotation(0f, 1f, 0f, (float) Math.toRadians(rotation));
    }

}

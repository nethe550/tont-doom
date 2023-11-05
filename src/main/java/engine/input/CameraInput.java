package engine.input;

import engine.Window;
import engine.scene.Scene;
import engine.scene.view.Camera;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class CameraInput {

    private final Camera camera;
    private float mouseSensitivity;
    private float mouseSpeed;

    public CameraInput(Camera camera) {
        this(camera, 0.1f, 0.1f);
    }

    public CameraInput(Camera camera, float mouseSensitivity, float mouseSpeed) {
        this.camera = camera;
        this.mouseSensitivity = mouseSensitivity;
        this.mouseSpeed = mouseSpeed;
    }

    public float getSensitivity() { return mouseSensitivity; }
    public float getSpeed() { return mouseSpeed; }

    public void setSensitivity(float sensitivity) { this.mouseSensitivity = sensitivity; }
    public void setSpeed(float speed) { this.mouseSpeed = speed; }

    public void input(Window window, float diffTimeMillis) {
        float speed = diffTimeMillis * mouseSpeed;
        if (window.isKeyPressed(GLFW_KEY_W)) camera.moveForward(speed);
        else if (window.isKeyPressed(GLFW_KEY_S)) camera.moveBack(speed);
        if (window.isKeyPressed(GLFW_KEY_A)) camera.moveLeft(speed);
        else if (window.isKeyPressed(GLFW_KEY_D)) camera.moveRight(speed);
        if (window.isKeyPressed(GLFW_KEY_SPACE)) camera.moveUp(speed);
        else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) camera.moveDown(speed);

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f deltaRotation = mouseInput.getDeltaRotation();
            camera.addRotation((float) Math.toRadians(deltaRotation.y * mouseSensitivity), (float) Math.toRadians(deltaRotation.x * mouseSensitivity));
        }
    }

}

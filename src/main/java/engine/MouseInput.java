package engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    private final Vector2f currentPos;
    private final Vector2f previousPos;
    private final Vector2f deltaRotation;
    private boolean inWindow;
    private boolean leftButtonPressed;
    private boolean rightButtonPressed;

    public MouseInput(long windowHandle) {
        previousPos = new Vector2f(-1f, -1f);
        currentPos = new Vector2f();
        deltaRotation = new Vector2f();
        inWindow = false;
        leftButtonPressed = false;
        rightButtonPressed = false;

        glfwSetCursorPosCallback(windowHandle, (handle, x, y) -> {
            currentPos.x = (float) x;
            currentPos.y = (float) y;
        });

        glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> inWindow = entered);

        glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getCurrentPosition() { return currentPos; }
    public Vector2f getDeltaRotation() { return deltaRotation; }
    public boolean isLeftButtonPressed() { return leftButtonPressed; }
    public boolean isRightButtonPressed() { return rightButtonPressed; }

    public void input() {
        deltaRotation.x = 0;
        deltaRotation.y = 0;
        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
            double deltaX = currentPos.x - previousPos.x;
            double deltaY = currentPos.y - previousPos.y;
            boolean rotateX = deltaX != 0;
            boolean rotateY = deltaY != 0;
            if (rotateX) deltaRotation.x = (float) deltaX;
            if (rotateY) deltaRotation.y = (float) deltaY;
        }
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

}

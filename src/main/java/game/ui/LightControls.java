package game.ui;

import engine.Window;
import engine.input.MouseInput;
import engine.scene.Scene;
import engine.scene.light.*;
import engine.ui.IGUIInstance;
import imgui.*;
import imgui.flag.ImGuiCond;
import org.joml.*;

public class LightControls implements IGUIInstance {

    private final float[] ambientColor;
    private final float[] ambientFactor;
    private final float[] dirLightColor;
    private final float[] dirLightIntensity;
    private final float[] dirLightX;
    private final float[] dirLightY;
    private final float[] dirLightZ;
    private final float[] pointLightColor;
    private final float[] pointLightIntensity;
    private final float[] pointLightX;
    private final float[] pointLightY;
    private final float[] pointLightZ;

    public LightControls(Scene scene) {
        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbient();
        Vector3f color = ambientLight.getColor();

        ambientFactor = new float[] { ambientLight.getIntensity() };
        ambientColor = new float[] { color.x, color.y, color.z };

        PointLight pointLight = sceneLights.getPoints().get(0);
        color = pointLight.getColor();
        Vector3f pos = pointLight.getPosition();
        pointLightColor = new float[] { color.x, color.y, color.z };
        pointLightX = new float[] { pos.x };
        pointLightY = new float[] { pos.y };
        pointLightZ = new float[] { pos.z };
        pointLightIntensity = new float[]{pointLight.getIntensity()};

        DirectionalLight directionalLight = sceneLights.getDirectional();
        color = directionalLight.getColor();
        pos = directionalLight.getDirection();
        dirLightColor = new float[] { color.x, color.y, color.z };
        dirLightX = new float[] { pos.x };
        dirLightY = new float[] { pos.y };
        dirLightZ = new float[] { pos.z };
        dirLightIntensity = new float[] { directionalLight.getIntensity() };
    }

    @Override
    public void drawGUI() {
        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("Lights controls");
        if (ImGui.collapsingHeader("Ambient Light")) {
            ImGui.sliderFloat("Ambient factor", ambientFactor, 0.0f, 1.0f, "%.2f");
            ImGui.colorEdit3("Ambient color", ambientColor);
        }

        if (ImGui.collapsingHeader("Point Light")) {
            ImGui.sliderFloat("Point Light - x", pointLightX, -10.0f, 10.0f, "%.2f");
            ImGui.sliderFloat("Point Light - y", pointLightY, -10.0f, 10.0f, "%.2f");
            ImGui.sliderFloat("Point Light - z", pointLightZ, -10.0f, 10.0f, "%.2f");
            ImGui.colorEdit3("Point Light color", pointLightColor);
            ImGui.sliderFloat("Point Light Intensity", pointLightIntensity, 0.0f, 1.0f, "%.2f");
        }

        if (ImGui.collapsingHeader("Dir Light")) {
            ImGui.sliderFloat("Dir Light - x", dirLightX, -1.0f, 1.0f, "%.2f");
            ImGui.sliderFloat("Dir Light - y", dirLightY, -1.0f, 1.0f, "%.2f");
            ImGui.sliderFloat("Dir Light - z", dirLightZ, -1.0f, 1.0f, "%.2f");
            ImGui.colorEdit3("Dir Light color", dirLightColor);
            ImGui.sliderFloat("Dir Light Intensity", dirLightIntensity, 0.0f, 1.0f, "%.2f");
        }

        ImGui.end();
        ImGui.endFrame();
        ImGui.render();
    }

    @Override
    public boolean handleGUIInput(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPosition();
        imGuiIO.setMousePos(mousePos.x, mousePos.y);
        imGuiIO.setMouseDown(0, mouseInput.isLeftButtonPressed());
        imGuiIO.setMouseDown(1, mouseInput.isRightButtonPressed());

        boolean consumed = imGuiIO.getWantCaptureMouse() || imGuiIO.getWantCaptureKeyboard();
        if (consumed) {
            SceneLights sceneLights = scene.getSceneLights();
            AmbientLight ambientLight = sceneLights.getAmbient();
            ambientLight.setIntensity(ambientFactor[0]);
            ambientLight.setColor(ambientColor[0], ambientColor[1], ambientColor[2]);

            PointLight pointLight = sceneLights.getPoints().get(0);
            pointLight.setPosition(pointLightX[0], pointLightY[0], pointLightZ[0]);
            pointLight.setColor(pointLightColor[0], pointLightColor[1], pointLightColor[2]);
            pointLight.setIntensity(pointLightIntensity[0]);

            DirectionalLight directionalLight = sceneLights.getDirectional();
            directionalLight.setDirection(dirLightX[0], dirLightY[0], dirLightZ[0]);
            directionalLight.setColor(dirLightColor[0], dirLightColor[1], dirLightColor[2]);
            directionalLight.setIntensity(dirLightIntensity[0]);
        }
        return consumed;
    }

}

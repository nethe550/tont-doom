package engine.graph.ui;

import engine.Window;
import engine.graph.render.Uniforms;
import engine.ui.IGUIInstance;
import imgui.*;
import imgui.flag.ImGuiKey;
import imgui.type.ImInt;
import org.joml.Vector2f;

import engine.graph.render.ShaderProgram;
import engine.scene.Scene;
import engine.graph.render.Texture;

import java.nio.ByteBuffer;
import java.util.*;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;

public class GuiRender {

    private GuiMesh mesh;
    private GLFWKeyCallback prevKeyCallback;
    private Vector2f scale;
    private ShaderProgram shaderProgram;
    private Texture texture;
    private Uniforms uniforms;

    public GuiRender(Window window) {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/ui/gui.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/ui/gui.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        createUniforms();
        createUIResources(window);
        setupKeyCallback(window);
    }

    public void cleanup() {
        shaderProgram.cleanup();
        texture.cleanup();
        if (prevKeyCallback != null) prevKeyCallback.free();
    }

    public void resize(int width, int height) {
        ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(width, height);
    }

    public void render(Scene scene) {
        IGUIInstance guiInstance = scene.getGUIInstance();
        if (guiInstance == null) return;

        guiInstance.drawGUI();

        shaderProgram.bind();

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glBindVertexArray(mesh.getVaoID());

        glBindBuffer(GL_ARRAY_BUFFER, mesh.getVerticesVBO());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.getIndicesVBO());

        ImGuiIO io = ImGui.getIO();
        scale.x = 2.0f / io.getDisplaySizeX();
        scale.y = -2.0f / io.getDisplaySizeY();
        uniforms.setUniform("scale", scale);

        ImDrawData drawData = ImGui.getDrawData();
        int numLists = drawData.getCmdListsCount();
        for (int i = 0; i < numLists; i++) {
            glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(i), GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(i), GL_STREAM_DRAW);

            int numCmds = drawData.getCmdListCmdBufferSize(i);
            for (int j = 0; j < numCmds; j++) {
                final int elementCount = drawData.getCmdListCmdBufferElemCount(i, j);
                final int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);
                final int indices = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX;

                texture.bind();
                glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, indices);
            }
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);
    }

    private void createUniforms() {
        uniforms = new Uniforms(shaderProgram.getProgramID());
        uniforms.createUniform("scale");
        scale = new Vector2f();
    }

    private void createUIResources(Window window) {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.setDisplaySize(window.getWidth(), window.getHeight());

        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer buf = fontAtlas.getTexDataAsRGBA32(width, height);
        texture = new Texture(width.get(), height.get(), buf);

        mesh = new GuiMesh();
    }

    private void setupKeyCallback(Window window) {
        ImGuiIO io = ImGui.getIO();
        io.setKeyMap(ImGuiKey.Tab, GLFW_KEY_TAB);
        io.setKeyMap(ImGuiKey.LeftArrow, GLFW_KEY_LEFT);
        io.setKeyMap(ImGuiKey.RightArrow, GLFW_KEY_RIGHT);
        io.setKeyMap(ImGuiKey.UpArrow, GLFW_KEY_UP);
        io.setKeyMap(ImGuiKey.DownArrow, GLFW_KEY_DOWN);
        io.setKeyMap(ImGuiKey.PageUp, GLFW_KEY_PAGE_UP);
        io.setKeyMap(ImGuiKey.PageDown, GLFW_KEY_PAGE_DOWN);
        io.setKeyMap(ImGuiKey.Home, GLFW_KEY_HOME);
        io.setKeyMap(ImGuiKey.End, GLFW_KEY_END);
        io.setKeyMap(ImGuiKey.Insert, GLFW_KEY_INSERT);
        io.setKeyMap(ImGuiKey.Delete, GLFW_KEY_DELETE);
        io.setKeyMap(ImGuiKey.Backspace, GLFW_KEY_BACKSPACE);
        io.setKeyMap(ImGuiKey.Space, GLFW_KEY_SPACE);
        io.setKeyMap(ImGuiKey.Enter, GLFW_KEY_ENTER);
        io.setKeyMap(ImGuiKey.Escape, GLFW_KEY_ESCAPE);
        io.setKeyMap(ImGuiKey.KeyPadEnter, GLFW_KEY_KP_ENTER);

        prevKeyCallback = glfwSetKeyCallback(window.getHandle(), (handle, key, scancode, action, mods) -> {
            window.keyCallBack(key, action);
            if (!io.getWantCaptureKeyboard()) {
                if (prevKeyCallback != null) prevKeyCallback.invoke(handle, key, scancode, action, mods);
                return;
            }

            if (action == GLFW_PRESS) io.setKeysDown(key, true);
            else if (action == GLFW_RELEASE) io.setKeysDown(key, false);

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        });

        glfwSetCharCallback(window.getHandle(), (handle, c) -> {
            if (!io.getWantCaptureKeyboard()) return;
            io.addInputCharacter(c);
        });
    }

}

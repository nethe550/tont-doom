package view;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    private long handle;

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW.");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // hide window until fully initialized
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        handle = glfwCreateWindow(640, 480, "tont", NULL, NULL);
        if (handle == NULL) throw new RuntimeException("Failed to create a GLFW window.");

        glfwSetKeyCallback(handle, (handle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(handle, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(handle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1); // vsync
        glfwShowWindow(handle); // show window
    }

    private void loop() {
        GL.createCapabilities();

        glClearColor(0.5f, 0.0f, 1.0f, 0.0f);

        while(!glfwWindowShouldClose(handle)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear framebuffer
            glfwSwapBuffers(handle);
            glfwPollEvents();
        }
    }

}

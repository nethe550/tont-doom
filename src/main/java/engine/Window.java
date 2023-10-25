package engine;

import engine.input.MouseInput;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final long handle;
    private int width;
    private int height;
    private Callable<Void> resizeFunc;
    private MouseInput mouseInput;

    public Window(String title, WindowOptions opts, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW.");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        if (opts.antiAliasing) glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);

        if (opts.compatibleProfile) glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        if (opts.width > 0 && opts.height > 0) {
            this.width = opts.width;
            this.height = opts.height;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode == null) throw new RuntimeException("Failed to get video mode.");
            width = vidmode.width();
            height = vidmode.height();
        }

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) throw new RuntimeException("Failed to create GLFW window.");

        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> resized(w, h));

        glfwSetErrorCallback((int error, long msgptr) -> {
            System.err.printf("Error %o -> \"" + MemoryUtil.memUTF8(msgptr) + "\"%n", error);
        });

        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });

        glfwMakeContextCurrent(handle);

        if (opts.fps > 0) glfwSwapInterval(0);
        else glfwSwapInterval(1);

        glfwShowWindow(handle);

        int[] awidth = new int[1];
        int [] aheight = new int[1];
        glfwGetFramebufferSize(handle, awidth, aheight);
        width = awidth[0];
        height = aheight[0];

        mouseInput = new MouseInput(handle);
    }

    public void cleanup() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) callback.free();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public long getHandle() { return handle; }
    public MouseInput getMouseInput() { return mouseInput; }

    public boolean isKeyPressed(int key) { return glfwGetKey(handle, key) == GLFW_PRESS; }
    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(handle, true);
        }
    }

    public void pollEvents() { glfwPollEvents(); }

    protected void resized(int width, int height) {
        this.width = width;
        this.height = height;
        try { resizeFunc.call(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        glfwSwapBuffers(handle);
    }

    public boolean windowShouldClose() { return glfwWindowShouldClose(handle); }

    public static class WindowOptions {
        public boolean compatibleProfile = false;
        public int fps = 60;
        public int ups = Engine.TARGET_UPS;
        public int width = 640;
        public int height = 480;
        public boolean antiAliasing = true;

        public WindowOptions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public WindowOptions(int width, int height, int fps) {
            this.width = width;
            this.height = height;
            this.fps = fps;
        }

        public WindowOptions(int width, int height, int fps, int ups) {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.ups = ups;
        }

        public WindowOptions(int width, int height, int fps, int ups, boolean compatibleProfile) {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.ups = ups;
            this.compatibleProfile = compatibleProfile;
        }

        public WindowOptions(int width, int height, int fps, int ups, boolean compatibleProfile, boolean antiAliasing) {
            this.width = width;
            this.height = height;
            this.fps = fps;
            this.ups = ups;
            this.compatibleProfile = compatibleProfile;
            this.antiAliasing = antiAliasing;
        }
    }

}

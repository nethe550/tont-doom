package engine;

import engine.graph.Render;
import engine.scene.Scene;

public class Engine {

    public static final int TARGET_UPS = 30;

    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private boolean running;
    private Scene scene;
    private int targetFPS;
    private int targetUPS;

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic) {
        window = new Window(windowTitle, opts, () -> {
            resize();
            return null;
        });

        targetFPS = opts.fps;
        targetUPS = opts.ups;
        this.appLogic = appLogic;
        render = new Render();
        scene = new Scene();
        appLogic.init(window, scene, render);
        running = true;
    }

    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        scene.cleanup();
        window.cleanup();
    }

    private void resize() {

    }

    private void run() {
        long time = System.currentTimeMillis();

        float timeU = 1000.0f / targetUPS;
        float timeR = targetFPS > 0 ? 1000.0f / targetFPS : 0;
        float deltaUpdate = 0;
        float deltaFps = 0;

        long updateTime = time;
        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - time) / timeU;
            deltaFps += (now - time) / timeR;

            if (targetFPS <= 0 || deltaFps >= 1) {
                appLogic.input(window, scene, now - time);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                appLogic.update(window, scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFPS <= 0 || deltaFps >= 1) {
                render.render(window, scene);
                deltaFps--;
                window.update();
            }
            time = now;
        }

        cleanup();
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }

}

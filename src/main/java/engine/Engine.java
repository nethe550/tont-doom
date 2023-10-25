package engine;

import engine.graph.render.Render;
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
        render = new Render(window);
        scene = new Scene(window.getWidth(), window.getHeight());
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
        int width = window.getWidth(), height = window.getHeight();
        scene.resize(width, height);
        render.resize(width, height);
    }

    private void run() {
        long time = System.currentTimeMillis();

        float timeU = 1000.0f / targetUPS;
        float timeR = targetFPS > 0 ? 1000.0f / targetFPS : 0;
        float deltaUpdate = 0;
        float deltaFPS = 0;

        long updateTime = time;
        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - time) / timeU;
            deltaFPS += (now - time) / timeR;

            if (targetFPS <= 0 || deltaFPS >= 1) {
                window.getMouseInput().input();
                appLogic.input(window, scene, now - time, false);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                appLogic.update(window, scene, diffTimeMillis);
                render.update(window.getWidth(), window.getHeight(), diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFPS <= 0 || deltaFPS >= 1) {
                render.render(window, scene);
                deltaFPS--;
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

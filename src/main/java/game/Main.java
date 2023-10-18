package game;

import engine.*;
import engine.graph.Mesh;
import engine.graph.Render;
import engine.scene.Scene;

public class Main implements IAppLogic {

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
        float[] positions = new float[] {
                 0.0f,  0.5f,  0.0f,
                -0.5f, -0.5f,  0.0f,
                 0.5f, -0.5f,  0.0f
        };
        Mesh mesh = new Mesh(positions, 3);
        scene.addMesh("triangle", mesh);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {

    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

    }

}

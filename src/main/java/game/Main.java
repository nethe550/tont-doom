package game;

import engine.*;
import engine.graph.Mesh;
import engine.graph.Model;
import engine.graph.Render;
import engine.scene.Entity;
import engine.scene.Scene;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    // = Entities ============== //

    private Entity cubeEntity;
    private final String cubeEntityID = "cube-entity";

    private List<Mesh> cubeMeshes;
    private Mesh cubeMesh;
    float[] cubeMeshPositions;
    float[] cubeMeshColors;
    int[] cubeMeshIndices;

    private Model cubeModel;
    private final String cubeModelID = "cube-model";

    // ========================= //

    private final Vector4f displInc = new Vector4f();
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
        cubeMeshPositions = new float[] {
                -0.5f,  0.5f,  0.5f, // 0
                -0.5f, -0.5f,  0.5f, // 1
                 0.5f, -0.5f,  0.5f, // 2
                 0.5f,  0.5f,  0.5f, // 3
                -0.5f,  0.5f, -0.5f, // 4
                 0.5f,  0.5f, -0.5f, // 5
                -0.5f, -0.5f, -0.5f, // 6
                 0.5f, -0.5f, -0.5f, // 7
        };
        cubeMeshColors = new float[] {
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
                0.5f, 0.5f, 0.0f,
                0.5f, 0.0f, 0.5f,
                0.5f, 0.5f, 0.5f,
                0.0f, 0.0f, 0.0f
        };
        cubeMeshIndices = new int[] {
// CLOCKWISE
//                1, 0, 3, 3, 2, 1, // front
//                0, 4, 5, 5, 3, 0, // top
//                2, 3, 5, 5, 7, 2, // right
//                6, 4, 0, 0, 1, 6, // left
//                2, 7, 6, 6, 1, 2, // bottom
//                5, 4, 7, 7, 4, 6, // back

// ANTICLOCKWISE
                0, 1, 3, 3, 1, 2, // front
                4, 0, 3, 5, 4, 3, // top
                3, 2, 7, 5, 3, 7, // right
                6, 1, 0, 6, 0, 4, // left
                2, 1, 6, 2, 6, 7, // bottom
                7, 6, 4, 7, 4, 5, // back
        };
        cubeMeshes = new ArrayList<>();
        cubeMesh = new Mesh(cubeMeshPositions, cubeMeshColors, cubeMeshIndices);
        cubeMeshes.add(cubeMesh);

        cubeModel = new Model(cubeModelID, cubeMeshes);
        scene.addModel(cubeModel);

        cubeEntity = new Entity(cubeEntityID, cubeModelID);
        cubeEntity.setPosition(0, 0, -2f);
        scene.addEntity(cubeEntity);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {
        displInc.zero();
        if (window.isKeyPressed(GLFW_KEY_UP)) displInc.y = 1;
        else if (window.isKeyPressed(GLFW_KEY_DOWN)) displInc.y = -1;
        if (window.isKeyPressed(GLFW_KEY_LEFT)) displInc.x = -1;
        else if (window.isKeyPressed(GLFW_KEY_RIGHT)) displInc.x = 1;
        if (window.isKeyPressed(GLFW_KEY_A)) displInc.z = -1;
        else if (window.isKeyPressed(GLFW_KEY_Q)) displInc.y = 1;
        if (window.isKeyPressed(GLFW_KEY_Z)) displInc.w = -1;
        else if (window.isKeyPressed(GLFW_KEY_X)) displInc.w = 1;

        displInc.mul(diffTimeMillis / 100.0f);

        Vector3f entitypos = cubeEntity.getPosition();
        cubeEntity.setPosition(displInc.x + entitypos.x, displInc.y + entitypos.y, displInc.z + entitypos.z, true);
        cubeEntity.setScale(cubeEntity.getScale() + displInc.w, true);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

        rotation += 1.5;
        if (rotation > 360) rotation = 0;
        cubeEntity.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
    }

}

package game;

import engine.*;
import engine.graph.*;
import engine.scene.Entity;
import engine.scene.Scene;
import engine.util.Util;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    // = Entities ============== //

    private final String turretModelID = "turret-model";
    private final String turretEntityID = "turret-entity";
    private Entity turretEntity;

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
        Material turretMaterial = new Material();

        Texture texture = scene.getTextureCache().createTexture("models/turret.png");
        turretMaterial.setTexturePath(texture.getTexturePath());

        List<Material> turretMaterials = new ArrayList<>();
        turretMaterials.add(turretMaterial);

        Mesh turretMesh = new Mesh(Util.loadOBJMeshData(Main.class, "models/turret.obj"));
        turretMaterial.getMeshes().add(turretMesh);

        Model turretModel = new Model(turretModelID, turretMaterials);
        scene.addModel(turretModel);

        turretEntity = new Entity(turretEntityID, turretModel.getID());
        turretEntity.setPosition(0f, -1f, -3f);
        turretEntity.setRotation(0f, 1f, 0f, (float) Math.toRadians(135));
        scene.addEntity(turretEntity);
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

        Vector3f entitypos = turretEntity.getPosition();
        turretEntity.setPosition(displInc.x + entitypos.x, displInc.y + entitypos.y, displInc.z + entitypos.z, true);
        turretEntity.setScale(turretEntity.getScale() + displInc.w, true);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {

        rotation += 1.5;
        if (rotation > 360) rotation = 0;
        turretEntity.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
    }

}

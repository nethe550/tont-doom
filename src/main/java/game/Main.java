package game;

import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.*;
import engine.graph.model.Model;
import engine.graph.render.Render;
import engine.input.*;
import engine.scene.*;
import engine.scene.light.*;
import engine.scene.view.*;
import engine.scene.model.*;
import engine.sound.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    public static Main Instance;

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.1f;

    private Entity turretEntity;
    private Entity monkeyEntity;
    private Entity bobEntity;
    private Entity testMapEntity;

    private SoundSource bobSoundSource;

    private AnimationData animationData;
    private SoundManager soundManager;

    public static void main(String[] args) {
        Instance = new Main();
        Window.WindowOptions opts = new Window.WindowOptions(1280, 720);
        opts.antiAliasing = true;
        Engine engine = new Engine("tont-doom", opts, Instance);
        engine.start();
    }

    @Override
    public void cleanup() {
        soundManager.cleanup();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        final String turretModelPath = "resources/models/turret/turret.obj";
        final String turretModelID = "turret-model";
        final String turretEntityID = "turret-entity";
        final String monkeyModelPath = "resources/models/monkey/monkey.obj";
        final String monkeyModelID = "monkey-model";
        final String monkeyEntityID = "monkey-entity";
        final String bobModelPath = "resources/models/bob/boblamp.md5mesh";
        final String bobModelID = "bob-model";
        final String bobEntityID = "bob-entity";
        final String testMapModelPath = "resources/models/testmap/testmap.obj";
        final String testMapModelID = "testmap-model";
        final String testMapEntityID = "testmap-entity";

        try {
            Model turretModel = ModelLoader.loadModel(turretModelID, turretModelPath, scene.getTextureCache(), false);
            scene.addModel(turretModel);
            turretEntity = new Entity(turretEntityID, turretModel.getID());
            turretEntity.setPosition(-10f, 0f, -3f);
            turretEntity.setRotation(0f, 1f, 0f, (float) Math.toRadians(225));
            scene.addEntity(turretEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create turret entity from path \"" + turretModelPath + "\"", e);
        }

        try {
            Model monkeyModel = ModelLoader.loadModel(monkeyModelID, monkeyModelPath, scene.getTextureCache(), false);
            scene.addModel(monkeyModel);
            monkeyEntity = new Entity(monkeyEntityID, monkeyModel.getID());
            monkeyEntity.setPosition(-1f, 1f, -1f);
            scene.addEntity(monkeyEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create monkey entity from path \"" + monkeyModelPath + "\"", e);
        }

        try {
            Model bobModel = ModelLoader.loadModel(bobModelID, bobModelPath, scene.getTextureCache(), true);
            scene.addModel(bobModel);
            bobEntity = new Entity(bobEntityID, bobModel.getID());
            bobEntity.setPosition(2.25f, 0f, -1f);
            bobEntity.setScale(0.025f);
            animationData = new AnimationData(bobModel.getAnimations().get(0));
            bobEntity.setAnimationData(animationData);
            scene.addEntity(bobEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create bob entity from path \"" + bobModelPath + "\"", e);
        }

        try {
            Model testMapModel = ModelLoader.loadModel(testMapModelID, testMapModelPath, scene.getTextureCache(), false);
            scene.addModel(testMapModel);
            testMapEntity = new Entity(testMapEntityID, testMapModel.getID());
            scene.addEntity(testMapEntity);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create test map entity from path \"" + testMapModelPath + "\"", e);
        }

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbient().setIntensity(0.1f);
        sceneLights.getDirectional().setDirection(0.0f, 0.65f, 0.75f);
        sceneLights.getDirectional().setColor(1.0f, 0.99f, 0.93f);
        scene.setSceneLights(sceneLights);

        sceneLights.getPoints().add(
            new PointLight(
                new Vector3f(1.0f, 0.0f, 0.0f),
                new Vector3f(-9.14f, 1.15f, -1.65f),
                1.0f
            )
        );
        sceneLights.getPoints().add(
            new PointLight(
                new Vector3f(0.5f, 0.0f, 1.0f),
                new Vector3f(-11.0f, 1.15f, -3.0f),
                1.0f
            )
        );
        sceneLights.getSpots().add(
            new SpotLight(
                new Vector3f(0.35f, 0.48f, 1.0f),
                new Vector3f(-9.8f, 1.5f, 1.25f),
                new Vector3f(0.0f, (float) -Math.sqrt(2) * 0.5f, (float) -Math.sqrt(2) * 0.5f),
                6.0f,
                20f,
                40f
            )
        );

        try {
            SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache());
            skyBox.getEntity().setScale(Projection.Z_FAR);
            scene.setSkyBox(skyBox);
            updateSkyBox(scene);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create skybox.");
        }

        scene.setFog(new Fog(true, new Vector3f(0.1f, 0.1f, 0.2f), 0.05f));

        Camera camera = scene.getCamera();
        camera.moveUp(1.0f);
        initSounds(bobEntity.getPosition(), camera);
    }

    private void initSounds(Vector3f position, Camera camera) {
        soundManager = new SoundManager();
        soundManager.setListener(new SoundListener(camera.getPosition()));

        SoundBuffer bobbuf = new SoundBuffer("resources/sounds/creak1.ogg", SoundBuffer.FileType.OGG);
        soundManager.addSoundBuffer(bobbuf);

        bobSoundSource = new SoundSource(false, false);
        bobSoundSource.setPosition(position);
        bobSoundSource.setBuffer(bobbuf.getBufferID());
        soundManager.addSoundSource("creak", bobSoundSource);

        bobbuf = new SoundBuffer("resources/sounds/woo_scary.ogg", SoundBuffer.FileType.OGG);
        soundManager.addSoundBuffer(bobbuf);

        SoundSource source = new SoundSource(true, true);
        source.setBuffer(bobbuf.getBufferID());
        soundManager.addSoundSource("music", source);
        source.play();
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) return;

        float speed = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) camera.moveForward(speed);
        else if (window.isKeyPressed(GLFW_KEY_S)) camera.moveBack(speed);
        if (window.isKeyPressed(GLFW_KEY_A)) camera.moveLeft(speed);
        else if (window.isKeyPressed(GLFW_KEY_D)) camera.moveRight(speed);
        if (window.isKeyPressed(GLFW_KEY_SPACE)) camera.moveUp(speed);
        else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) camera.moveDown(speed);

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f deltaRotation = mouseInput.getDeltaRotation();
            camera.addRotation((float) Math.toRadians(deltaRotation.y * MOUSE_SENSITIVITY), (float) Math.toRadians(deltaRotation.x * MOUSE_SENSITIVITY));
        }

        soundManager.updateListenerPosition(camera);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        updateSkyBox(scene);
        animationData.nextFrame();
        if (animationData.getCurrentFrameIndex() == 45) bobSoundSource.play();
    }

    private void updateSkyBox(Scene scene) {
        Camera camera = scene.getCamera();
        Vector3f cameraPos = camera.getPosition();
        Entity skyBoxEntity = scene.getSkyBox().getEntity();
        skyBoxEntity.setPosition(cameraPos);
    }

}

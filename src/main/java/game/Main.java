package game;

import engine.level.Level;
import org.joml.Vector2f;
import org.joml.Vector3f;

import engine.*;
import engine.graph.render.Render;
import engine.input.*;
import engine.scene.*;
import engine.scene.view.*;
import engine.scene.model.*;
import engine.sound.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    public static Main Instance;

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.1f;

    private static Level level1;

    private SoundSource bobSoundSource;

    private AnimationData animationData;

    public static void main(String[] args) {
        Instance = new Main();

        Window.WindowOptions opts = new Window.WindowOptions(1280, 720);
        opts.antiAliasing = true;

        level1 = new Level("resources/levels/level1/level1.poop");

        Engine engine = new Engine("tont-doom", opts, Instance, level1);
        engine.start();
    }

    @Override
    public void cleanup() {}

    @Override
    public void init(Window window, Scene scene, Render render) {
        Camera camera = scene.getCamera();
        camera.moveUp(1.0f);

        level1.getScene().getModel("bob-model").getEntities().get(0).setAnimation(scene, 0);

        initSounds();
    }

    private void initSounds() {
        SoundManager soundManager = level1.getScene().getSoundManager();

        SoundBuffer bobbuf = new SoundBuffer("resources/sounds/creak1.ogg", SoundBuffer.FileType.OGG);
        soundManager.addSoundBuffer(bobbuf);

        bobSoundSource = new SoundSource(false, false);
        bobSoundSource.setPosition(level1.getScene().getModel("bob-model").getEntities().get(0).getPosition());
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
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        animationData.nextFrame();
        if (animationData.getCurrentFrameIndex() == 45) bobSoundSource.play();
    }

}

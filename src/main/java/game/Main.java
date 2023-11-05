package game;

import engine.level.Level;

import engine.*;
import engine.graph.render.Render;
import engine.input.*;
import engine.scene.*;
import engine.scene.view.*;
import engine.scene.model.*;

public class Main implements IAppLogic {

    public static Main Instance;
    public static CameraInput cameraInput;

    private static final Level level1 = new Level("resources/levels/level1/level1.poop");
    private Entity bobEntity;

    public static void main(String[] args) {
        Instance = new Main();

        Window.WindowOptions opts = new Window.WindowOptions(1280, 720);
        opts.antiAliasing = true;

        Engine engine = new Engine("tont-doom", opts, Instance, level1);
        engine.start();
    }

    @Override
    public void cleanup() {}

    @Override
    public void init(Window window, Scene scene, Render render) {
        Camera camera = scene.getCamera();
        camera.moveUp(1.0f);

        cameraInput = new CameraInput(camera, 0.1f, 0.1f);

        bobEntity = level1.getScene().getModel("bob-model").getEntities().get(0);
        bobEntity.setAnimation(scene, 0);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) return;
        cameraInput.input(window, diffTimeMillis);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        AnimationData bobAnimationData = bobEntity.getAnimationData();
        bobAnimationData.nextFrame();
        if (bobAnimationData.getCurrentFrameIndex() == 45) bobEntity.getSound().play();
    }

}

package engine.scene.model;

import engine.graph.model.Model;
import org.joml.Matrix4f;

import java.util.Arrays;

public class AnimationData {

    public static final Matrix4f[] DEFAULT_BONES_MATRICES = new Matrix4f[ModelLoader.MAX_BONES];

    static {
        Matrix4f zeroMatrix = new Matrix4f().zero();
        Arrays.fill(DEFAULT_BONES_MATRICES, zeroMatrix);
    }

    private Model.Animation currentAnimation;
    private int currentFrameIndex;

    public AnimationData(Model.Animation currentAnimation) {
        currentFrameIndex = 0;
        this.currentAnimation = currentAnimation;
    }

    public Model.Animation getCurrentAnimation() { return currentAnimation; }
    public Model.AnimatedFrame getCurrentFrame() { return currentAnimation.frames().get(currentFrameIndex); }
    public int getCurrentFrameIndex() { return currentFrameIndex; }

    public void nextFrame() {
        int nextFrame = currentFrameIndex + 1;
        if (nextFrame > currentAnimation.frames().size() - 1) currentFrameIndex = 0;
        else currentFrameIndex = nextFrame;
    }

    public void setCurrentAnimation(Model.Animation currentAnimation) {
        currentFrameIndex = 0;
        this.currentAnimation = currentAnimation;
    }

}

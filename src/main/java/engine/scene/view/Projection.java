package engine.scene.view;

import org.joml.Matrix4f;

public class Projection {

    private static final float FOV = (float) Math.toRadians(70.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    private Matrix4f matrix;

    public Projection(int width, int height) {
        matrix = new Matrix4f();
        updateProjection(width, height);
    }

    public Matrix4f getMatrix() { return matrix; }

    public void updateProjection(int width, int height) {
        matrix.setPerspective(FOV, (float) width / height, Z_NEAR, Z_FAR);
    }

}

package engine.scene.view;

import org.joml.Matrix4f;

public class Projection {

    public static final float FOV = (float) Math.toRadians(70.0f);
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 256.0f;

    private Matrix4f matrix;
    private Matrix4f inverseMatrix;

    public Projection(int width, int height) {
        matrix = new Matrix4f();
        inverseMatrix = new Matrix4f();
        updateProjection(width, height);
    }

    public Matrix4f getMatrix() { return matrix; }
    public Matrix4f getInverseMatrix() { return inverseMatrix; }

    public void updateProjection(int width, int height) {
        matrix.setPerspective(FOV, (float) width / height, Z_NEAR, Z_FAR);
        inverseMatrix.set(matrix).invert();
    }

}

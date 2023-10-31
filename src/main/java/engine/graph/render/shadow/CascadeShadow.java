package engine.graph.render.shadow;

import org.joml.*;

import engine.scene.Scene;

import java.util.List;
import java.lang.Math;

public class CascadeShadow {

    public static final int SHADOW_MAP_CASCADE_COUNT = 3;

    private Matrix4f projectionViewMatrix;
    private float splitDistance;

    public CascadeShadow() {
        projectionViewMatrix = new Matrix4f();
    }

    // derived from Sascha Willems' Vulkan implementation (MIT License)
    // https://johanmedestrom.wordpress.com/2016/03/18/opengl-cascaded-shadow-maps/
    public static void updateCascadeShadows(List<CascadeShadow> cascadeShadows, Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
        Matrix4f projectionMatrix = scene.getProjection().getMatrix();
        Vector4f lightPosition = new Vector4f(scene.getSceneLights().getDirectional().getDirection(), 0);

        float cascadeSplitLambda = 0.95f;

        float[] cascadeSplits = new float[SHADOW_MAP_CASCADE_COUNT];

        float nearClip = projectionMatrix.perspectiveNear();
        float farClip = projectionMatrix.perspectiveFar();
        float clip = farClip - nearClip;

        float maxZ = nearClip + clip;

        float range = maxZ - nearClip;
        float ratio = maxZ / nearClip;

        // calculate split depths from view camera frustum
        for (int i = 0; i < SHADOW_MAP_CASCADE_COUNT; i++) {
            float p = (i + 1) / (float) SHADOW_MAP_CASCADE_COUNT;
            float log = (float) (nearClip * Math.pow(ratio, p));
            float uniform = nearClip + range * p;
            float d = cascadeSplitLambda * (log - uniform) + uniform;
            cascadeSplits[i] = (d - nearClip) / clip;
        }

        // calculate orthographic projection matrix for each cascade
        float lastSplitDistance = 0.0f;
        for (int i = 0; i < SHADOW_MAP_CASCADE_COUNT; i++) {
            float splitDistance = cascadeSplits[i];

            Vector3f[] frustumCorners = new Vector3f[]{
                new Vector3f(-1.0f,  1.0f, -1.0f),
                new Vector3f( 1.0f,  1.0f, -1.0f),
                new Vector3f( 1.0f, -1.0f, -1.0f),
                new Vector3f(-1.0f, -1.0f, -1.0f),
                new Vector3f(-1.0f,  1.0f,  1.0f),
                new Vector3f( 1.0f,  1.0f,  1.0f),
                new Vector3f( 1.0f, -1.0f,  1.0f),
                new Vector3f(-1.0f, -1.0f,  1.0f)
            };

            int j;

            // project frustum corners into world space
            Matrix4f inverseCamera = (new Matrix4f(projectionMatrix).mul(viewMatrix)).invert();
            for (j = 0; j < 8; j++) {
                Vector4f inverseCorner = new Vector4f(frustumCorners[j], 1.0f).mul(inverseCamera);
                frustumCorners[j] = new Vector3f(inverseCorner.x / inverseCorner.w, inverseCorner.y / inverseCorner.w, inverseCorner.z / inverseCorner.w);
            }

            // adjust frustum corners to each split
            for (j = 0; j < 4; j++) {
                Vector3f distance = new Vector3f(frustumCorners[j + 4]).sub(frustumCorners[j]);
                frustumCorners[j + 4] = new Vector3f(frustumCorners[j]).add(new Vector3f(distance).mul(splitDistance));
                frustumCorners[j] = new Vector3f(frustumCorners[j]).add(new Vector3f(distance).mul(lastSplitDistance));
            }

            // get frustum center
            Vector3f frustumCenter = new Vector3f(0.0f);
            for (j = 0; j < 8; j++) { frustumCenter.add(frustumCorners[j]); }
            frustumCenter.div(8.0f);

            float radius = 0.0f;
            for (j = 0; j < 8; j++) {
                float distance = (new Vector3f(frustumCorners[j]).sub(frustumCenter)).length();
                radius = Math.max(radius, distance);
            }
            radius = (float) Math.ceil(radius * 16.0f) / 16.0f;

            // calculate light view and orthographic matrix
            Vector3f maxExtents = new Vector3f(radius);
            Vector3f minExtents = new Vector3f(maxExtents).mul(-1.0f);

            Vector3f lightDirection = (new Vector3f(lightPosition.x, lightPosition.y, lightPosition.z).mul(-1.0f)).normalize();
            Vector3f eye = new Vector3f(frustumCenter).sub(new Vector3f(lightDirection).mul(-minExtents.z));
            Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
            Matrix4f lightViewMatrix = new Matrix4f().lookAt(eye, frustumCenter, up);
            Matrix4f lightOrthographicMatrix = new Matrix4f().ortho(minExtents.x, maxExtents.x, minExtents.y, maxExtents.y, 0.0f, maxExtents.z - minExtents.z, true);

            CascadeShadow cascadeShadow = cascadeShadows.get(i);
            cascadeShadow.splitDistance = (nearClip + splitDistance * clip) * -1.0f;
            cascadeShadow.projectionViewMatrix = lightOrthographicMatrix.mul(lightViewMatrix);

            lastSplitDistance = cascadeSplits[i];
        }
    }

    public Matrix4f getProjectionViewMatrix() { return projectionViewMatrix; }
    public float getSplitDistance() { return splitDistance; }

}

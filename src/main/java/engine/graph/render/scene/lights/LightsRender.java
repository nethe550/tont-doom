package engine.graph.render.scene.lights;

import engine.graph.render.GBuffer;
import engine.graph.render.QuadMesh;
import engine.graph.render.shader.*;
import engine.scene.*;
import engine.scene.light.*;

import java.util.*;

import org.joml.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

public class LightsRender {

    public static final int MAX_POINT_LIGHTS = 16;
    public static final int MAX_SPOT_LIGHTS = 16;

    private final ShaderProgram shaderProgram;

    private QuadMesh quadMesh;
    private Uniforms uniforms;

    public LightsRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene/lights/lights.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene/lights/lights.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        quadMesh = new QuadMesh();
        uniforms = createUniforms();
    }

    private Uniforms createUniforms() {
        Uniforms u = new Uniforms(shaderProgram.getProgramID());
        u.createUniform("albedoSampler");
        u.createUniform("normalSampler");
        u.createUniform("specularSampler");
        u.createUniform("depthSampler");
        u.createUniform("inverseProjectionMatrix");

        u.createUniform("ambientLight.intensity");
        u.createUniform("ambientLight.color");

        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            String name = "pointLights[" + i + "]";
            u.createUniform(name + ".position");
            u.createUniform(name + ".color");
            u.createUniform(name + ".intensity");
            u.createUniform(name + ".attenuation.constant");
            u.createUniform(name + ".attenuation.linear");
            u.createUniform(name + ".attenuation.exponent");
        }

        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            String name = "spotLights[" + i + "]";
            u.createUniform(name + ".pl.position");
            u.createUniform(name + ".pl.color");
            u.createUniform(name + ".pl.intensity");
            u.createUniform(name + ".pl.attenuation.constant");
            u.createUniform(name + ".pl.attenuation.linear");
            u.createUniform(name + ".pl.attenuation.exponent");
            u.createUniform(name + ".direction");
            u.createUniform(name + ".innerCutoff");
            u.createUniform(name + ".outerCutoff");
        }

        u.createUniform("directionalLight.color");
        u.createUniform("directionalLight.direction");
        u.createUniform("directionalLight.intensity");

        u.createUniform("fog.activeFog");
        u.createUniform("fog.color");
        u.createUniform("fog.density");

        return u;
    }

    public void cleanup() {
        quadMesh.cleanup();
        shaderProgram.cleanup();
    }

    public void render(Scene scene, GBuffer gBuffer) {
        shaderProgram.bind();

        updateLights(scene);

        int[] textureIDs = gBuffer.getTextureIDs();
        int numTextures = textureIDs != null ? textureIDs.length : 0;
        for (int i = 0; i < numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIDs[i]);
        }

        uniforms.setUniform("albedoSampler", 0);
        uniforms.setUniform("normalSampler", 1);
        uniforms.setUniform("specularSampler", 2);
        uniforms.setUniform("depthSampler", 3);

        Fog fog = scene.getFog();
        uniforms.setUniform("fog.activeFog", fog.getActive() ? 1 : 0);
        uniforms.setUniform("fog.color", fog.getColor());
        uniforms.setUniform("fog.density", fog.getDensity());

        uniforms.setUniform("inverseProjectionMatrix", scene.getProjection().getInverseMatrix());

        glBindVertexArray(quadMesh.getVaoID());
        glDrawElements(GL_TRIANGLES, quadMesh.getNumVertices(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        shaderProgram.unbind();
    }

    private void updateLights(Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbient();
        uniforms.setUniform("ambientLight.intensity", ambientLight.getIntensity());
        uniforms.setUniform("ambientLight.color", ambientLight.getColor());

        DirectionalLight directionalLight = sceneLights.getDirectional();
        Vector4f auxDirection = new Vector4f(directionalLight.getDirection(), 0.0f);
        auxDirection.mul(viewMatrix);
        Vector3f direction = new Vector3f(auxDirection.x, auxDirection.y, auxDirection.z);
        uniforms.setUniform("directionalLight.color", directionalLight.getColor());
        uniforms.setUniform("directionalLight.direction", direction);
        uniforms.setUniform("directionalLight.intensity", directionalLight.getIntensity());

        List<PointLight> pointLights = sceneLights.getPoints();
        int numPointLights = pointLights.size();
        PointLight pointLight;
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < numPointLights) pointLight = pointLights.get(i);
            else pointLight = null;
            String name = "pointLights[" + i + "]";
            updatePointLight(pointLight, name, viewMatrix);
        }

        List<SpotLight> spotLights = sceneLights.getSpots();
        int numSpotLights = spotLights.size();
        SpotLight spotLight;
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if ( i < numSpotLights) spotLight = spotLights.get(i);
            else spotLight = null;
            String name = "spotLights[" + i + "]";
            updateSpotLight(spotLight, name, viewMatrix);
        }
    }

    private void updatePointLight(PointLight pointLight, String prefix, Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f();
        Vector3f lightPosition = new Vector3f();
        Vector3f color = new Vector3f();
        float intensity = 0.0f;
        float constant = 0.0f;
        float linear = 0.0f;
        float exponent = 0.0f;

        if (pointLight != null) {
            aux.set(pointLight.getPosition(), 1); // w=1; treat like position
            aux.mul(viewMatrix);
            lightPosition.set(aux.x, aux.y, aux.z);
            color.set(pointLight.getColor());
            intensity = pointLight.getIntensity();
            PointLight.Attenuation attenuation = pointLight.getAttenuation();
            constant = attenuation.getConstant();
            linear = attenuation.getLinear();
            exponent = attenuation.getExponent();
        }

        uniforms.setUniform(prefix + ".position", lightPosition);
        uniforms.setUniform(prefix + ".color", color);
        uniforms.setUniform(prefix + ".intensity", intensity);
        uniforms.setUniform(prefix + ".attenuation.constant", constant);
        uniforms.setUniform(prefix + ".attenuation.linear", linear);
        uniforms.setUniform(prefix + ".attenuation.exponent", exponent);
    }

    private void updateSpotLight(SpotLight spotLight, String prefix, Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f();
        Vector3f position = new Vector3f();
        Vector3f direction = new Vector3f();
        Vector3f color = new Vector3f();
        float intensity = 0.0f;
        float constant = 0.0f;
        float linear = 0.0f;
        float exponent = 0.0f;
        float innerCutoff = 0.0f;
        float outerCutoff = 0.0f;
        if (spotLight != null) {
            aux.set(spotLight.getPosition(), 1); // w=1; treat like position
            aux.mul(viewMatrix);
            position.set(aux.x, aux.y, aux.z);
            aux.set(spotLight.getDirection(), 0); // w=0; treat like direction
            aux.mul(viewMatrix);
            direction.set(aux.x, aux.y, aux.z);
            color.set(spotLight.getColor());
            intensity = spotLight.getIntensity();
            PointLight.Attenuation attenuation = spotLight.getAttenuation();
            constant = attenuation.getConstant();
            linear = attenuation.getLinear();
            exponent = attenuation.getExponent();
            innerCutoff = spotLight.getInnerCutoff();
            outerCutoff = spotLight.getOuterCutoff();
        }

        uniforms.setUniform(prefix + ".pl.position", position);
        uniforms.setUniform(prefix + ".direction", direction);
        uniforms.setUniform(prefix + ".pl.color", color);
        uniforms.setUniform(prefix + ".pl.intensity", intensity);
        uniforms.setUniform(prefix + ".pl.attenuation.constant", constant);
        uniforms.setUniform(prefix + ".pl.attenuation.linear", linear);
        uniforms.setUniform(prefix + ".pl.attenuation.exponent", exponent);
        uniforms.setUniform(prefix + ".innerCutoff", innerCutoff);
        uniforms.setUniform(prefix + ".outerCutoff", outerCutoff);
    }

}

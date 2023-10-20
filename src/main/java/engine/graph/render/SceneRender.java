package engine.graph.render;

import engine.graph.model.Material;
import engine.graph.model.Mesh;
import engine.graph.model.Model;
import engine.scene.light.*;
import engine.scene.model.Entity;
import engine.scene.Scene;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private static final int MAX_POINT_LIGHTS = 16;

    private final ShaderProgram shaderProgram;
    private final Uniforms uniforms;

    private float timeElapsed = 0.0f;
    private float width = 1.0f;
    private float height = 1.0f;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene/scene.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene/scene.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        uniforms = createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    private Uniforms createUniforms() {
        Uniforms u = new Uniforms(shaderProgram.getProgramID());
        u.createUniform("projectionMatrix");
        u.createUniform("viewMatrix");
        u.createUniform("modelMatrix");

        u.createUniform("texSampler");
        try {
            u.createUniform("timeElapsed");
            u.createUniform("resolution");
        }
        catch (Exception ignored) {}

        u.createUniform("material.ambient");
        u.createUniform("material.diffuse");
        u.createUniform("material.specular");
        u.createUniform("material.glossiness");

        u.createUniform("ambientLight.factor");
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

        u.createUniform("directionalLight.color");
        u.createUniform("directionalLight.direction");
        u.createUniform("directionalLight.intensity");
        return u;
    }

    public void update(float diffTimeMillis, int width, int height) {
        timeElapsed += diffTimeMillis;
        this.width = (float) width;
        this.height = (float) height;
    }

    public void render(Scene scene) {
        shaderProgram.bind();

        uniforms.setUniform("projectionMatrix", scene.getProjection().getMatrix());
        uniforms.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniforms.setUniform("texSampler", 0);
        if (uniforms.hasUniform("timeElapsed")) uniforms.setUniform("timeElapsed", timeElapsed);
        if (uniforms.hasUniform("resolution")) uniforms.setUniform("resolution", new Vector2f(width, height));

        updateLights(scene);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
                uniforms.setUniform("material.ambient", material.getAmbientColor());
                uniforms.setUniform("material.diffuse", material.getDiffuseColor());
                uniforms.setUniform("material.specular", material.getSpecularColor());
                uniforms.setUniform("material.glossiness", material.getSpecular());

                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                for (Mesh mesh : material.getMeshes()) {
                    glBindVertexArray(mesh.getVaoID());
                    for (Entity entity : entities) {
                        uniforms.setUniform("modelMatrix", entity.getModelMatrix());
                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    }
                }
            }
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    private void updateLights(Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbient();
        uniforms.setUniform("ambientLight.factor", ambientLight.getIntensity());
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
            aux.set(pointLight.getPosition(), 1);
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

}

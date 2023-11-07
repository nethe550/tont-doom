package engine.graph.render.skybox;

import engine.graph.render.Renderer;
import engine.graph.shader.ShaderProgram;
import engine.graph.shader.Uniforms;
import engine.graph.texture.Texture;
import engine.graph.texture.TextureCache;
import org.joml.Matrix4f;

import engine.graph.model.*;
import engine.scene.Scene;
import engine.scene.SkyBox;
import engine.scene.model.Entity;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class SkyBoxRender extends Renderer {

    private final Matrix4f viewMatrix;

    public SkyBoxRender() {
        shaderProgram = createShaderProgram();
        uniforms = createUniforms();
        viewMatrix = new Matrix4f();
    }

    public ShaderProgram createShaderProgram() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/skybox/skybox.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/skybox/skybox.fs", GL_FRAGMENT_SHADER));
        return new ShaderProgram(shaderModuleDataList);
    }

    public Uniforms createUniforms() {
        Uniforms u = new Uniforms(shaderProgram.getProgramID());
        u.createUniform("projectionMatrix");
        u.createUniform("viewMatrix");
        u.createUniform("modelMatrix");
        u.createUniform("diffuse");
        u.createUniform("texSampler");
        u.createUniform("hasTexture");
        return u;
    }

    public void update(float diffTimeMillis, int width, int height) {}

    public void render(Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox == null) return;
        shaderProgram.bind();

        uniforms.setUniform("projectionMatrix", scene.getProjection().getMatrix());
        viewMatrix.set(scene.getCamera().getViewMatrix());
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        uniforms.setUniform("viewMatrix", viewMatrix);
        uniforms.setUniform("texSampler", 0);

        Model skyBoxModel = skyBox.getModel();
        Entity skyBoxEntity = skyBox.getEntity();
        TextureCache textureCache = scene.getTextureCache();
        for (Material material : skyBoxModel.getMaterials()) {
            Texture texture = textureCache.getTexture(material.getTexturePath());
            glActiveTexture(GL_TEXTURE0);
            texture.bind();

            uniforms.setUniform("diffuse", material.getDiffuseColor());
            uniforms.setUniform("hasTexture", texture.getTexturePath().equals(TextureCache.DEFAULT_TEXTURE) ? 0 : 1);

            for (Mesh mesh : material.getMeshes()) {
                glBindVertexArray(mesh.getVaoID());

                uniforms.setUniform("modelMatrix", skyBoxEntity.getModelMatrix());
                glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
            }
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

}

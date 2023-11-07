package engine.graph.render.scene;

import engine.graph.render.GBuffer;
import engine.graph.render.shader.ShaderProgram;
import engine.graph.render.shader.Uniforms;
import engine.graph.render.texture.Texture;
import engine.graph.render.texture.TextureCache;
import org.joml.*;

import engine.graph.model.*;
import engine.scene.Fog;
import engine.scene.light.*;
import engine.scene.model.*;
import engine.scene.Scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private static final int MAX_POINT_LIGHTS = 16;
    private static final int MAX_SPOT_LIGHTS = 16;

    private final ShaderProgram shaderProgram;
    private final Uniforms uniforms;

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
        u.createUniform("boneMatrices");
        u.createUniform("texSampler");
        u.createUniform("normalTexSampler");
        try { u.createUniform("material.ambient"); } catch (Exception ignored) {}
        u.createUniform("material.diffuse");
        u.createUniform("material.specular");
        u.createUniform("material.glossiness");
        u.createUniform("material.hasNormalMap");
        u.createUniform("billboard");
        return u;
    }

    public void update(float diffTimeMillis, int width, int height) {}

    public void render(Scene scene, GBuffer gBuffer) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferID());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, gBuffer.getWidth(), gBuffer.getHeight());
        glDisable(GL_BLEND);

        shaderProgram.bind();

        uniforms.setUniform("projectionMatrix", scene.getProjection().getMatrix());
        uniforms.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniforms.setUniform("texSampler", 0);
        uniforms.setUniform("normalTexSampler", 1);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
                if (uniforms.hasUniform("material.ambient")) uniforms.setUniform("material.ambient", material.getAmbientColor());
                uniforms.setUniform("material.diffuse", material.getDiffuseColor());
                uniforms.setUniform("material.specular", material.getSpecularColor());
                uniforms.setUniform("material.glossiness", material.getSpecular());

                String normalMapPath = material.getNormalMapPath();
                boolean hasNormalMapPath = normalMapPath != null;
                uniforms.setUniform("material.hasNormalMap", hasNormalMapPath ? 1 : 0);

                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                if (hasNormalMapPath) {
                    Texture normalMapTexture = textureCache.getTexture(normalMapPath);
                    glActiveTexture(GL_TEXTURE1);
                    normalMapTexture.bind();
                }

                for (Mesh mesh : material.getMeshes()) {
                    glBindVertexArray(mesh.getVaoID());
                    for (Entity entity : entities) {
                        if (entity instanceof BillboardEntity) uniforms.setUniform("billboard", 1);
                        else uniforms.setUniform("billboard", 0);

                        uniforms.setUniform("modelMatrix", entity.getModelMatrix());
                        AnimationData animData = entity.getAnimationData();
                        uniforms.setUniform("boneMatrices", animData == null ? AnimationData.DEFAULT_BONES_MATRICES : animData.getCurrentFrame().boneMatrices());

                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    }
                }
            }
        }

        glBindVertexArray(0);
        glEnable(GL_BLEND);
        shaderProgram.unbind();
    }

}

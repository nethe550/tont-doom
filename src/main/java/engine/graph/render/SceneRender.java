package engine.graph.render;

import engine.graph.model.Material;
import engine.graph.model.Mesh;
import engine.graph.model.Model;
import engine.scene.model.Entity;
import engine.scene.Scene;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private final ShaderProgram shaderProgram;
    private final Uniforms uniforms;

    private float timeElapsed = 0.0f;
    private float width = 1.0f;
    private float height = 1.0f;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        uniforms = createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    private Uniforms createUniforms() {
        Uniforms u = new Uniforms(shaderProgram.getProgramID());
        try {
            u.createUniform("projectionMatrix");
            u.createUniform("viewMatrix");
            u.createUniform("modelMatrix");
            u.createUniform("texSampler");
            u.createUniform("material.diffuse");
            u.createUniform("resolution");
            u.createUniform("timeElapsed");
        }
        catch (Exception ignored) {}
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

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
                uniforms.setUniform("material.diffuse", material.getDiffuseColor());

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


}

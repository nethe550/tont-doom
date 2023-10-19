package engine.graph;

import engine.scene.Entity;
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
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<ShaderProgram.ShaderModuleData>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.fs", GL_FRAGMENT_SHADER));
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
            u.createUniform("modelMatrix");
            u.createUniform("timeElapsed");
            u.createUniform("resolution");
            u.createUniform("texSampler");
        } catch (Exception e) {}
        return u;
    }

    public void update(float diffTimeMillis, int width, int height) {
        timeElapsed += diffTimeMillis;
        this.width = (float) width;
        this.height = (float) height;
    }

    public void render(Scene scene) {
        shaderProgram.bind();

        if (uniforms.hasUniform("timeElapsed")) uniforms.setUniform("timeElapsed", timeElapsed);
        if (uniforms.hasUniform("projectionMatrix")) uniforms.setUniform("projectionMatrix", scene.getProjection().getMatrix());
        if (uniforms.hasUniform("resolution")) uniforms.setUniform("resolution", new Vector2f(width, height));
        if (uniforms.hasUniform("texSampler")) uniforms.setUniform("texSampler", 0);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
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

package engine.graph;

import engine.scene.Entity;
import engine.scene.Scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private final ShaderProgram shaderProgram;
    private final Uniforms uniforms;

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
        u.createUniform("projectionMatrix");
        u.createUniform("modelMatrix");
        return u;
    }

    public void render(Scene scene) {
        shaderProgram.bind();

        uniforms.setUniform("projectionMatrix", scene.getProjection().getMatrix());

        Collection<Model> models = scene.getModelMap().values();
        for (Model model : models) {
            model.getMeshes().stream().forEach(mesh -> {
                glBindVertexArray(mesh.getVaoID());
                List<Entity> entities = model.getEntities();
                for (Entity entity : entities) {
                    uniforms.setUniform("modelMatrix", entity.getModelMatrix());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
            });
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }


}

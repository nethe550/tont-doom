package engine.graph;

import engine.scene.Scene;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private final ShaderProgram shaderProgram;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<ShaderProgram.ShaderModuleData>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void render(Scene scene) {
        shaderProgram.bind();

        scene.getMeshMap().values().forEach(mesh -> {
            glBindVertexArray(mesh.getVaoID());
            glDrawArrays(GL_TRIANGLES, 0, mesh.getNumVertices());
        });

        glBindVertexArray(0);

        shaderProgram.unbind();
    }


}

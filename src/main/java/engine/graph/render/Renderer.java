package engine.graph.render;

import engine.graph.shader.ShaderProgram;
import engine.graph.shader.Uniforms;
import engine.scene.Scene;

public abstract class Renderer implements IRenderer {

    protected ShaderProgram shaderProgram;
    protected Uniforms uniforms;

    public abstract ShaderProgram createShaderProgram();
    public abstract Uniforms createUniforms();

    public void cleanup() {
        if (shaderProgram != null) shaderProgram.cleanup();
    }

    public abstract void update(float diffTimeMillis, int width, int height);

    public abstract void render(Scene scene);


}

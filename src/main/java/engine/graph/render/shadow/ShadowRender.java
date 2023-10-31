package engine.graph.render.shadow;

import java.util.*;

import engine.graph.model.*;
import engine.graph.render.shader.ShaderProgram;
import engine.graph.render.shader.Uniforms;
import engine.scene.*;
import engine.scene.model.AnimationData;
import engine.scene.model.Entity;

import static org.lwjgl.opengl.GL30.*;

public class ShadowRender {
    private ArrayList<CascadeShadow> cascadeShadows;
    private ShaderProgram shaderProgram;
    private ShadowBuffer shadowBuffer;
    private Uniforms uniforms;

    public ShadowRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/shadow/shadow.vs", GL_VERTEX_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);

        shadowBuffer = new ShadowBuffer();

        cascadeShadows = new ArrayList<>();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            CascadeShadow cascadeShadow = new CascadeShadow();
            cascadeShadows.add(cascadeShadow);
        }

        uniforms = createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        shadowBuffer.cleanup();
    }

    private Uniforms createUniforms() {
        Uniforms u = new Uniforms(shaderProgram.getProgramID());
        u.createUniform("modelMatrix");
        u.createUniform("projectionViewMatrix");
        u.createUniform("boneMatrices");
        return u;
    }

    public List<CascadeShadow> getCascadeShadows() { return cascadeShadows; }
    public ShadowBuffer getShadowBuffer() { return shadowBuffer; }

    public void render(Scene scene) {
        // TODO: optimize with shadow cache; currently re-rendering every frame
        CascadeShadow.updateCascadeShadows(cascadeShadows, scene);

        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);

        shaderProgram.bind();

        Collection<Model> models = scene.getModelMap().values();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIDs()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);

            CascadeShadow shadowCascade = cascadeShadows.get(i);
            uniforms.setUniform("projectionViewMatrix", shadowCascade.getProjectionViewMatrix());

            for (Model model : models) {
                List<Entity> entities = model.getEntities();

                for (Material material : model.getMaterials()) {
                    for (Mesh mesh : material.getMeshes()) {
                        glBindVertexArray(mesh.getVaoID());

                        for (Entity entity : entities) {
                            uniforms.setUniform("modelMatrix", entity.getModelMatrix());

                            AnimationData animationData = entity.getAnimationData();
                            uniforms.setUniform("boneMatrices", animationData == null ? AnimationData.DEFAULT_BONES_MATRICES : animationData.getCurrentFrame().boneMatrices());

                            glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        }

                    }
                }

            }
        }

        shaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}

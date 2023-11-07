package engine.graph.shader;

import org.joml.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL20.*;

public class Uniforms {

    private int programID;
    private Map<String, Integer> uniforms;

    public Uniforms(int programID) {
        this.programID = programID;
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programID, uniformName);
        if (uniformLocation < 0) throw new RuntimeException("Failed to locate uniform \"" + uniformName + "\" in shader program [" + programID + "]");
        uniforms.put(uniformName, uniformLocation);
    }

    public boolean hasUniform(String uniformName) {
        return uniforms.containsKey(uniformName);
    }

    private int getUniformLocation(String uniformName) {
        Integer location = uniforms.get(uniformName);
        if (location == null) throw new RuntimeException("Failed to find uniform \"" + uniformName + "\"");
        return location.intValue();
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, Vector2f value) {
        glUniform2fv(getUniformLocation(uniformName), new float[] { value.x, value.y });
    }

    public void setUniform(String uniformName, Vector3f value) {
        glUniform3fv(getUniformLocation(uniformName), new float[] { value.x, value.y, value.z });
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4fv(getUniformLocation(uniformName), new float[] { value.x, value.y, value.z, value.w });
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, Matrix4f[] values) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int length = values != null ? values.length : 0;
            FloatBuffer buf = stack.mallocFloat(16 * length);
            for (int i = 0; i < length; i++) { values[i].get(16 * i, buf); }
            glUniformMatrix4fv(getUniformLocation(uniformName), false, buf);
        }
    }

}

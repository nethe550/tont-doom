package engine.graph;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

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

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Integer location = uniforms.get(uniformName);
            if (location == null) throw new RuntimeException("Failed to find uniform \"" + uniformName + "\"");
            glUniformMatrix4fv(location, false, value.get(stack.mallocFloat(16)));
        }
    }

}

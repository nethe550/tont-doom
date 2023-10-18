package engine.graph;

import org.lwjgl.opengl.GL30;
import engine.util.Util;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class ShaderProgram {

    private final int programID;

    public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
        programID = glCreateProgram();
        if (programID == 0) throw new RuntimeException("Failed to create shader program.");

        List<Integer> shaderModules = new ArrayList<Integer>();
        shaderModuleDataList.forEach(s -> {
            shaderModules.add(createShader(Util.readFile(ShaderProgram.class, s.shaderFile), s.shaderType));
        });

        link(shaderModules);
    }

    public void bind() { glUseProgram(programID); }

    public void cleanup() {
        unbind();
        if (programID != 0) glDeleteProgram(programID);
    }

    protected int createShader(String shaderCode, int shaderType) {
        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) throw new RuntimeException("Failed to create shader. Type: " + shaderType);

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shaderID, 1024));

        glAttachShader(programID, shaderID);

        return shaderID;
    }

    public int getProgramID() { return programID; }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) throw new RuntimeException("Failed to link shader: " + glGetShaderInfoLog(programID, 1024));

        shaderModules.forEach(s -> glDetachShader(programID, s));
        shaderModules.forEach(GL30::glDeleteShader);
    }

    public void unbind() { glUseProgram(0); }

    public void validate() {
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) throw new RuntimeException("Failed to validate shader: " + glGetProgramInfoLog(programID, 1024));
    }

    public record ShaderModuleData(String shaderFile, int shaderType) {}

}

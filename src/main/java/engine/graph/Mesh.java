package engine.graph;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int numVertices;
    private final int vaoID;
    private final List<Integer> vboIDList;

    public Mesh(float[] positions, int numVertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.numVertices = numVertices;
            vboIDList = new ArrayList<Integer>();

            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            int vboID = glGenBuffers();
            vboIDList.add(vboID);
            FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
            positionsBuffer.put(0, positions);
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    public void cleanup() {
        vboIDList.forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoID);
    }

    public int getNumVertices() { return numVertices; }
    public final int getVaoID() { return vaoID; }

}

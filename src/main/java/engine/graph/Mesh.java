package engine.graph;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public static class MeshData {
        public float[] positions;
        public float[] texcoords;
        public int[] indices;
        public MeshData(float[] positions, float[] texcoords, int[] indices) {
            this.positions = positions;
            this.texcoords = texcoords;
            this.indices = indices;
        }
    }

    private int numVertices;
    private int vaoID;
    private List<Integer> vboIDList;

    public Mesh(MeshData data) {
        initGL(data.positions, data.texcoords, data.indices);
    }

    public Mesh(float[] positions, float[] texcoords, int[] indices) {
        initGL(positions, texcoords, indices);
    }

    private void initGL(float[] positions, float[] texcoords, int[] indices) {
        this.numVertices = indices.length;
        vboIDList = new ArrayList<>();

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // positions
        int vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer positionsBuffer = BufferUtils.createFloatBuffer(positions.length);
        positionsBuffer.put(positions).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // texcoords
        vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer texCoordsBuffer = BufferUtils.createFloatBuffer(texcoords.length);
        texCoordsBuffer.put(0, texcoords);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        vboID = glGenBuffers();
        vboIDList.add(vboID);

        // indices
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        vboIDList.forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoID);
    }

    public int getNumVertices() { return numVertices; }
    public final int getVaoID() { return vaoID; }

}

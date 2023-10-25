package engine.graph.model;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;

    public record MeshData(float[] positions, float[] texcoords, int[] indices, float[] normals, float[] tangents, float[] bitangents, int[] boneIndices, float[] weights) {}

    private int numVertices;
    private int vaoID;
    private List<Integer> vboIDList;

    public Mesh(MeshData data) {
        initGL(data.positions, data.texcoords, data.indices, data.normals, data.tangents, data.bitangents, data.boneIndices, data.weights);
    }

    public Mesh(float[] positions, float[] texcoords, int[] indices, float[] normals, float[] tangents, float[] bitangents) {
        this(positions, texcoords, indices, normals, tangents, bitangents, new int[Mesh.MAX_WEIGHTS * positions.length / 3], new float[Mesh.MAX_WEIGHTS * positions.length / 3]);
    }

    public Mesh(float[] positions, float[] texcoords, int[] indices, float[] normals, float[] tangents, float[] bitangents, int[] boneIndices, float[] weights) {
        initGL(positions, texcoords, indices, normals, tangents, bitangents, boneIndices, weights);
    }

    private void initGL(float[] positions, float[] texcoords, int[] indices, float[] normals, float[] tangents, float[] bitangents, int[] boneIndices, float[] weights) {
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

        // normals
        vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalsBuffer.put(0, normals);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

        // tangents
        vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer tangentsBuffer = BufferUtils.createFloatBuffer(tangents.length);
        tangentsBuffer.put(0, tangents);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, tangentsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

        // bitangents
        vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer bitangentsBuffer = BufferUtils.createFloatBuffer(bitangents.length);
        bitangentsBuffer.put(0, bitangents);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, bitangentsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

        // texcoords
        vboID = glGenBuffers();
        vboIDList.add(vboID);

        FloatBuffer texCoordsBuffer = BufferUtils.createFloatBuffer(texcoords.length);
        texCoordsBuffer.put(0, texcoords);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, 0, 0);

        // bone weights
        vboID = glGenBuffers();
        vboIDList.add(vboID);
        FloatBuffer weightsBuffer = BufferUtils.createFloatBuffer(weights.length);
        weightsBuffer.put(weights).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, 0, 0);

        // bone indices
        vboID = glGenBuffers();
        vboIDList.add(vboID);
        IntBuffer boneIndicesBuffer = BufferUtils.createIntBuffer(boneIndices.length);
        boneIndicesBuffer.put(boneIndices).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, boneIndicesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(6);
        glVertexAttribPointer(6, 4, GL_FLOAT, false, 0, 0);

        // indices
        vboID = glGenBuffers();
        vboIDList.add(vboID);

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

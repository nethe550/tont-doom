package engine.util;

import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.Model;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class Util {

    private static class Vertex {
        public Vector3f position;
        public Vector2f texcoord;
        public Vertex(Vector3f position, Vector2f texcoord) {
            this.position = position;
            this.texcoord = texcoord;
        }
    }

    public static <T> String readFile(Class<T> clazz, String resourcePath) {
        StringBuilder result = new StringBuilder();
        ClassLoader loader = clazz.getClassLoader();
        try {
            InputStream is = loader.getResourceAsStream(resourcePath);
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
            } else throw new RuntimeException("Resource not found: " + resourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
        return result.toString();
    }

    public static <T> ByteBuffer readImage(Class<T> clazz, String resourcePath, IntBuffer width, IntBuffer height, IntBuffer channels) {
        ByteBuffer imageBuffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Load the resource as an input stream
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Failed to read image \"" + resourcePath + "\"");
            }

            // Read the input stream into a ByteBuffer
            byte[] imageData = inputStream.readAllBytes();
            imageBuffer = stack.malloc(imageData.length);
            imageBuffer.put(imageData).flip();

            // Use STBImage to load the image
            imageBuffer = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (imageBuffer == null) {
                throw new RuntimeException("Failed to read image \"" + resourcePath + "\"\n" + STBImage.stbi_failure_reason());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return imageBuffer;
    }

    public static <T> Mesh.MeshData loadOBJMeshData(Class<T> clazz, String objPath) {
        String fileContent = readFile(clazz, objPath);

        List<Vector3f> positions = new ArrayList<>();
        List<Vector2f> texcoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        List<Vertex> uniqueVertices = new ArrayList<>();

        for (String line : fileContent.split("\n")) {
            String[] parts = line.split("\\s+");

            switch (parts[0]) {
                case "v" -> positions.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                case "vt" -> texcoords.add(new Vector2f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                case "f" -> {
                    for (int i = 1; i <= 3; i++) {
                        String[] vertexParts = parts[i].split("/");
                        // subtract 1 as .obj indices start at 1
                        int positionIndex = Integer.parseInt(vertexParts[0]) - 1;
                        int texcoordIndex = Integer.parseInt(vertexParts[1]) - 1;

                        Vertex vertex = new Vertex(positions.get(positionIndex), texcoords.get(texcoordIndex));

                        int index = uniqueVertices.indexOf(vertex);
                        if (index == -1) {
                            uniqueVertices.add(vertex);
                            index = uniqueVertices.size() - 1;
                        }

                        indices.add(index);
                    }
                }
            }
        }

        float[] aPositions = new float[uniqueVertices.size() * 3];
        float[] aTexcoords = new float[uniqueVertices.size() * 2];
        int[] aIndices = new int[indices.size()];
        float[] aColors = new float[aPositions.length];

        for (int i = 0; i < uniqueVertices.size(); i++) {
            Vertex vertex = uniqueVertices.get(i);
            aPositions[i * 3    ] = vertex.position.x;
            aPositions[i * 3 + 1] = vertex.position.y;
            aPositions[i * 3 + 2] = vertex.position.z;
            aTexcoords[i * 2    ] = vertex.texcoord.x;
            aTexcoords[i * 2 + 1] = vertex.texcoord.y;
        }

        for (int i = 0; i < indices.size(); i++) { aIndices[i] = indices.get(i); }
        Arrays.fill(aColors, 1.0f);

        return new Mesh.MeshData(aPositions, aColors, aTexcoords, aIndices);
    }

    public static <T> float[] loadVertexColors(Class<T> clazz, String resourcePath) {
        String fileContent = readFile(clazz, resourcePath);

        Map<Integer, Vector3f> colors = new HashMap<Integer, Vector3f>();

        for (String line : fileContent.split("\n")) {
            String[] parts = line.split("\\s+");
            int vertexIndex = Integer.parseInt(parts[0]);
            Vector3f color = new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
            colors.put(vertexIndex, color);
        }

        float[] vertexColors = new float[colors.values().size() * 3];

        SortedSet<Integer> keys = new TreeSet<Integer>(colors.keySet());
        for (int key : keys) {
            Vector3f color = colors.get(key);
            vertexColors[key * 3    ] = color.x;
            vertexColors[key * 3 + 1] = color.y;
            vertexColors[key * 3 + 2] = color.z;
        }

        return vertexColors;
    }

}

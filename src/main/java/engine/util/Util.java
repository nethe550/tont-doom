package engine.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Util {

    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read file: \"" + filePath + "\"", e);
        }
        return str;
    }

    public static float[] listToFloatArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] arr = new float[size];
        for (int i = 0; i < size; i++) { arr[i] = list.get(i); }
        return arr;
    }

    public static int[] listToIntArray(List<Integer> list) {
        return list.stream().mapToInt((Integer v) -> v).toArray();
    }

}

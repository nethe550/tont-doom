package engine.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Util {

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
            }
            else throw new RuntimeException("Resource not found: " + resourcePath);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
        return result.toString();
    }

    private Util() {}

//    public static String readFile(String path) {
//        String str;
//        try {
//            str = new String(Files.readAllBytes(Paths.get(path)));
//        }
//        catch (IOException e) {
//            throw new RuntimeException("Failed to read file \"" + path + "\"", e);
//        }
//        return str;
//    }

}

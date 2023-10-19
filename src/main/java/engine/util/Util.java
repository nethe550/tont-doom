package engine.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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

}

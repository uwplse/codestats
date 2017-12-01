package edu.washington.cse.codestats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Slurper {
    public static byte[] slurp(String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputStream.read(buffer);
        return buffer;
    }
}

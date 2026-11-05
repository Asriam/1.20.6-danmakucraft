package com.adrian.thDanmakuCraft.util.io;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class FileLoader {

    public static String readFile (String fileName) throws FileNotFoundException {
        return new BufferedReader(new FileReader(fileName)).lines().collect(Collectors.joining("\n"));
    }
}

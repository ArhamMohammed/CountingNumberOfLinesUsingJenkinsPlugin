package com.example.counting.numberoflines.methods;

import java.io.*;

public class countLinesInTheFile {
//    public static int countLines(File path) {
////        String[] splittingTheFile = content.split("\n");
////        return splittingTheFile.length;
//
//        int lines = 0;
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
////        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
//            // new FileInputStream(path): This part creates a FileInputStream object,
//            // which is used for reading bytes from the specified file (path).
//
//            // new InputStreamReader(new FileInputStream(path), "UTF-8"):
//            // This part wraps the FileInputStream with an InputStreamReader.
//            // The InputStreamReader translates bytes into characters using a specified character encoding.
//            // In this case, "UTF-8" is used as the character encoding.
//
//            // new BufferedReader(new InputStreamReader(...)):
//            // This part wraps the InputStreamReader with a BufferedReader.
//            // The BufferedReader reads text from a character-input stream,
//            // buffering characters to provide efficient reading of characters, arrays, and lines.
//            //            while (reader.readLine() != null) {
//            //                lines++;
//            //            }
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Process each line or perform actions with the line
//                lines++;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return lines;
//    }
    public static int countLinesFromAString(String content) {
        int count = 0;
        for(int i =0; i<content.length();i++){
            if(content.charAt(i) == '\n')
                count++;
        }
        return count;
    }
}

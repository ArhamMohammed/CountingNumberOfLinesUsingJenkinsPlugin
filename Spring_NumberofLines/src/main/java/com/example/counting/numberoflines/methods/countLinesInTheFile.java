package com.example.counting.numberoflines.methods;

public class countLinesInTheFile {
    public static int countLinesFromAString(String content) {
        return (int) content.chars().filter(ch -> ch == '\n').count();
    }
}

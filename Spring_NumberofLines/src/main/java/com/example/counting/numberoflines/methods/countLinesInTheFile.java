package com.example.counting.numberoflines.methods;

import java.io.IOException;
import java.io.Reader;

public class CountLinesInTheFile {
    public CountLinesInTheFile(){
        // Private constructor to prevent instantiation
    }
    public int countingLinesInTheFile(String content) {
        return (int) content.chars().filter(ch -> ch == '\n').count();
    }
    public int countingLinesInTheFile(Reader reader) throws IOException {
        char [] cout = new char[1024];
        int numChar = 0;
        int count =0;
        while (numChar != -1){
            numChar = reader.read(cout,0,1024);
            if(numChar !=-1){
            for(char charCount:cout){
                if(charCount == '\n')
                    count++;
            }
            }
        }
        return count;
    }
}

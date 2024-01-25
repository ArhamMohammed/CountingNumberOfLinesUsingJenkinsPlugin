package com.example.counting.numberoflines.methods;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadContentFromTheFile {

    public ReadContentFromTheFile(){
        // Private constructor to prevent instantiation
    }

    public String readContent(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            InputStreamReader: This class is a bridge from byte streams (like InputStream) to character streams
//            like Reader and its subclasses.
//            It reads bytes from the InputStream and decodes them into characters using a specified charset or
//            the default charset if not specified.
//            BufferedReader: This class reads text from a character-input stream, buffering characters
//            to provide for the efficient reading of characters, arrays, and lines.
//            In the context of your code, BufferedReader is wrapped around InputStreamReader.
//            This buffered reader allows efficient reading (e.g., reading line by line) from the InputStream.
            return IOUtils.toString(reader);
//            This method, from Apache Commons IO library, reads from a Reader and returns the content as a String.
//            Internally, it reads the content from the Reader in chunks and builds the String.
//            Reader input: The parameter input is expected to be a Reader instance.
//            In this context, you're passing the BufferedReader (which is a subtype of Reader) to this method.
        }
    }
}

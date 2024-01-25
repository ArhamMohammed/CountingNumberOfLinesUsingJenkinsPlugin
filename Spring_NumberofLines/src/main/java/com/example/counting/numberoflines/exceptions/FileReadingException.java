package com.example.counting.numberoflines.exceptions;

public class FileReadingException extends RuntimeException{

    public FileReadingException(String message, Throwable cause){
        super(message,cause);
    }
}

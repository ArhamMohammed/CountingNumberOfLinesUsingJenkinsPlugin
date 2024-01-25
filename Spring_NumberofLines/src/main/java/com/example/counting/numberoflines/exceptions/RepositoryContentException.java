package com.example.counting.numberoflines.exceptions;

public class RepositoryContentException extends RuntimeException{

    public RepositoryContentException(String message, Throwable cause){
        super(message,cause);
    }
}

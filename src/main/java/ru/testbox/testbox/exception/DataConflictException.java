package ru.testbox.testbox.exception;

public class DataConflictException extends RuntimeException{
    public DataConflictException(String message) {
        super(message);
    }
}

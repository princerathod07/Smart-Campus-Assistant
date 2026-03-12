package com.campus.exceptions;

/**
 * FileStorageException — thrown when file I/O operations fail.
 * Demonstrates: Custom Exception Handling, checked exceptions
 */
public class FileStorageException extends Exception {

    private String filePath;
    private String operation; // READ, WRITE, DELETE

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileStorageException(String filePath, String operation, String message) {
        super("File " + operation + " failed on [" + filePath + "]: " + message);
        this.filePath  = filePath;
        this.operation = operation;
    }

    public FileStorageException(String filePath, String operation,
                                 String message, Throwable cause) {
        super("File " + operation + " failed on [" + filePath + "]: " + message, cause);
        this.filePath  = filePath;
        this.operation = operation;
    }

    public String getFilePath()  { return filePath; }
    public String getOperation() { return operation; }
}

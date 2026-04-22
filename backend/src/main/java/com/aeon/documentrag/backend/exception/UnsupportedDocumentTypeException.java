package com.aeon.documentrag.backend.exception;

public class UnsupportedDocumentTypeException extends RuntimeException {

    public UnsupportedDocumentTypeException(String message) {
        super(message);
    }
}

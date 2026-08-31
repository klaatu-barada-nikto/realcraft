package com.realcraft.buildmodel.service;

public class ModelDownloadException extends RuntimeException {

    public ModelDownloadException(String message) {
        super(message);
    }

    public ModelDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
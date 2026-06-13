package com.linkmesh.exception;

public class PremiumRequiredException extends RuntimeException {
    public PremiumRequiredException(String feature) {
        super("'" + feature + "' is a Premium feature. Please upgrade your plan.");
    }
}
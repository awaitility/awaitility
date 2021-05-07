package org.awaitility.core;

/**
 * If thrown, indicates terminal failure condition has been reached, and that the system should immediately stop polling
 * conditions and fail.
 */
public class TerminalFailureException extends RuntimeException {
    public TerminalFailureException(final String message) {
        super(message);
    }
}
package com.campus.exceptions;

/**
 * LoginException — thrown when authentication fails.
 * Demonstrates: Custom Exception Handling
 */
public class LoginException extends Exception {

    private String username;
    private String reason;

    public LoginException(String message) {
        super(message);
        this.reason = message;
    }

    public LoginException(String username, String reason) {
        super("Login failed for user '" + username + "': " + reason);
        this.username = username;
        this.reason = reason;
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getUsername() { return username; }
    public String getReason()   { return reason; }
}
